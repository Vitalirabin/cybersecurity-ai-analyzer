package com.cybersecurity.aianalyzer.data.repository

import com.cybersecurity.aianalyzer.domain.repository.HistoryRepository
import com.cybersecurity.aianalyzer.model.AnalysisHistoryItem
import com.cybersecurity.aianalyzer.util.KeyValueStorage
import kotlinx.serialization.json.Json

class PersistentHistoryRepository(
    private val storage: KeyValueStorage
) : HistoryRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val key = "saved_analysis_history"

    override fun getHistory(): List<AnalysisHistoryItem> {
        val raw = storage.getString(key, "")
        if (raw.isBlank()) return emptyList()
        return try {
            json.decodeFromString<List<AnalysisHistoryItem>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun saveHistory(items: List<AnalysisHistoryItem>) {
        try {
            val raw = json.encodeToString(items)
            storage.setString(key, raw)
        } catch (_: Exception) {
        }
    }

    override fun clearHistory() {
        storage.setString(key, "")
    }
}
