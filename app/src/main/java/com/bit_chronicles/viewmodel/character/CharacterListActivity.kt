package com.bit_chronicles.viewmodel.character

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bit_chronicles.R
import com.bit_chronicles.model.firebase.RealTime
import com.bit_chronicles.viewmodel.campaign.CampaignInfoActivity

class CharacterListActivity : AppCompatActivity() {

    private lateinit var characterListContainer: LinearLayout
    private val db = RealTime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_list)

        characterListContainer = findViewById(R.id.characterListContainer)
        cargarPersonajes()
    }

    private fun cargarPersonajes() {
        val userId = "Mike"
        db.getCharacterList(userId,
            onResult = { lista ->
                for (camp in lista) {
                    val card = LayoutInflater.from(this)
                        .inflate(R.layout.card_character, characterListContainer, false)

                    val textView = card.findViewById<TextView>(R.id.tvCharacterName)
                    textView.text = camp

                    card.setOnClickListener {
                        val intent = Intent(this, CharacterinfoActivity::class.java)
                        intent.putExtra("characterName", camp)
                        startActivity(intent)
                    }

                    characterListContainer.addView(card)
                }
            },
            onError = {
                Log.e("CharacterListActivity", "Error al cargar personajes: ${it.message}")
            }
        )
    }
}
