package com.cybersecurity.aianalyzer

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cybersecurity.aianalyzer.ui.AppThemeColor
import com.cybersecurity.aianalyzer.ui.LogAnalyzerScreen
import com.cybersecurity.aianalyzer.ui.SettingsScreen
import com.cybersecurity.aianalyzer.ui.SettingsState

@Composable
fun App() {
    val settingsState = remember { SettingsState() }
    val strings = settingsState.strings

    val colorScheme = when (settingsState.selectedColor) {
        AppThemeColor.PURPLE -> lightColorScheme(primary = AppThemeColor.PURPLE.color)
        AppThemeColor.BLUE -> lightColorScheme(primary = AppThemeColor.BLUE.color)
        AppThemeColor.GREEN -> lightColorScheme(primary = AppThemeColor.GREEN.color)
        AppThemeColor.CRIMSON -> lightColorScheme(primary = AppThemeColor.CRIMSON.color)
    }

    var selectedTab by remember { mutableStateOf(0) }

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text(strings.analyzerTab) },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(strings.settingsTab) },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }
            }
        ) { paddingValues ->
            when (selectedTab) {
                0 -> LogAnalyzerScreen(
                    strings = strings,
                    modifier = Modifier.padding(paddingValues)
                )
                1 -> SettingsScreen(
                    settingsState = settingsState,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
