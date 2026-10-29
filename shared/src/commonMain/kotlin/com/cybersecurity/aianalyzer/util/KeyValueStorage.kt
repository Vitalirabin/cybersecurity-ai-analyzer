package com.cybersecurity.aianalyzer.util

expect class KeyValueStorage() {
    fun getString(key: String, defaultValue: String): String
    fun setString(key: String, value: String)
}
