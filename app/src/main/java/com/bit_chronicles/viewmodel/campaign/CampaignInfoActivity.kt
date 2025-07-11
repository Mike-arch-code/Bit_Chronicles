package com.bit_chronicles.viewmodel.campaign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bit_chronicles.R
import com.bit_chronicles.model.firebase.RealTime
import com.bit_chronicles.viewmodel.map.MapActivity

class CampaignInfoActivity : AppCompatActivity() {

    private val db = RealTime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign_info)

        val userId = "Mike" // Reemplazar por autenticación real si aplica
        val campaignName = intent.getStringExtra("campaignName") ?: return

        val tvInfo = findViewById<TextView>(R.id.tvCampaignInfo)

        db.getCampaignInfo(
            userId,
            campaignName,
            onResult = { data ->
                val builder = StringBuilder()
                builder.append("🌍 Campaña: $campaignName\n\n")

                val historia = data["historia"] as? String ?: "No se encontró la historia."
                builder.append("📜 Historia:\n$historia")

                tvInfo.text = builder.toString()
            },
            onError = {
                Log.e("CampaignInfoActivity", "Error: ${it.message}")
                tvInfo.text = "❌ Error al cargar la información de la campaña"
            }
        )

        val playButton = findViewById<Button>(R.id.btnPlayCampaign)
        playButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }
}
