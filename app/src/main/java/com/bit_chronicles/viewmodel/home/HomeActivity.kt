package com.bit_chronicles.viewmodel.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.bit_chronicles.R
import com.bit_chronicles.viewmodel.character.CharacterActivity
import com.bit_chronicles.viewmodel.campaign.CampaignActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val botoncampañas = findViewById<Button>(R.id.botoncampañas)
        val botonpjs = findViewById<Button>(R.id.botonpjs)

        botoncampañas.setOnClickListener {
            val intent = Intent(this, CampaignActivity::class.java)
            startActivity(intent)
        }

        botonpjs.setOnClickListener {
            val intent = Intent(this, CharacterActivity::class.java)
            startActivity(intent)
        }
    }
}