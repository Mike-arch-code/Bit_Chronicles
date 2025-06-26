
package com.bit_chronicles.viewmodel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bit_chronicles.R
import com.bit_chronicles.model.firebase.RealTime

class CampaignActivity : AppCompatActivity() {

    private lateinit var campaignContainer: LinearLayout
    private val db = RealTime()
    private val userId = "mike" // Ajusta con tu lógica de usuario real

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camps_screen)

        campaignContainer = findViewById(R.id.campaignContainer)
        val btnCrearCampaña = findViewById<Button>(R.id.btnCrearCampaña)
        val btnVerCampanias = findViewById<Button>(R.id.btnVerCampanias)

        btnCrearCampaña.setOnClickListener {
            val intent = Intent(this, activity_create_campaign::class.java)
            startActivity(intent)
        }
        btnVerCampanias.setOnClickListener {
            val intent = Intent(this, CampaignListActivity::class.java)
            startActivity(intent)
        }


    }

}

