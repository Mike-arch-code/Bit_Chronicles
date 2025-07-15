package com.bit_chronicles.viewmodel.character

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bit_chronicles.R
import com.bit_chronicles.model.firebase.RealTime
import android.widget.ImageButton

class CharacterinfoActivity : AppCompatActivity() {

    private val db = RealTime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_info)


        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val userId = "Mike"
        val characterName = intent.getStringExtra("characterName") ?: return


        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        db.getCharacterInfo(
            userId = userId,
            characterName = characterName,
            onResult = { result ->

                // Obtener los datos directamente sin 'metadata'
                val name = result["nombre"] ?: characterName
                val raza = result["raza"] ?: ""
                val clase = result["clase"] ?: ""
                val nivel = result["nivel"] ?: ""
                val alineamiento = result["alineamiento"] ?: ""

                val hp = result["hp"] ?: ""
                val ca = result["ca"] ?: ""
                val estadisticas = result["estadisticas"] ?: ""

                val habilidades = result["habilidades"] ?: ""
                val habilidadesEspeciales = result["habilidadesEspeciales"] ?: ""
                val ataques = result["ataques"] ?: ""

                val equipo = result["equipo"] ?: ""
                val oro = result["oro"] ?: ""
                val mochila = result["mochila"] ?: ""

                val rasgos = result["rasgos"] ?: ""
                val personalidad = result["personalidad"] ?: ""
                val motivacion = result["motivacion"] ?: ""

                // Mostrar en UI
                findViewById<TextView>(R.id.tvName).text = "Nombre: $name"
                findViewById<TextView>(R.id.tvRace).text = "Raza: $raza"
                findViewById<TextView>(R.id.tvClass).text = "Clase: $clase"
                findViewById<TextView>(R.id.tvLevel).text = "Nivel: $nivel"
                findViewById<TextView>(R.id.tvAlignment).text = "Alineamiento: $alineamiento"

                findViewById<TextView>(R.id.tvHp).text = "HP: $hp"
                findViewById<TextView>(R.id.tvCa).text = "CA: $ca"
                findViewById<TextView>(R.id.tvStats).text = "Estadísticas: $estadisticas"

                findViewById<TextView>(R.id.tvSkills).text = "Habilidades: $habilidades"
                findViewById<TextView>(R.id.tvSpecialSkills).text = "Habilidades Especiales: $habilidadesEspeciales"

                findViewById<TextView>(R.id.tvEquipment).text = equipo.toString()
                findViewById<TextView>(R.id.tvGold).text = "Oro: $oro"
                findViewById<TextView>(R.id.tvBackpack).text = mochila.toString()

                findViewById<TextView>(R.id.tvPersonality).text = personalidad.toString()
                findViewById<TextView>(R.id.tvMotivation).text = motivacion.toString()
            },
            onError = {
                Toast.makeText(this, "Error cargando personaje", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
