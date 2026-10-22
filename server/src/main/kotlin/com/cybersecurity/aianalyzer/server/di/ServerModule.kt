package com.cybersecurity.aianalyzer.server.di

import com.cybersecurity.aianalyzer.data.remote.AiApiClient
import com.cybersecurity.aianalyzer.data.repository.LogAnalyzerRepositoryImpl
import com.cybersecurity.aianalyzer.domain.repository.LogAnalyzerRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val serverModule = module {
    singleOf(::AiApiClient)
    single<LogAnalyzerRepository> { LogAnalyzerRepositoryImpl(get()) }
}
