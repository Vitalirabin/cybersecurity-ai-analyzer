package com.cybersecurity.aianalyzer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cybersecurity.aianalyzer.model.AnalysisHistoryItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryScreen(
    onSelectItem: (AnalysisHistoryItem) -> Unit,
    strings: Strings,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = koinViewModel()
) {
    var selectedItem by remember { mutableStateOf<AnalysisHistoryItem?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.refreshHistory()
    }

    val historyItems = viewModel.historyItems

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.historyTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (historyItems.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearHistory() }) {
                    Text(strings.clearHistory)
                }
            }
        }

        if (historyItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.emptyHistory,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyItems, key = { it.id }) { item ->
                    val isSelected = selectedItem?.id == item.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedItem = item
                                onSelectItem(item)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = item.logsSnippet,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                            HorizontalDivider()
                            Text(
                                text = "${strings.vulnerabilitiesCount.substringBefore("%d")}${item.vulnerabilities.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Detail view right inside the card when selected
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Full Logs / Code:",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                OutlinedTextField(
                                    value = item.fullLogs,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Findings (${item.vulnerabilities.size}):",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                item.vulnerabilities.forEach { vuln ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    VulnerabilityCard(vuln)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
