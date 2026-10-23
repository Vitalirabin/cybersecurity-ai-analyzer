package com.cybersecurity.aianalyzer.server.plugins

import com.cybersecurity.aianalyzer.data.dto.AnalyzeLogsRequest
import com.cybersecurity.aianalyzer.data.dto.AnalyzeLogsResponse
import com.cybersecurity.aianalyzer.domain.usecase.AnalyzeLogsUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val analyzeLogsUseCase: AnalyzeLogsUseCase by inject()

    routing {
        post("/api/analyze-logs") {
            try {
                val request = call.receive<AnalyzeLogsRequest>()
                log.info("Received analyze-logs request. Logs length: ${request.logs.length}, apiKey provided: ${request.apiKey.isNotBlank()}")
                log.info("Request logs content:\n${request.logs}")

                val result = analyzeLogsUseCase(request.logs, request.apiKey)

                result.fold(
                    onSuccess = { vulnerabilities ->
                        log.info("POST /api/analyze-logs processed successfully. Found ${vulnerabilities.size} vulnerabilities: $vulnerabilities")
                        call.respond(
                            HttpStatusCode.OK,
                            AnalyzeLogsResponse(vulnerabilities = vulnerabilities)
                        )
                    },
                    onFailure = { error ->
                        log.error("Failed to analyze logs: ${error.message}", error)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            AnalyzeLogsResponse(error = error.message ?: "Analysis failed")
                        )
                    }
                )
            } catch (e: Exception) {
                log.error("Invalid request format: ${e.message}", e)
                call.respond(
                    HttpStatusCode.BadRequest,
                    AnalyzeLogsResponse(error = "Invalid request format: ${e.message}")
                )
            }
        }
    }
}
