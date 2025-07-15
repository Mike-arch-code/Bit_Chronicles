package com.bit_chronicles.model.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RealTime {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val rootRef: DatabaseReference = database.reference

    fun write(path: String, value: Any, onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        rootRef.child(path).setValue(value)
            .addOnSuccessListener {
                Log.d("RealTime", "Data written at $path")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("RealTime", "Error writing to $path: ${exception.message}")
                onError(exception)
            }
    }

    fun readOnce(path: String, onData: (Any?) -> Unit, onError: (Exception) -> Unit = {}) {
        rootRef.child(path).get()
            .addOnSuccessListener { snapshot ->
                onData(snapshot.value)
            }
            .addOnFailureListener { exception ->
                Log.e("RealTime", "Error reading $path: ${exception.message}")
                onError(exception)
            }
    }


    fun getCampaignList(userId: String, onResult: (List<String>) -> Unit, onError: (Exception) -> Unit = {}) {
        val path = "aventuras/$userId"

        rootRef.child(path).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val campaignNames = mutableListOf<String>()

                for (child in snapshot.children) {
                    val campaignName = child.key
                    campaignName?.let { campaignNames.add(it) }
                }

                onResult(campaignNames)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RealTime", "Error al leer campañas: ${error.message}")
                onError(Exception(error.message))
            }
        })
    }
    fun getCampaignInfo(
        userId: String,
        campaignName: String,
        onResult: (Map<String, String>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val path = "aventuras/$userId/$campaignName/historia"

        rootRef.child(path).get()
            .addOnSuccessListener { snapshot ->
                val historia = snapshot.value as? String ?: ""
                val result = mapOf(
                    "campaignName" to campaignName,
                    "historia" to historia
                )
                onResult(result)
            }
            .addOnFailureListener { exception ->
                Log.e("RealTime", "Error al obtener historia: ${exception.message}")
                onError(exception)
            }
    }

    fun getCharacterList(
        userId: String,
        onResult: (List<String>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val path = "personajes/$userId"

        rootRef.child(path).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val characterNames = mutableListOf<String>()

                for (child in snapshot.children) {
                    val characterName = child.key
                    characterName?.let { characterNames.add(it) }
                }

                onResult(characterNames)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al leer personajes para $userId: ${error.message}")
                onError(error.toException())
            }
        })
    }

    fun getCharacterInfo(
        userId: String,
        characterName: String,
        onResult: (Map<String, Any?>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val path = "personajes/$userId/$characterName"

        rootRef.child(path).get()
            .addOnSuccessListener { snapshot ->
                val result = mutableMapOf<String, Any?>()

                snapshot.children.forEach { child ->
                    result[child.key ?: ""] = child.value
                }

                onResult(result)
            }
            .addOnFailureListener { exception ->
                Log.e("RealTime", "Error al obtener info: ${exception.message}")
                onError(exception)
            }
    }
    fun read(
        path: String,
        onSuccess: (DataSnapshot) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        rootRef.child(path).get()
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onError)
    }

}