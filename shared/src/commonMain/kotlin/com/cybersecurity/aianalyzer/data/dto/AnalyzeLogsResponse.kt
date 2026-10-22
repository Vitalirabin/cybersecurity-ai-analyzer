package com.cybersecurity.aianalyzer.data.dto

import com.cybersecurity.aianalyzer.model.Vulnerability
import kotlinx.serialization.Serializable

@Serializable
data class AnalyzeLogsResponse(
    val vulnerabilities: List<Vulnerability> = emptyList(),
    val error: String? = null
)
