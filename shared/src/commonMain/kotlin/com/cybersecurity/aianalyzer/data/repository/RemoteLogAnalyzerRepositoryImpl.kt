package com.cybersecurity.aianalyzer.data.repository

import com.cybersecurity.aianalyzer.data.dto.AnalyzeLogsRequest
import com.cybersecurity.aianalyzer.data.dto.AnalyzeLogsResponse
import com.cybersecurity.aianalyzer.data.remote.AiApiClient
import com.cybersecurity.aianalyzer.domain.repository.LogAnalyzerRepository
import com.cybersecurity.aianalyzer.model.Vulnerability
import com.cybersecurity.aianalyzer.util.getPlatformServerUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RemoteLogAnalyzerRepositoryImpl(
    private val serverUrl: String = getPlatformServerUrl(),
    private val httpClient: HttpClient = AiApiClient.createDefaultHttpClient()
) : LogAnalyzerRepository {

    override suspend fun analyzeLogs(logs: String): Result<List<Vulnerability>> {
        return runCatching {
            val response: AnalyzeLogsResponse = httpClient.post("$serverUrl/api/analyze-logs") {
                contentType(ContentType.Application.Json)
                setBody(AnalyzeLogsRequest(logs = logs))
            }.body()

            if (response.error != null) {
                throw IllegalStateException(response.error)
            }
            response.vulnerabilities
        }
    }
}
