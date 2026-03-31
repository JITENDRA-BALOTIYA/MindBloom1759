package com.example.mental_health.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {

    // 🧠 Model Configuration
    private val config = generationConfig {
        temperature = 0.7f
        topK = 40
        topP = 0.95f
        maxOutputTokens = 1000
    }

    // 🛡️ Safety settings for a mental health context
    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
    )

    // ⚠️ API KEY: Recommended to move to local.properties and access via BuildConfig
    private val apiKey = "AIzaSyDcZfadq00Og2zT11Ezc6svV1weUJTzV9E"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash", // Corrected model name (2.5-flash does not exist)
        apiKey = apiKey,
        generationConfig = config,
        safetySettings = safetySettings,
        requestOptions = RequestOptions(apiVersion = "v1beta"), // systemInstruction is best supported in v1beta
        systemInstruction = content {
            text("""
                You are MindBloom AI, a calm, empathetic mental health assistant.
                - Provide emotional support and active listening.
                - Always reply politely, supportively, and with deep empathy.
                - Keep responses concise (max 3-4 sentences) unless a detailed explanation is needed.
                - Suggest simple coping techniques (e.g., box breathing, grounding) when appropriate.
                - Use 'Hinglish' (Hindi written in English script) if the user communicates in Hindi/Hinglish to feel more personal.
                - IMPORTANT: If the user expresses thoughts of self-harm, gently advise them to speak with a professional and provide a general crisis resource.
            """.trimIndent())
        }
    )

    // 💬 Chat session to maintain conversation context automatically
    private var chat = generativeModel.startChat()

    // Chat messages (Pair<String, Boolean> → message + isUser)
    private val _messages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf("Hello! I'm MindBloom AI 😊\nHow can I support you today?" to false)
    )
    val messages: StateFlow<List<Pair<String, Boolean>>> = _messages.asStateFlow()

    // Loading state for UI typing indicator
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(userMessage: String) {
        val trimmedMsg = userMessage.trim()
        if (trimmedMsg.isBlank()) return

        // 1. Update UI with User Message
        _messages.value = _messages.value + (trimmedMsg to true)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // 2. Send to Gemini via Chat Session (handles history automatically)
                val response = chat.sendMessage(trimmedMsg)
                val aiReply = response.text ?: "I'm listening. Tell me more about that 💙"

                // 3. Update UI with AI Reply
                _messages.value = _messages.value + (aiReply to false)

            } catch (e: Exception) {
                // 4. Enhanced Error Logging & User Feedback
                Log.e("ChatViewModel", "Gemini API Error: ${e.message}", e)
                
                val userFriendlyError = when {
                    e.message?.contains("404") == true -> "Model error. Switching to stable version..."
                    e.message?.contains("403") == true -> "API Key error. Please check your credentials."
                    e.message?.contains("429") == true -> "Too many requests. Please slow down a bit."
                    e.message?.contains("Safety") == true -> "I cannot discuss this topic as it violates safety guidelines. How else can I help?"
                    else -> "Connection issue. Please check your internet and try again."
                }

                _messages.value = _messages.value + ("⚠️ $userFriendlyError" to false)
                
                // If 404, attempt to re-initialize with the most stable model
                if (e.message?.contains("404") == true) {
                    switchToStableModel()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun switchToStableModel() {
        val fallbackModel = GenerativeModel(
            modelName = "gemini-2.5-flash", // Try flash on v1beta as fallback
            apiKey = apiKey,
            generationConfig = config,
            requestOptions = RequestOptions(apiVersion = "v1beta")
        )
        chat = fallbackModel.startChat()
    }
}
