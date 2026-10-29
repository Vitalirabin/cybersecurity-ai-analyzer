package com.cybersecurity.aianalyzer.data.repository

import com.cybersecurity.aianalyzer.domain.repository.HistoryRepository
import com.cybersecurity.aianalyzer.model.AnalysisHistoryItem

class InMemoryHistoryRepository : HistoryRepository {
    private var items = listOf<AnalysisHistoryItem>()

    override fun getHistory(): List<AnalysisHistoryItem> = items

    override fun saveHistory(items: List<AnalysisHistoryItem>) {
        this.items = items
    }

    override fun clearHistory() {
        this.items = emptyList()
    }
}
