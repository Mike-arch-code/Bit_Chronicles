package com.bit_chronicles.viewmodel.campaign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bit_chronicles.R
import com.bit_chronicles.model.firebase.RealTime
import android.widget.ImageButton

class CampaignListActivity : AppCompatActivity() {

    private lateinit var campaignListContainer: LinearLayout
    private val db = RealTime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign_list)

        campaignListContainer = findViewById(R.id.campaignListContainer)
        cargarCampañas()
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun cargarCampañas() {
        val userId = "Mike"
        db.getCampaignList(userId,
            onResult = { lista ->
                for (camp in lista) {
                    val card = LayoutInflater.from(this)
                        .inflate(R.layout.card_campaign, campaignListContainer, false)
                    val textView = card.findViewById<TextView>(R.id.tvCampName)
                    textView.text = camp
                    card.setOnClickListener {
                        val intent = Intent(this, CampaignInfoActivity::class.java)
                        intent.putExtra("campaignName", camp)
                        startActivity(intent)
                    }

                    campaignListContainer.addView(card)
                }
            },
            onError = {
                Log.e("CampaignListActivity", "Error: ${it.message}")
            }
        )
    }
}