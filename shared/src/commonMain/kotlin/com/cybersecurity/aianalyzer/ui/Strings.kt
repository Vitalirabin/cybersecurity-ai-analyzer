package com.cybersecurity.aianalyzer.ui

interface Strings {
    val analyzerTab: String
    val historyTab: String
    val settingsTab: String
    val insertLogsHint: String
    val pasteLogsPlaceholder: String
    val checkVulnerabilities: String
    val settingsTitle: String
    val interfaceLanguage: String
    val primaryColor: String
    val historyTitle: String
    val historyLimit: String
    val emptyHistory: String
    val clearHistory: String
    val vulnerabilitiesCount: String
}

object EnglishStrings : Strings {
    override val analyzerTab = "Analyzer"
    override val historyTab = "History"
    override val settingsTab = "Settings"
    override val insertLogsHint = "Insert logs or code here"
    override val pasteLogsPlaceholder = "Paste system logs..."
    override val checkVulnerabilities = "Check Vulnerabilities"
    override val settingsTitle = "Settings"
    override val interfaceLanguage = "Interface Language"
    override val primaryColor = "App Primary Color"
    override val historyTitle = "Analysis History"
    override val historyLimit = "Max History Items"
    override val emptyHistory = "No history records yet"
    override val clearHistory = "Clear History"
    override val vulnerabilitiesCount = "Vulnerabilities found: %d"
}

object RussianStrings : Strings {
    override val analyzerTab = "Анализ"
    override val historyTab = "История"
    override val settingsTab = "Настройки"
    override val insertLogsHint = "Вставьте логи или код здесь"
    override val pasteLogsPlaceholder = "Вставьте системные логи..."
    override val checkVulnerabilities = "Проверить уязвимости"
    override val settingsTitle = "Настройки"
    override val interfaceLanguage = "Язык интерфейса"
    override val primaryColor = "Основной цвет приложения"
    override val historyTitle = "История запросов"
    override val historyLimit = "Макс. размер истории"
    override val emptyHistory = "История запросов пуста"
    override val clearHistory = "Очистить историю"
    override val vulnerabilitiesCount = "Найдено уязвимостей: %d"
}

fun getStrings(language: AppLanguage): Strings {
    return when (language) {
        AppLanguage.EN -> EnglishStrings
        AppLanguage.RU -> RussianStrings
    }
}
