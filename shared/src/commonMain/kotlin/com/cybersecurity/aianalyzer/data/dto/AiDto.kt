package com.cybersecurity.aianalyzer.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.2
)

@Serializable
data class ChatCompletionResponse(
    val choices: List<Choice>? = null
)

@Serializable
data class Choice(
    val message: ChatMessage? = null
)

@Serializable
data class VulnerabilityDto(
    val description: String,
    val risk: String
)
