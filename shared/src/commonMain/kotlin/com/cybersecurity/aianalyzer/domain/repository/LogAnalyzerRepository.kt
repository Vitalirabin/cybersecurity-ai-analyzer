package com.cybersecurity.aianalyzer.domain.repository

import com.cybersecurity.aianalyzer.model.Vulnerability

interface LogAnalyzerRepository {
    suspend fun analyzeLogs(logs: String): Result<List<Vulnerability>>
}
