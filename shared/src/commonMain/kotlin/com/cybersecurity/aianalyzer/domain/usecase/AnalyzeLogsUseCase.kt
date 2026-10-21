package com.cybersecurity.aianalyzer.domain.usecase

import com.cybersecurity.aianalyzer.domain.repository.LogAnalyzerRepository
import com.cybersecurity.aianalyzer.model.Vulnerability

class AnalyzeLogsUseCase(
    private val repository: LogAnalyzerRepository
) {
    suspend operator fun invoke(logs: String): Result<List<Vulnerability>> {
        if (logs.isBlank()) {
            return Result.failure(IllegalArgumentException("Logs content cannot be empty"))
        }
        return repository.analyzeLogs(logs)
    }
}
