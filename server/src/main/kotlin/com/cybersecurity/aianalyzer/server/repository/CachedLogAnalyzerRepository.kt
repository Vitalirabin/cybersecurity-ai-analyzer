package com.cybersecurity.aianalyzer.server.repository

import com.cybersecurity.aianalyzer.config.AppConfig
import com.cybersecurity.aianalyzer.data.dto.ChatCompletionRequest
import com.cybersecurity.aianalyzer.data.dto.ChatMessage
import com.cybersecurity.aianalyzer.data.remote.AiApiClient
import com.cybersecurity.aianalyzer.domain.repository.LogAnalyzerRepository
import com.cybersecurity.aianalyzer.model.RiskLevel
import com.cybersecurity.aianalyzer.model.Vulnerability
import com.cybersecurity.aianalyzer.server.db.AiCacheTable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class CachedLogAnalyzerRepository(
    private val aiApiClient: AiApiClient
) : LogAnalyzerRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun analyzeLogs(logs: String): Result<List<Vulnerability>> {
        return runCatching {
            val effectiveKey = AppConfig.OPENROUTER_API_KEY
            val isLocal = logs.length <= AppConfig.SMALL_LOG_THRESHOLD
            val targetUrl = if (isLocal) AppConfig.OLLAMA_BASE_URL else AppConfig.CLOUD_BASE_URL
            val targetModel = if (isLocal) AppConfig.OLLAMA_MODEL else AppConfig.CLOUD_MODEL

            val trimmedLogs = logs.trim()

            // 1. Check cache in Database for exact same prompt and model
            val cachedResponseJson: String? = transaction {
                AiCacheTable.selectAll()
                    .where { (AiCacheTable.requestPrompt eq trimmedLogs) and (AiCacheTable.modelName eq targetModel) }
                    .limit(1)
                    .map { it[AiCacheTable.responseJson] }
                    .singleOrNull()
            }

            if (cachedResponseJson != null) {
                return@runCatching json.decodeFromString<List<Vulnerability>>(cachedResponseJson)
            }

            // 2. Not found in cache, call API
            val systemPrompt = """
                You are a cybersecurity expert analyzing system logs and code for vulnerabilities.
                Analyze the provided logs/code and return a JSON array of findings.
                Each object in the array MUST have:
                - "description": clear summary of the detected security vulnerability or anomaly
                - "risk": one of "High", "Medium", "Low"
                
                OUTPUT ONLY VALID JSON ARRAY OF OBJECTS AND NOTHING ELSE. Example:
                [{"description": "SQL Injection pattern detected", "risk": "High"}]
            """.trimIndent()

            val modelsList = if (isLocal) listOf(targetModel) else (listOf(targetModel) + AppConfig.FALLBACK_MODELS).distinct()

            val chatRequest = ChatCompletionRequest(
                model = targetModel,
                models = modelsList,
                messages = listOf(
                    ChatMessage(role = "system", content = systemPrompt),
                    ChatMessage(role = "user", content = logs)
                ),
                stream = false
            )

            val dtos = try {
                aiApiClient.sendChatCompletion(targetUrl, chatRequest, if (isLocal) "" else effectiveKey)
            } catch (_: Exception) {
                val fallbackIsLocal = !isLocal
                val fallbackUrl = if (fallbackIsLocal) AppConfig.OLLAMA_BASE_URL else AppConfig.CLOUD_BASE_URL
                val fallbackModel = if (fallbackIsLocal) AppConfig.OLLAMA_MODEL else cloudModelForFallback(targetModel)

                val fallbackRequest = ChatCompletionRequest(
                    model = fallbackModel,
                    models = listOf(fallbackModel),
                    messages = listOf(
                        ChatMessage(role = "system", content = systemPrompt),
                        ChatMessage(role = "user", content = logs)
                    ),
                    stream = false
                )
                aiApiClient.sendChatCompletion(fallbackUrl, fallbackRequest, if (fallbackIsLocal) "" else effectiveKey)
            }

            val vulnerabilities = dtos.map { dto ->
                Vulnerability(
                    description = dto.description,
                    risk = parseRiskLevel(dto.risk)
                )
            }

            // 3. Save to database cache
            val responseToCache = json.encodeToString(vulnerabilities)
            transaction {
                AiCacheTable.insert {
                    it[requestPrompt] = trimmedLogs
                    it[modelName] = targetModel
                    it[responseJson] = responseToCache
                }
            }

            vulnerabilities
        }
    }

    private fun cloudModelForFallback(currentModel: String): String {
        return AppConfig.FALLBACK_MODELS.firstOrNull { it != currentModel } ?: AppConfig.CLOUD_MODEL
    }

    private fun parseRiskLevel(riskStr: String): RiskLevel {
        return when (riskStr.lowercase().trim()) {
            "high", "critical" -> RiskLevel.High
            "medium", "moderate" -> RiskLevel.Medium
            "low", "info" -> RiskLevel.Low
            else -> RiskLevel.Medium
        }
    }
}
