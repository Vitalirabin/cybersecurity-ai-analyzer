package com.cybersecurity.aianalyzer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.cybersecurity.aianalyzer.di.initKoin
import org.koin.core.context.GlobalContext
import org.koin.logger.slf4jLogger

fun main() {
    if (GlobalContext.getOrNull() == null) {
        initKoin {
            slf4jLogger()
        }
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Cybersecurity AI Analyzer"
        ) {
            App()
        }
    }
}
