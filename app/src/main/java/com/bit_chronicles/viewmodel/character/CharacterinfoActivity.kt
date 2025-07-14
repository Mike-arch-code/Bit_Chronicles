package com.bit_chronicles.viewmodel.character

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bit_chronicles.R
import com.bit_chronicles.model.firebase.RealTime
import com.bit_chronicles.viewmodel.map.MapActivity

class CharacterinfoActivity : AppCompatActivity(){

    private val db = RealTime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign_info)

        val userId = "Mike"
        val characterName = intent.getStringExtra("characterName") ?: return

        val tvInfo = findViewById<TextView>(R.id.tvCampaignInfo)
        tvInfo.movementMethod = android.text.method.ScrollingMovementMethod.getInstance()

        db.getCharacterInfo(
            userId,
            characterName,
            onResult = { data ->
                val historia = data["historia"] ?: "No se encontró la historia."
                val contenido = """
                🌍 Campaña: $characterName
                
                📜 Historia:
                $historia
            """.trimIndent()
                tvInfo.text = contenido
                Log.d("INFO_OK", contenido)
            },
            onError = {
                Log.e("CampaignInfoActivity", "Error: ${it.message}")
                tvInfo.text = "❌ Error al cargar la información de la campaña"
            }
        )
    }

}