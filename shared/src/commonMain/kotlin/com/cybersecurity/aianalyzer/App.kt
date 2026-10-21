package com.cybersecurity.aianalyzer

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cybersecurity.aianalyzer.ui.LogAnalyzerScreen

@Composable
fun App() {
    MaterialTheme {
        Scaffold { paddingValues ->
            LogAnalyzerScreen(
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
