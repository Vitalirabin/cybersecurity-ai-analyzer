package com.cybersecurity.aianalyzer.data.remote

import com.cybersecurity.aianalyzer.data.dto.ChatCompletionRequest
import com.cybersecurity.aianalyzer.data.dto.ChatCompletionResponse
import com.cybersecurity.aianalyzer.data.dto.VulnerabilityDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
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
    private val httpClient: HttpClient = createDefaultHttpClient()

    suspend fun sendChatCompletion(
        url: String,
        request: ChatCompletionRequest,
        apiKey: String
    ): List<VulnerabilityDto> {
        val httpResponse = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            if (apiKey.isNotBlank()) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header("HTTP-Referer", "https://github.com/cybersecurity-ai-analyzer")
                header("X-Title", "Cybersecurity AI Analyzer")
            }
            setBody(request)
        }

        val responseBodyText = httpResponse.bodyAsText()
        if (httpResponse.status.value !in 200..299) {
            val errorMsg = extractErrorMessage(responseBodyText) ?: "HTTP ${httpResponse.status.value}"
            throw IllegalStateException("API error: $errorMsg")
        }

        val content = extractMessageContent(responseBodyText)
            ?: throw IllegalStateException("Empty response from AI model")

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

        try {
            return jsonFormat.decodeFromString<List<VulnerabilityDto>>(cleanedJson)
        } catch (_: Exception) {}

        try {
            val singleDto = jsonFormat.decodeFromString<VulnerabilityDto>(cleanedJson)
            return listOf(singleDto)
        } catch (_: Exception) {}

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
                install(HttpTimeout) {
                    requestTimeoutMillis = 120_000
                    connectTimeoutMillis = 30_000
                    socketTimeoutMillis = 120_000
                }
            }
        }
    }
}
