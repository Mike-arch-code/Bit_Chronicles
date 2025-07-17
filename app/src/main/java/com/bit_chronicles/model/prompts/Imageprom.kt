package com.bit_chronicles.model.prompts

import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.bit_chronicles.model.api.ApiService
import com.bit_chronicles.viewmodel.UiState
import com.bit_chronicles.viewmodel.map.ImageFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Imageprom {

    companion object {

        fun generarPromptNarrativoPixelArt(textoEscena: String): String {
            return """
                Representa la siguiente escena como una imagen en estilo pixel art en una cuadrícula de 32x32 caracteres:
                "$textoEscena"
        
                Instrucciones:
                - Usa una composición simbólica y minimalista: no dibujes detalles, sino formas generales, siluetas y elementos clave.
                - Usa exactamente tres niveles de color, representados con los caracteres:
                  - 0 = fondo (por ejemplo, blanco o cielo)
                  - 1 = detalles secundarios o sombras (por ejemplo, terreno, objetos pequeños, efectos)
                  - 2 = elementos principales (por ejemplo, figuras, estructuras, explosiones, magia)
        
                Restricciones:
                - La imagen debe tener exactamente 32 líneas, con 32 caracteres en cada línea.
                - Solo usa los caracteres `0`, `1`, y `2`.
                - No añadas ningún texto, explicación ni encabezado adicional.
        
                Consejo:
                - Usa el color 2 para destacar lo importante de la escena (como personajes, rayos, estructuras grandes).
                - Usa el color 1 para añadir profundidad, sombras, formas suaves o contexto.
                - Mantén el fondo (0) limpio para reforzar el contraste.
        
                Formato de salida (ejemplo):
                00000000000000000000000000000000
                00000000111000000000000000000000
                00000001221000000000000000000000
                ...
                (total 32 líneas)
            """.trimIndent()
        }









        fun generateAndDisplayImageFromText(
            activity: FragmentActivity,
            apiService: ApiService,
            inputText: String,
            containerId: Int,
            onError: (Exception) -> Unit = {}
        )
        {
            val prompt = generarPromptNarrativoPixelArt(inputText)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d("Imageprom", "Enviando prompt a la IA...")
                    apiService.sendPrompt(prompt)
                    Log.d("Imageprom", "Esperando respuesta de la IA...")

                    val loggingJob = launch {
                        var seconds = 0
                        while (true) {
                            delay(5000)
                            seconds = seconds + 5
                            Log.d("Imageprom", "Esperando respuesta de la IA... (${seconds}s)")
                        }
                    }

                    val result = apiService.uiState.first {
                        it is UiState.Success || it is UiState.Error
                    }

                    // Cancelamos el log de espera al recibir respuesta
                    loggingJob.cancel()

                    when (result) {
                        is UiState.Success -> {
                            val imageData = result.response


                            Log.d("Imageprom", "Respuesta IA:\n$imageData")

                            withContext(Dispatchers.Main) {
                                val imageFragment = ImageFragment.newInstance(imageData)

                                activity.supportFragmentManager.beginTransaction()
                                    .replace(containerId, imageFragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                        }

                        is UiState.Error -> {
                            withContext(Dispatchers.Main) {
                                onError(Exception(result.message))
                            }
                        }

                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e("Imageprom", "Error al generar imagen IA", e)
                    withContext(Dispatchers.Main) {
                        onError(e)
                    }
                }
            }
        }
    }
}
