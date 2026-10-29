package com.cybersecurity.aianalyzer.model

import kotlinx.serialization.Serializable

@Serializable
data class AnalysisHistoryItem(
    val id: String,
    val timestamp: Long,
    val logsSnippet: String,
    val fullLogs: String,
    val vulnerabilities: List<Vulnerability>
)
