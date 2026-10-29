package com.cybersecurity.aianalyzer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cybersecurity.aianalyzer.domain.repository.HistoryRepository
import com.cybersecurity.aianalyzer.domain.usecase.AnalyzeLogsUseCase
import com.cybersecurity.aianalyzer.model.Vulnerability

class LogAnalyzerViewModel(
    private val analyzeLogsUseCase: AnalyzeLogsUseCase,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    var logs by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var results by mutableStateOf<List<Vulnerability>>(emptyList())

    fun findCachedResult(logsText: String): List<Vulnerability>? {
        val trimmed = logsText.trim()
        val history = historyRepository.getHistory()
        val match = history.firstOrNull { it.fullLogs.trim() == trimmed }
        return match?.vulnerabilities
    }

    fun saveToHistory(logsText: String, vulnerabilities: List<Vulnerability>, maxLimit: Int) {
        val trimmed = logsText.trim()
        val history = historyRepository.getHistory().toMutableList()

        history.removeAll { it.fullLogs.trim() == trimmed }

        val snippet = trimmed.take(150)
        val newItem = com.cybersecurity.aianalyzer.model.AnalysisHistoryItem(
            id = history.size.toString() + "_" + trimmed.hashCode(),
            timestamp = 0L,
            logsSnippet = snippet,
            fullLogs = trimmed,
            vulnerabilities = vulnerabilities
        )
        history.add(0, newItem)

        while (history.size > maxLimit) {
            history.removeAt(history.size - 1)
        }

        historyRepository.saveHistory(history)
    }

    suspend fun analyzeLogs(maxLimit: Int) {
        if (logs.isBlank()) return
        errorMessage = null

        val cached = findCachedResult(logs)
        if (cached != null) {
            results = cached
            saveToHistory(logs, cached, maxLimit)
            return
        }

        isLoading = true
        results = emptyList()

        val result = analyzeLogsUseCase(logs)
        result.fold(
            onSuccess = { vulnerabilities ->
                results = vulnerabilities
                isLoading = false
                saveToHistory(logs, vulnerabilities, maxLimit)
            },
            onFailure = { error ->
                errorMessage = error.message ?: "Failed to analyze logs"
                isLoading = false
            }
        )
    }
}
