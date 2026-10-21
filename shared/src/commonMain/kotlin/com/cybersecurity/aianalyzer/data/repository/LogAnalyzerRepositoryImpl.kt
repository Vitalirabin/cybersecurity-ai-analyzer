package com.cybersecurity.aianalyzer.data.repository

import com.cybersecurity.aianalyzer.data.remote.AiApiClient
import com.cybersecurity.aianalyzer.domain.repository.LogAnalyzerRepository
import com.cybersecurity.aianalyzer.model.RiskLevel
import com.cybersecurity.aianalyzer.model.Vulnerability

class LogAnalyzerRepositoryImpl(
    private val aiApiClient: AiApiClient = AiApiClient()
) : LogAnalyzerRepository {

    override suspend fun analyzeLogs(logs: String): Result<List<Vulnerability>> {
        return runCatching {
            val dtos = aiApiClient.analyzeLogs(logs)
            dtos.map { dto ->
                Vulnerability(
                    description = dto.description,
                    risk = parseRiskLevel(dto.risk)
                )
            }
        }.recoverCatching { throwable ->
            performLocalFallbackAnalysis(logs, throwable)
        }
    }

    private fun parseRiskLevel(riskStr: String): RiskLevel {
        return when (riskStr.lowercase().trim()) {
            "high", "critical" -> RiskLevel.High
            "medium", "moderate" -> RiskLevel.Medium
            "low", "info" -> RiskLevel.Low
            else -> RiskLevel.Medium
        }
    }

    private fun performLocalFallbackAnalysis(
        logs: String,
        originalError: Throwable
    ): List<Vulnerability> {
        val findings = mutableListOf<Vulnerability>()
        val lowercaseLogs = logs.lowercase()

        if (lowercaseLogs.contains("select") && (lowercaseLogs.contains("or 1=1") || lowercaseLogs.contains(
                "union"
            ) || lowercaseLogs.contains("'"))
        ) {
            findings.add(
                Vulnerability(
                    "Potential SQL Injection (SQLi) pattern detected in query logs",
                    RiskLevel.High
                )
            )
        }
        if (lowercaseLogs.contains("failed login") || lowercaseLogs.contains("unauthorized") || lowercaseLogs.contains(
                "401"
            )
        ) {
            findings.add(
                Vulnerability(
                    "Suspicious repetitive failed authentication attempts (Brute Force risk)",
                    RiskLevel.Medium
                )
            )
        }
        if (lowercaseLogs.contains("http://") || lowercaseLogs.contains("cleartext")) {
            findings.add(
                Vulnerability(
                    "Unencrypted sensitive data transmission (HTTP cleartext) detected",
                    RiskLevel.High
                )
            )
        }
        if (lowercaseLogs.contains("exception") || lowercaseLogs.contains("error") || lowercaseLogs.contains(
                "stacktrace"
            )
        ) {
            findings.add(
                Vulnerability(
                    "Sensitive technical information leakage in log error outputs",
                    RiskLevel.Low
                )
            )
        }

        if (findings.isEmpty()) {
            if (originalError is IllegalStateException || originalError.message?.contains("API") == true) {
                throw originalError
            }
            findings.add(
                Vulnerability(
                    "No explicit threat patterns found in input logs",
                    RiskLevel.Low
                )
            )
        }

        return findings
    }
}
