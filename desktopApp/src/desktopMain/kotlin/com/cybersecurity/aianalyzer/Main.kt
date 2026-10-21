package com.cybersecurity.aianalyzer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KMP Starter (AGP 9+)"
    ) {
        App()
    }
}
