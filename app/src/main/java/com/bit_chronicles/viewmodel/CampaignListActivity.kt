package com.bit_chronicles.viewmodel



import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bit_chronicles.R
import com.bit_chronicles.model.firebase.RealTime

class CampaignListActivity : AppCompatActivity() {

    private lateinit var campaignListContainer: LinearLayout
    private val db = RealTime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign_list)

        campaignListContainer = findViewById(R.id.campaignListContainer)
        cargarCampañas()
    }

    private fun cargarCampañas() {
        val userId = "Mike" // Reemplazar por ID dinámico si usas Auth
        db.getCampaignList(userId,
            onResult = { lista ->
                for (camp in lista) {
                    val card = LayoutInflater.from(this)
                        .inflate(R.layout.card_campaign, campaignListContainer, false)
                    val textView = card.findViewById<TextView>(R.id.tvCampName)
                    textView.text = camp
                    campaignListContainer.addView(card)
                }
            },
            onError = {
                Log.e("CampaignListActivity", "Error: ${it.message}")
            }
        )
    }
}
