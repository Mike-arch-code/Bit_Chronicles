package com.bit_chronicles.model.firebase



import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class ActivityListCampaigns : AppCompatActivity() {

    private val db = RealTime()
    private val userId = "test_user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Aún no cargamos UI, solo la lógica de lectura
        loadCampaignsFromFirebase()
    }

    private fun loadCampaignsFromFirebase() {
        val path = "aventuras/$userId"

        db.readOnce(path,
            onData = { result ->
                Log.d("CampaignsData", "Resultado: $result")
                // Aquí después transformamos en objetos
            },
            onError = { e ->
                Log.e("CampaignsData", "Error leyendo campañas: ${e.message}")
            }
        )
    }
}
