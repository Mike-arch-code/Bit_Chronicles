package com.bit_chronicles.viewmodel.character

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.bit_chronicles.R

class CharacterActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_persons_screen)

        val btnCrearpersonaje = findViewById<Button>(R.id.btnCrearpersonaje)
        val btnVerpersonajes = findViewById<Button>(R.id.btnVerPersonajes)

        btnCrearpersonaje.setOnClickListener {
            val intent = Intent(this, CreateCharacterActivity::class.java)
            startActivity(intent)
        }
        btnVerpersonajes.setOnClickListener {
            val intent = Intent(this, CharacterListActivity::class.java)
            startActivity(intent)
        }
    }
}