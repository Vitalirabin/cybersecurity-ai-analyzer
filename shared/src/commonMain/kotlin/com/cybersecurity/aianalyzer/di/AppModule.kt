package com.cybersecurity.aianalyzer.di

import com.cybersecurity.aianalyzer.data.remote.AiApiClient
import com.cybersecurity.aianalyzer.data.repository.PersistentHistoryRepository
import com.cybersecurity.aianalyzer.domain.repository.HistoryRepository
import com.cybersecurity.aianalyzer.domain.repository.LogAnalyzerRepository
import com.cybersecurity.aianalyzer.data.repository.RemoteLogAnalyzerRepositoryImpl
import com.cybersecurity.aianalyzer.domain.usecase.AnalyzeLogsUseCase
import com.cybersecurity.aianalyzer.ui.HistoryViewModel
import com.cybersecurity.aianalyzer.ui.LogAnalyzerViewModel
import com.cybersecurity.aianalyzer.ui.SettingsViewModel
import com.cybersecurity.aianalyzer.util.KeyValueStorage
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::AiApiClient)
    single { KeyValueStorage() }
    single<LogAnalyzerRepository> { RemoteLogAnalyzerRepositoryImpl() }
    single<HistoryRepository> { PersistentHistoryRepository(get()) }
    factoryOf(::AnalyzeLogsUseCase)
    viewModelOf(::LogAnalyzerViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::SettingsViewModel)
}
