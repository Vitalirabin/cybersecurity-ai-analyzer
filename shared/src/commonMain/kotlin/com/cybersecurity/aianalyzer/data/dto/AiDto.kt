package com.cybersecurity.aianalyzer.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: String
)

@Serializable
data class ChatCompletionRequest(
    @SerialName("model")
    val model: String = "nvidia/nemotron-3-ultra-550b-a55b:free",
    @SerialName("messages")
    val messages: List<ChatMessage>,
    @SerialName("temperature")
    val temperature: Double = 0.2
)

@Serializable
data class ChatCompletionResponse(
    @SerialName("choices")
    val choices: List<Choice>? = null
)

@Serializable
data class Choice(
    @SerialName("message")
    val message: ChatMessage? = null
)

@Serializable
data class VulnerabilityDto(
    @SerialName("description")
    val description: String,
    @SerialName("risk")
    val risk: String
)
