package com.cybersecurity.aianalyzer.data.repository

import com.cybersecurity.aianalyzer.config.AppConfig
import com.cybersecurity.aianalyzer.data.dto.ChatCompletionRequest
import com.cybersecurity.aianalyzer.data.dto.ChatMessage
import com.cybersecurity.aianalyzer.data.remote.AiApiClient
import com.cybersecurity.aianalyzer.domain.repository.LogAnalyzerRepository
import com.cybersecurity.aianalyzer.model.RiskLevel
import com.cybersecurity.aianalyzer.model.Vulnerability

class LogAnalyzerRepositoryImpl(
    private val aiApiClient: AiApiClient
) : LogAnalyzerRepository {

    override suspend fun analyzeLogs(logs: String, apiKey: String): Result<List<Vulnerability>> {
        return runCatching {
            val routing = determineRouting(logs)
            val request = createChatRequest(logs, routing.targetModel, routing.isLocal)

            val dtos = try {
                executeRequest(routing.targetUrl, request, apiKey, routing.isLocal)
            } catch (_: Exception) {
                executeFallbackRequest(logs, routing.isLocal, apiKey)
            }

            dtos.map { dto ->
                Vulnerability(
                    description = dto.description,
                    risk = parseRiskLevel(dto.risk)
                )
            }
        }
    }

    private data class RoutingConfig(
        val isLocal: Boolean,
        val targetUrl: String,
        val targetModel: String
    )

    private fun determineRouting(logs: String): RoutingConfig {
        val isLocal = logs.length <= AppConfig.SMALL_LOG_THRESHOLD
        return RoutingConfig(
            isLocal = isLocal,
            targetUrl = if (isLocal) AppConfig.OLLAMA_BASE_URL else AppConfig.CLOUD_BASE_URL,
            targetModel = if (isLocal) AppConfig.OLLAMA_MODEL else AppConfig.CLOUD_MODEL
        )
    }

    private fun createChatRequest(logs: String, model: String, isLocal: Boolean): ChatCompletionRequest {
        val systemPrompt = """
            You are a cybersecurity expert analyzing system logs and code for vulnerabilities.
            Analyze the provided logs/code and return a JSON array of findings.
            Each object in the array MUST have:
            - "description": clear summary of the detected security vulnerability or anomaly
            - "risk": one of "High", "Medium", "Low"
            
            OUTPUT ONLY VALID JSON ARRAY OF OBJECTS AND NOTHING ELSE. Example:
            [{"description": "SQL Injection pattern detected", "risk": "High"}]
        """.trimIndent()

        val modelsList = if (isLocal) listOf(model) else (listOf(model) + AppConfig.FALLBACK_MODELS).distinct()

        return ChatCompletionRequest(
            model = model,
            models = modelsList,
            messages = listOf(
                ChatMessage(role = "system", content = systemPrompt),
                ChatMessage(role = "user", content = logs)
            ),
            stream = false
        )
    }

    private suspend fun executeRequest(
        url: String,
        request: ChatCompletionRequest,
        apiKey: String,
        isLocal: Boolean
    ) = aiApiClient.sendChatCompletion(
        url = url,
        request = request,
        apiKey = if (isLocal) "" else apiKey.ifBlank { AppConfig.OPENROUTER_API_KEY }
    )

    private suspend fun executeFallbackRequest(
        logs: String,
        wasLocal: Boolean,
        apiKey: String
    ) = run {
        val fallbackIsLocal = !wasLocal
        val fallbackUrl = if (fallbackIsLocal) AppConfig.OLLAMA_BASE_URL else AppConfig.CLOUD_BASE_URL
        val fallbackModel = if (fallbackIsLocal) AppConfig.OLLAMA_MODEL else AppConfig.CLOUD_MODEL
        val fallbackRequest = createChatRequest(logs, fallbackModel, fallbackIsLocal)
        
        aiApiClient.sendChatCompletion(
            url = fallbackUrl,
            request = fallbackRequest,
            apiKey = if (fallbackIsLocal) "" else apiKey.ifBlank { AppConfig.OPENROUTER_API_KEY }
        )
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
