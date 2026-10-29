package com.cybersecurity.aianalyzer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cybersecurity.aianalyzer.ui.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settingsState = settingsViewModel.settingsState
    val strings = settingsState.strings

    val colorScheme = when (settingsState.selectedColor) {
        AppThemeColor.PURPLE -> lightColorScheme(primary = AppThemeColor.PURPLE.color)
        AppThemeColor.BLUE -> lightColorScheme(primary = AppThemeColor.BLUE.color)
        AppThemeColor.GREEN -> lightColorScheme(primary = AppThemeColor.GREEN.color)
        AppThemeColor.CRIMSON -> lightColorScheme(primary = AppThemeColor.CRIMSON.color)
    }

    var selectedTab by remember { mutableStateOf(0) }
    val analyzerViewModel: LogAnalyzerViewModel = koinViewModel()

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text(strings.analyzerTab) },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = null) },
                        label = { Text(strings.historyTab) },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(strings.settingsTab) },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                }
            }
        ) { paddingValues ->
            when (selectedTab) {
                0 -> LogAnalyzerScreen(
                    strings = strings,
                    maxHistoryLimit = settingsState.historyLimit,
                    modifier = Modifier.padding(paddingValues),
                    viewModel = analyzerViewModel
                )
                1 -> HistoryScreen(
                    onSelectItem = { item ->
                        analyzerViewModel.logs = item.fullLogs
                        analyzerViewModel.results = item.vulnerabilities
                        selectedTab = 0
                    },
                    strings = strings,
                    modifier = Modifier.padding(paddingValues)
                )
                2 -> SettingsScreen(
                    modifier = Modifier.padding(paddingValues),
                    viewModel = settingsViewModel
                )
            }
        }
    }
}
