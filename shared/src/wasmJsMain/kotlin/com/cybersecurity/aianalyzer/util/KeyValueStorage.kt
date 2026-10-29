package com.cybersecurity.aianalyzer.util

actual class KeyValueStorage {
    private val map = mutableMapOf<String, String>()

    actual fun getString(key: String, defaultValue: String): String {
        return map[key] ?: defaultValue
    }

    actual fun setString(key: String, value: String) {
        map[key] = value
    }
}
