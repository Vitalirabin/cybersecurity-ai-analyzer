package com.cybersecurity.aianalyzer.ui

interface Strings {
    val analyzerTab: String
    val settingsTab: String
    val insertLogsHint: String
    val pasteLogsPlaceholder: String
    val checkVulnerabilities: String
    val settingsTitle: String
    val interfaceLanguage: String
    val primaryColor: String
}

object EnglishStrings : Strings {
    override val analyzerTab = "Analyzer"
    override val settingsTab = "Settings"
    override val insertLogsHint = "Insert logs or code here"
    override val pasteLogsPlaceholder = "Paste system logs..."
    override val checkVulnerabilities = "Check Vulnerabilities"
    override val settingsTitle = "Settings"
    override val interfaceLanguage = "Interface Language"
    override val primaryColor = "App Primary Color"
}

object RussianStrings : Strings {
    override val analyzerTab = "Анализ"
    override val settingsTab = "Настройки"
    override val insertLogsHint = "Вставьте логи или код здесь"
    override val pasteLogsPlaceholder = "Вставьте системные логи..."
    override val checkVulnerabilities = "Проверить уязвимости"
    override val settingsTitle = "Настройки"
    override val interfaceLanguage = "Язык интерфейса"
    override val primaryColor = "Основной цвет приложения"
}

fun getStrings(language: AppLanguage): Strings {
    return when (language) {
        AppLanguage.EN -> EnglishStrings
        AppLanguage.RU -> RussianStrings
    }
}
