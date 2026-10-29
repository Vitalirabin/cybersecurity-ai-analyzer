package com.cybersecurity.aianalyzer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cybersecurity.aianalyzer.domain.repository.HistoryRepository
import com.cybersecurity.aianalyzer.model.AnalysisHistoryItem

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {
    var historyItems by mutableStateOf(historyRepository.getHistory())
        private set

    fun refreshHistory() {
        historyItems = historyRepository.getHistory()
    }

    fun clearHistory() {
        historyRepository.clearHistory()
        historyItems = emptyList()
    }
}
