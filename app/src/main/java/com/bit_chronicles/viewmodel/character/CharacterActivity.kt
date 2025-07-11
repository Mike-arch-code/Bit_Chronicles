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

        btnCrearpersonaje.setOnClickListener {
            val intent = Intent(this, activity_create_character::class.java)
            startActivity(intent)
        }
    }
}