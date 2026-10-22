package com.cybersecurity.aianalyzer.server.plugins

import com.cybersecurity.aianalyzer.server.di.serverModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(serverModule)
    }
}
