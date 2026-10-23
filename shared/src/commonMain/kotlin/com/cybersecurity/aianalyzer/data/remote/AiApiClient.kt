package com.cybersecurity.aianalyzer.data.remote

import com.cybersecurity.aianalyzer.config.AppConfig
import com.cybersecurity.aianalyzer.data.dto.ChatCompletionRequest
import com.cybersecurity.aianalyzer.data.dto.ChatCompletionResponse
import com.cybersecurity.aianalyzer.data.dto.ChatMessage
import com.cybersecurity.aianalyzer.data.dto.VulnerabilityDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AiApiClient {
    private val apiKey: String = AppConfig.OPENROUTER_API_KEY
    private val baseUrl: String = AppConfig.CLOUD_BASE_URL
    private val model: String = AppConfig.CLOUD_MODEL
    
    // Ollama local configuration
    private val ollamaBaseUrl: String = AppConfig.OLLAMA_BASE_URL
    private val ollamaModel: String = AppConfig.OLLAMA_MODEL

    private val httpClient: HttpClient = createDefaultHttpClient()

    private data class AnalysisRoutingConfig(
        val isLocal: Boolean,
        val targetModel: String,
        val targetUrl: String
    )

    suspend fun analyzeLogs(
        logs: String,
        apiKey: String = this.apiKey,
        model: String = this.model
    ): List<VulnerabilityDto> {
        val effectiveKey = apiKey.ifBlank { this.apiKey.ifBlank { AppConfig.OPENROUTER_API_KEY } }
        val systemPrompt = createSystemPrompt()

        val primaryConfig = determineRoutingConfig(logs, model)
        println("AiApiClient: Log length is ${logs.length}. Primary choice: ${if (primaryConfig.isLocal) "Local Ollama (${primaryConfig.targetModel})" else "Cloud Model (${primaryConfig.targetModel})"}")

        return try {
            executeAnalysisRequest(logs, systemPrompt, primaryConfig, effectiveKey)
        } catch (e: Exception) {
            println("AiApiClient: Primary request failed (${e.message}). Attempting fallback...")
            val fallbackConfig = createFallbackConfig(primaryConfig.isLocal, model)
            executeAnalysisRequest(logs, systemPrompt, fallbackConfig, effectiveKey)
        }
    }

    private fun createSystemPrompt(): String {
        return """
            You are a cybersecurity expert analyzing system logs and code for vulnerabilities.
            Analyze the provided logs/code and return a JSON array of findings.
            Each object in the array MUST have:
            - "description": clear summary of the detected security vulnerability or anomaly
            - "risk": one of "High", "Medium", "Low"
            
            OUTPUT ONLY VALID JSON ARRAY OF OBJECTS AND NOTHING ELSE. Example:
            [{"description": "SQL Injection pattern detected", "risk": "High"}]
        """.trimIndent()
    }

    private fun determineRoutingConfig(logs: String, cloudModel: String): AnalysisRoutingConfig {
        val smallLogThreshold = AppConfig.SMALL_LOG_THRESHOLD
        val isSmallLog = logs.length <= smallLogThreshold
        return AnalysisRoutingConfig(
            isLocal = isSmallLog,
            targetModel = if (isSmallLog) ollamaModel else cloudModel,
            targetUrl = if (isSmallLog) ollamaBaseUrl else baseUrl
        )
    }

    private fun createFallbackConfig(wasLocal: Boolean, cloudModel: String): AnalysisRoutingConfig {
        val fallbackIsLocal = !wasLocal
        return AnalysisRoutingConfig(
            isLocal = fallbackIsLocal,
            targetModel = if (fallbackIsLocal) ollamaModel else cloudModel,
            targetUrl = if (fallbackIsLocal) ollamaBaseUrl else baseUrl
        )
    }

    private suspend fun executeAnalysisRequest(
        logs: String,
        systemPrompt: String,
        config: AnalysisRoutingConfig,
        effectiveKey: String
    ): List<VulnerabilityDto> {
        val chatRequest = if (config.isLocal) {
            ChatCompletionRequest(
                model = config.targetModel,
                messages = listOf(
                    ChatMessage(role = "system", content = systemPrompt),
                    ChatMessage(role = "user", content = logs)
                ),
                stream = false
            )
        } else {
            val fallbackCloudModels = (listOf(config.targetModel) + AppConfig.FALLBACK_MODELS).distinct()

            ChatCompletionRequest(
                model = config.targetModel,
                models = fallbackCloudModels,
                messages = listOf(
                    ChatMessage(role = "system", content = systemPrompt),
                    ChatMessage(role = "user", content = logs)
                ),
                stream = false
            )
        }

        val httpResponse = httpClient.post(config.targetUrl) {
            contentType(ContentType.Application.Json)
            if (!config.isLocal && effectiveKey.isNotBlank()) {
                header(HttpHeaders.Authorization, "Bearer $effectiveKey")
                header("HTTP-Referer", "https://github.com/cybersecurity-ai-analyzer")
                header("X-Title", "Cybersecurity AI Analyzer")
            }
            setBody(chatRequest)
        }

        val responseBodyText = httpResponse.bodyAsText()
        println("AiApiClient: Received response from ${if (config.isLocal) "Ollama" else "Cloud"}: $responseBodyText")

        if (httpResponse.status.value !in 200..299) {
            val errorMsg =
                extractErrorMessage(responseBodyText) ?: "HTTP ${httpResponse.status.value}"
            throw IllegalStateException("API error from ${if (config.isLocal) "Ollama" else "Cloud"}: $errorMsg")
        }

        val content = extractMessageContent(responseBodyText)

        if (content.isNullOrBlank()) {
            throw IllegalStateException("Empty response from AI model (${config.targetModel})")
        }

        return parseVulnerabilitiesJson(content)
    }

    private fun extractErrorMessage(responseBodyText: String): String? {
        return try {
            val jsonElement = jsonFormat.parseToJsonElement(responseBodyText)
            val errorObj = jsonElement.jsonObject["error"]?.jsonObject
            errorObj?.get("message")?.jsonPrimitive?.content
        } catch (_: Exception) {
            null
        }
    }

    private fun extractMessageContent(responseBodyText: String): String? {
        val ndjsonContent = extractContentFromNdjson(responseBodyText)
        if (!ndjsonContent.isNullOrBlank()) {
            return ndjsonContent
        }

        return try {
            val response: ChatCompletionResponse = jsonFormat.decodeFromString(responseBodyText)
            response.choices?.firstOrNull()?.message?.content 
                ?: response.message?.content
                ?: extractContentManually(responseBodyText)
        } catch (_: Exception) {
            extractContentManually(responseBodyText)
        }
    }

    private fun extractContentFromNdjson(responseBodyText: String): String? {
        return if (responseBodyText.contains("\n{") || responseBodyText.trim().startsWith("{")) {
            val stringBuilder = StringBuilder()
            responseBodyText.lineSequence().forEach { line ->
                if (line.isNotBlank()) {
                    try {
                        val jsonElement = jsonFormat.parseToJsonElement(line)
                        val messageObj = jsonElement.jsonObject["message"]?.jsonObject
                        val contentPart = messageObj?.get("content")?.jsonPrimitive?.content
                        if (contentPart != null) {
                            stringBuilder.append(contentPart)
                        }
                    } catch (_: Exception) {}
                }
            }
            val res = stringBuilder.toString()
            if (res.isNotBlank()) res else null
        } else {
            null
        }
    }

    private fun extractContentManually(responseBodyText: String): String? {
        return try {
            val jsonElement = jsonFormat.parseToJsonElement(responseBodyText)
            val jsonObj = jsonElement.jsonObject
            val messageObj = jsonObj["message"]?.jsonObject
            messageObj?.get("content")?.jsonPrimitive?.content
        } catch (_: Exception) {
            null
        }
    }

    private fun parseVulnerabilitiesJson(jsonText: String): List<VulnerabilityDto> {
        val cleanedJson = jsonText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        println("AiApiClient: Parsing JSON text: '$cleanedJson'")

        // If the cleaned json is already a valid JSON list, decode it directly
        try {
            return jsonFormat.decodeFromString<List<VulnerabilityDto>>(cleanedJson)
        } catch (_: Exception) {}

        // Fallback: If it's a single JSON object instead of an array, wrap it in a list
        try {
            val singleDto = jsonFormat.decodeFromString<VulnerabilityDto>(cleanedJson)
            return listOf(singleDto)
        } catch (_: Exception) {}

        // Final fallback: treat as freeform text description
        return if (cleanedJson.isNotBlank()) {
            listOf(VulnerabilityDto(description = cleanedJson, risk = "Low"))
        } else {
            emptyList()
        }
    }

    companion object {
        private val jsonFormat = Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
            explicitNulls = false
        }

        fun createDefaultHttpClient(): HttpClient {
            return HttpClient {
                install(ContentNegotiation) {
                    json(jsonFormat)
                }
                install(io.ktor.client.plugins.HttpTimeout) {
                    requestTimeoutMillis = 120_000
                    connectTimeoutMillis = 30_000
                    socketTimeoutMillis = 120_000
                }
            }
        }
    }
}
