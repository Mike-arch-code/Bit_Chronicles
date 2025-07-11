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

        val userId = "Mike" // Puedes hacerlo dinámico si tienes auth
        val campaignName = intent.getStringExtra("campaignName") ?: return

        val tvInfo = findViewById<TextView>(R.id.tvCampaignInfo)

        db.getCampaignInfo(userId, campaignName,
            onResult = { data ->
                val builder = StringBuilder()
                builder.append("🌍 Campaña: $campaignName\n\n")
                builder.append("📜 Prompt:\n${data["prompt"]}\n\n")
                builder.append("📘 Metadata:\n")

                data.filterKeys { it != "prompt" }.forEach { (key, value) ->
                    builder.append("$key: ${value ?: "-"}\n")
                }

                tvInfo.text = builder.toString()
            },
            onError = {
                Log.e("CampaignInfoActivity", "Error: ${it.message}")
                tvInfo.text = "Error al cargar la información"
            }
        )
        val playButton = findViewById<Button>(R.id.btnPlayCampaign)
        playButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

    }
}