package com.cybersecurity.aianalyzer.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnalyzeLogsRequest(
    val logs: String
)
