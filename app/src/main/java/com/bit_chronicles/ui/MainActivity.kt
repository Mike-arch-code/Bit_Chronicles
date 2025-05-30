package com.bit_chronicles.ui;

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bit_chronicles.data.api.IaConexion
import com.bit_chronicles.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val iaConexion: IaConexion by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val promptEditText = findViewById<EditText>(R.id.promptEditText)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val responseTextView = findViewById<TextView>(R.id.responseTextView)

        sendButton.setOnClickListener {
            val prompt = promptEditText.text.toString()
            if (prompt.isNotBlank()) {
                iaConexion.sendPrompt(prompt)
            }
        }

        lifecycleScope.launch {
            iaConexion.uiState.collectLatest { state ->
                when (state) {
                    is UiState.Initial -> responseTextView.text = ""
                    is UiState.Loading -> responseTextView.text = "Cargando..."
                    is UiState.Success -> responseTextView.text = state.response
                    is UiState.Error -> responseTextView.text = "Error: ${state.message}"
                }
            }
        }
    }
}
