package com.cybersecurity.aianalyzer.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.cybersecurity.aianalyzer.App
import com.cybersecurity.aianalyzer.di.initKoin
import kotlinx.browser.document
import org.koin.core.context.GlobalContext

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    if (GlobalContext.getOrNull() == null) {
        initKoin()
    }
    document.getElementById("RootContainer")?.let {
        ComposeViewport(it) {
            App()
        }
    }
}
