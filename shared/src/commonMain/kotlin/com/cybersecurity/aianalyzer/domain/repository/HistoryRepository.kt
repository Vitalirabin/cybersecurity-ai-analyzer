package com.cybersecurity.aianalyzer.domain.repository

import com.cybersecurity.aianalyzer.model.AnalysisHistoryItem

interface HistoryRepository {
    fun getHistory(): List<AnalysisHistoryItem>
    fun saveHistory(items: List<AnalysisHistoryItem>)
    fun clearHistory()
}
