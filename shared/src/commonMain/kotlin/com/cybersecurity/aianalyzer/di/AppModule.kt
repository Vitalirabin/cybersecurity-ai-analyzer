package com.cybersecurity.aianalyzer.di

import com.cybersecurity.aianalyzer.data.remote.AiApiClient
import com.cybersecurity.aianalyzer.data.repository.LogAnalyzerRepositoryImpl
import com.cybersecurity.aianalyzer.domain.repository.LogAnalyzerRepository
import com.cybersecurity.aianalyzer.domain.usecase.AnalyzeLogsUseCase
import com.cybersecurity.aianalyzer.ui.LogAnalyzerViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    singleOf(::AiApiClient)
    singleOf(::LogAnalyzerRepositoryImpl) bind LogAnalyzerRepository::class
    factoryOf(::AnalyzeLogsUseCase)
    viewModelOf(::LogAnalyzerViewModel)
}
