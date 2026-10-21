package com.cybersecurity.aianalyzer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cybersecurity.aianalyzer.data.remote.AiApiClient
import com.cybersecurity.aianalyzer.data.repository.LogAnalyzerRepositoryImpl
import com.cybersecurity.aianalyzer.domain.usecase.AnalyzeLogsUseCase
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LogAnalyzerScreen(
    modifier: Modifier = Modifier,
    viewModel: LogAnalyzerViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = viewModel.logs,
            onValueChange = { viewModel.logs = it },
            modifier = Modifier.fillMaxWidth().height(200.dp),
            label = { Text("Insert logs or code here") },
            placeholder = { Text("Paste system logs...") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { scope.launch { viewModel.analyzeLogs() } },
            enabled = !viewModel.isLoading && viewModel.logs.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Check Vulnerabilities")
            }
        }

        viewModel.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(viewModel.results) { vulnerability ->
                VulnerabilityCard(vulnerability)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview
@Composable
fun LogAnalyzerScreenPreview() {
    val viewModel = remember {
        LogAnalyzerViewModel(
            analyzeLogsUseCase = AnalyzeLogsUseCase(
                repository = LogAnalyzerRepositoryImpl(
                    aiApiClient = AiApiClient()
                )
            )
        )
    }
    MaterialTheme {
        Surface {
            LogAnalyzerScreen(viewModel = viewModel)
        }
    }
}
