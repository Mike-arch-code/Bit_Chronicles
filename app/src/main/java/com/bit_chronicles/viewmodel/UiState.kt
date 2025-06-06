package com.bit_chronicles.viewmodel

sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    data class Success(val response: String) : UiState()
    data class Error(val message: String) : UiState()
}