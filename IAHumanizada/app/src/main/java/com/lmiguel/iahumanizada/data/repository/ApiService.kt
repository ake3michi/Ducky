package com.lmiguel.iahumanizada.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// Antes NetworkError/QuotaExceeded/SafetyFiltered no tenían mensaje propio,
// así que al usuario le aparecía literalmente "Error: null" en pantalla.
sealed class ApiError(override val message: String) : Exception(message) {
    object NetworkError : ApiError(
        "No se pudo conectar con el servidor de IA. Revisa que Ollama esté " +
            "corriendo y que el móvil tenga acceso a esa red (Tailscale/Wi-Fi)."
    )
    object ModeloNoEncontrado : ApiError(
        "El modelo o el endpoint configurado no existen en el servidor Ollama."
    )
    object PromptInvalido : ApiError(
        "El servidor rechazó el mensaje (solicitud inválida)."
    )
    object RespuestaVacia : ApiError(
        "La IA no devolvió texto. Puede ser un problema temporal del modelo."
    )
    data class UnknownError(val code: Int, val detalle: String) :
        ApiError("Error inesperado del servidor (código $code): $detalle")
}

class ApiService(
    private val modelo: String = "llama3.2:3b",
    /**
     * Endpoints tried en orden hasta que uno responda. Vienen de BuildConfig,
     * que a su vez los lee de local.properties (ver app/build.gradle.kts),
     * así cada dev puede apuntar a su propio servidor Ollama sin tocar código.
     */
    private val endpoints: List<String> = listOf(
        com.lmiguel.iahumanizada.BuildConfig.OLLAMA_URL_PRIMARY,
        com.lmiguel.iahumanizada.BuildConfig.OLLAMA_URL_FALLBACK
    ),
    private val connectTimeoutMs: Int = 5000,
    private val readTimeoutMs: Int = 10000
) {

    suspend fun enviarMensaje(prompt: String): String {
        return withContext(Dispatchers.IO) {
            if (prompt.length > 30000) throw Exception("Prompt demasiado largo")

            // Pre-build body once
            val body = JSONObject().apply {
                put("model", modelo)
                put("prompt", prompt)
                put("stream", false)
            }.toString()

            var lastException: Exception? = null

            for (endpoint in endpoints) {
                // Antes la conexión nunca se cerraba (ni en éxito ni en
                // error), lo que con el tiempo puede agotar el pool de
                // conexiones del sistema. Ahora se cierra siempre con
                // try/finally, pase lo que pase dentro del try.
                var connection: HttpURLConnection? = null
                try {
                    android.util.Log.d("IAHumanizada", "🔵 Intentando endpoint: $endpoint con modelo: $modelo")

                    val url = URL(endpoint)
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                        connectTimeout = connectTimeoutMs
                        readTimeout = readTimeoutMs
                    }

                    android.util.Log.d("IAHumanizada", "🔵 Conexión establecida a $endpoint, enviando payload...")

                    OutputStreamWriter(connection.outputStream).use {
                        it.write(body)
                        it.flush()
                    }

                    val responseCode = connection.responseCode
                    android.util.Log.d("IAHumanizada", "🔵 API Response Code: $responseCode (endpoint: $endpoint)")

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        android.util.Log.d("IAHumanizada", "✅ Respuesta OK recibida desde $endpoint, parseando JSON...")

                        val json = JSONObject(response)

                        if (!json.has("response")) {
                            android.util.Log.e("IAHumanizada", "❌ Sin campo 'response' en respuesta de $endpoint: $response")
                            throw ApiError.RespuestaVacia
                        }

                        val generatedText = json.getString("response")

                        if (generatedText.isNotBlank()) {
                            android.util.Log.d("IAHumanizada", "✅ Respuesta extraída exitosamente (${generatedText.length} caracteres) desde $endpoint")
                            return@withContext generatedText
                        } else {
                            android.util.Log.e("IAHumanizada", "❌ Respuesta vacía en el campo 'response' desde $endpoint: $response")
                            throw ApiError.RespuestaVacia
                        }
                    } else {
                        val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Sin detalle"
                        android.util.Log.e("IAHumanizada", "❌ Error API ($responseCode) en $endpoint: $errorText")
                        android.util.Log.e("IAHumanizada", "❌ Tipo de error: ${when(responseCode) {
                            400 -> "Solicitud inválida (prompt problemático?)"
                            404 -> "Modelo no encontrado o endpoint incorrecto"
                            500, 502, 503 -> "Error del servidor Ollama"
                            else -> "Error desconocido"
                        }}")

                        when (responseCode) {
                            404 -> throw ApiError.ModeloNoEncontrado
                            400 -> throw ApiError.PromptInvalido
                            else -> throw ApiError.UnknownError(responseCode, errorText)
                        }
                    }

                } catch (e: ApiError) {
                    // If the API returned a semantic error (quota, safety, unknown), stop trying other endpoints
                    throw e
                } catch (e: Exception) {
                    // Connection-level errors: log and try next endpoint
                    android.util.Log.e("IAHumanizada", "❌ Error en ApiService al conectar a $endpoint: ${e.javaClass.simpleName} - ${e.message}")
                    e.printStackTrace()
                    lastException = e
                    // try next endpoint
                } finally {
                    connection?.disconnect()
                }
            }

            android.util.Log.e("IAHumanizada", "❌ Todos los endpoints configurados fallaron. Último error: ${lastException?.message}")
            throw ApiError.NetworkError
        }
    }
}
