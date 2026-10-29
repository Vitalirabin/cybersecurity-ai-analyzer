package com.cybersecurity.aianalyzer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class AppLanguage(val title: String) {
    EN("English"),
    RU("Русский")
}

enum class AppThemeColor(val title: String, val color: Color) {
    PURPLE("Deep Purple", Color(0xFF6750A4)),
    BLUE("Ocean Blue", Color(0xFF0061A4)),
    GREEN("Forest Green", Color(0xFF006D3B)),
    CRIMSON("Crimson Red", Color(0xFF9C4146))
}

class SettingsState {
    var selectedLanguage by mutableStateOf(AppLanguage.EN)
    var selectedColor by mutableStateOf(AppThemeColor.PURPLE)

    val strings: Strings
        get() = getStrings(selectedLanguage)
}

@Composable
fun SettingsScreen(
    settingsState: SettingsState,
    modifier: Modifier = Modifier
) {
    val strings = settingsState.strings

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = strings.settingsTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Language selection
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = strings.interfaceLanguage,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AppLanguage.entries.forEach { lang ->
                        val selected = settingsState.selectedLanguage == lang
                        Button(
                            onClick = { settingsState.selectedLanguage = lang },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(lang.title)
                        }
                    }
                }
            }
        }

        // App Theme Color selection
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = strings.primaryColor,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppThemeColor.entries.forEach { themeColor ->
                        val selected = settingsState.selectedColor == themeColor
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { settingsState.selectedColor = themeColor }
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(themeColor.color)
                                    .then(
                                        if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                                        else Modifier
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = themeColor.title.substringBefore(" "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
