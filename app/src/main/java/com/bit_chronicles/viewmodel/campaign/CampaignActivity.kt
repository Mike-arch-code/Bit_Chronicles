package com.bit_chronicles.viewmodel.campaign

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.bit_chronicles.R

class CampaignActivity : AppCompatActivity() {

    private lateinit var campaignContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camps_screen)

        campaignContainer = findViewById(R.id.campaignContainer)
        val btnCrearCampaña = findViewById<Button>(R.id.btnCrearCampaña)
        val btnVerCampanias = findViewById<Button>(R.id.btnVerCampanias)

        btnCrearCampaña.setOnClickListener {
            val intent = Intent(this, CreateCampaignActivity::class.java)
            startActivity(intent)
        }
        btnVerCampanias.setOnClickListener {
            val intent = Intent(this, CampaignListActivity::class.java)
            startActivity(intent)
        }


    }

}