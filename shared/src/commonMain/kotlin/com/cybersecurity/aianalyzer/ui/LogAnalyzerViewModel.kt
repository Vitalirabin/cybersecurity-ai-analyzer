package com.cybersecurity.aianalyzer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cybersecurity.aianalyzer.data.repository.LogAnalyzerRepositoryImpl
import com.cybersecurity.aianalyzer.domain.usecase.AnalyzeLogsUseCase
import com.cybersecurity.aianalyzer.model.Vulnerability

class LogAnalyzerViewModel(
    private val analyzeLogsUseCase: AnalyzeLogsUseCase = AnalyzeLogsUseCase(LogAnalyzerRepositoryImpl())
) {
    var logs by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var results by mutableStateOf<List<Vulnerability>>(emptyList())

    suspend fun analyzeLogs() {
        if (logs.isBlank()) return
        isLoading = true
        errorMessage = null
        results = emptyList()

        val result = analyzeLogsUseCase(logs)
        result.fold(
            onSuccess = { vulnerabilities ->
                results = vulnerabilities
                isLoading = false
            },
            onFailure = { error ->
                errorMessage = error.message ?: "Failed to analyze logs"
                isLoading = false
            }
        )
    }
}
