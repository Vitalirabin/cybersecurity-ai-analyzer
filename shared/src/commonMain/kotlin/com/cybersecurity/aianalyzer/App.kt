package com.cybersecurity.aianalyzer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.cybersecurity.aianalyzer.ui.LogAnalyzerScreen

@Composable
fun App() {
    MaterialTheme {
        Scaffold { it->
            LogAnalyzerScreen()
        }
    }
}
