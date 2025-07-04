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

    fun listen(path: String, onChange: (Any?) -> Unit) {
        rootRef.child(path).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onChange(snapshot.value)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RealTime", "Listener cancelled at $path: ${error.message}")
            }
        })
    }

    fun delete(path: String, onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        rootRef.child(path).removeValue()
            .addOnSuccessListener {
                Log.d("RealTime", "Data deleted at $path")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("RealTime", "Error deleting $path: ${exception.message}")
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
        onResult: (Map<String, Any?>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val path = "aventuras/$userId/$campaignName"

        rootRef.child(path).get()
            .addOnSuccessListener { snapshot ->
                val prompt = snapshot.child("historia/prompt").value as? String ?: ""
                val metadata = snapshot.child("metadata").value as? Map<String, Any?> ?: emptyMap()

                val result = metadata.toMutableMap()
                result["prompt"] = prompt

                onResult(result)
            }
            .addOnFailureListener { exception ->
                Log.e("RealTime", "Error al obtener info campaña: ${exception.message}")
                onError(exception)
            }
    }


}