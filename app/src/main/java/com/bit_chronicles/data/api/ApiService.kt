package com.bit_chronicles.data.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit_chronicles.BuildConfig
import com.bit_chronicles.ui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class ApiService : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()




    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )




    fun sendPrompt(prompt: String) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        text(prompt)
                    }
                )
                val output = response.text ?: "No hubo respuesta"
                _uiState.value = UiState.Success(output)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}

