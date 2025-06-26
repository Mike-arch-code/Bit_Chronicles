package com.bit_chronicles.viewmodel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.bit_chronicles.R

class CampaignActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camps_screen)

        val btnCrearCampaña = findViewById<Button>(R.id.btnCrearCampaña)

        btnCrearCampaña.setOnClickListener {
            val intent = Intent(this, activity_create_campaign::class.java)
            startActivity(intent)
        }
    }
}