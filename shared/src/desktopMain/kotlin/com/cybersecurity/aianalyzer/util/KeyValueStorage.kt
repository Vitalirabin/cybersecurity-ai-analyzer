package com.cybersecurity.aianalyzer.util

import java.util.prefs.Preferences

actual class KeyValueStorage {
    private val prefs = Preferences.userNodeForPackage(KeyValueStorage::class.java)

    actual fun getString(key: String, defaultValue: String): String {
        return prefs.get(key, defaultValue)
    }

    actual fun setString(key: String, value: String) {
        prefs.put(key, value)
    }
}
