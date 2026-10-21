package com.cybersecurity.aianalyzer.data.remote

import com.cybersecurity.aianalyzer.config.AppConfig
import com.cybersecurity.aianalyzer.data.dto.ChatCompletionRequest
import com.cybersecurity.aianalyzer.data.dto.ChatCompletionResponse
import com.cybersecurity.aianalyzer.data.dto.ChatMessage
import com.cybersecurity.aianalyzer.data.dto.VulnerabilityDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AiApiClient(
    private val apiKey: String = AppConfig.OPENROUTER_API_KEY,
    private val baseUrl: String = "https://openrouter.ai/api/v1/chat/completions",
    private val model: String = "google/gemma-4-26b-a4b-it",
    private val httpClient: HttpClient = createDefaultHttpClient()
) {
    suspend fun analyzeLogs(
        logs: String,
        apiKey: String = this.apiKey,
        model: String = this.model
    ): List<VulnerabilityDto> {
        val effectiveKey = apiKey.ifBlank { this.apiKey.ifBlank { AppConfig.OPENROUTER_API_KEY } }
        val systemPrompt = """
            You are a cybersecurity expert analyzing system logs and code for vulnerabilities.
            Analyze the provided logs/code and return a JSON array of findings.
            Each object in the array MUST have:
            - "description": clear summary of the detected security vulnerability or anomaly
            - "risk": one of "High", "Medium", "Low"
            
            OUTPUT ONLY VALID JSON ARRAY OF OBJECTS AND NOTHING ELSE. Example:
            [{"description": "SQL Injection pattern detected", "risk": "High"}]
        """.trimIndent()

        val response: ChatCompletionResponse = httpClient.post(baseUrl) {
            contentType(ContentType.Application.Json)
            if (effectiveKey.isNotBlank()) {
                header(HttpHeaders.Authorization, "Bearer $effectiveKey")
            }
            header("HTTP-Referer", "https://github.com/cybersecurity-ai-analyzer")
            header("X-Title", "Cybersecurity AI Analyzer")

            setBody(
                ChatCompletionRequest(
                    model = model,
                    messages = listOf(
                        ChatMessage(role = "system", content = systemPrompt),
                        ChatMessage(role = "user", content = logs)
                    )
                )
            )
        }.body()

        val content = response.choices?.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Empty response from AI model")

        return parseVulnerabilitiesJson(content)
    }

    private fun parseVulnerabilitiesJson(jsonText: String): List<VulnerabilityDto> {
        val cleanedJson = jsonText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return jsonFormat.decodeFromString<List<VulnerabilityDto>>(cleanedJson)
    }

    companion object {
        private val jsonFormat = Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        }

        fun createDefaultHttpClient(): HttpClient {
            return HttpClient {
                install(ContentNegotiation) {
                    json(jsonFormat)
                }
            }
        }
    }
}
