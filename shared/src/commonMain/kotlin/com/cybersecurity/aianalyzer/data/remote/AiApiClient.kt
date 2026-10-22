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

class AiApiClient {
    private val apiKey: String = AppConfig.OPENROUTER_API_KEY
    private val baseUrl: String = "https://openrouter.ai/api/v1/chat/completions"
    private val model: String = "nvidia/nemotron-3-ultra-550b-a55b:free"
    private val httpClient: HttpClient = createDefaultHttpClient()
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

        println("AiApiClient: Sending request to model '$model' with logs length ${logs.length}, apiKey provided: ${effectiveKey.isNotBlank()}")
        val chatRequest = ChatCompletionRequest(
            model = model,
            messages = listOf(
                ChatMessage(role = "system", content = systemPrompt),
                ChatMessage(role = "user", content = logs)
            )
        )

        val requestBodyJson = jsonFormat.encodeToString(chatRequest)
        println("AiApiClient: Request JSON: $requestBodyJson")

        val httpResponse = httpClient.post(baseUrl) {
            contentType(ContentType.Application.Json)
            if (effectiveKey.isNotBlank()) {
                header(HttpHeaders.Authorization, "Bearer $effectiveKey")
            }
            header("HTTP-Referer", "https://github.com/cybersecurity-ai-analyzer")
            header("X-Title", "Cybersecurity AI Analyzer")

            setBody(chatRequest)
        }

        val responseBodyText = httpResponse.bodyAsText()
        println("AiApiClient: Received raw response text: $responseBodyText")

        val response: ChatCompletionResponse = jsonFormat.decodeFromString(responseBodyText)

        val content = response.choices?.firstOrNull()?.message?.content
        println("AiApiClient: Extracted content: $content")

        if (content.isNullOrBlank()) {
            throw IllegalStateException("Empty response from AI model ($model)")
        }

        return parseVulnerabilitiesJson(content)
    }

    private fun parseVulnerabilitiesJson(jsonText: String): List<VulnerabilityDto> {
        val cleanedJson = jsonText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        println("AiApiClient: Parsing JSON text: '$cleanedJson'")

        return try {
            jsonFormat.decodeFromString<List<VulnerabilityDto>>(cleanedJson)
        } catch (e: Exception) {
            println("AiApiClient: Failed to parse JSON directly: ${e.message}. Treating as descriptive response.")
            if (cleanedJson.isNotBlank()) {
                listOf(VulnerabilityDto(description = cleanedJson, risk = "Low"))
            } else {
                emptyList()
            }
        }
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
