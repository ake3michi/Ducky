package com.lmiguel.iahumanizada.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmiguel.iahumanizada.data.model.Afinacion
import com.lmiguel.iahumanizada.data.model.Alma
import com.lmiguel.iahumanizada.engine.PersonalityEngine
import com.lmiguel.iahumanizada.data.repository.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

// Modelo de mensaje
data class Mensaje(
    val contenido: String,
    val esUsuario: Boolean,
    val estadoTipo: String = "NEUTRO",
    val id: String = java.util.UUID.randomUUID().toString(),
    // Solo relevante para mensajes de la IA: identifican qué aprendizaje
    // reforzar/debilitar si el usuario da feedback (👍/👎) sobre este mensaje.
    val feedbackKey: String? = null,
    val feedbackArchetypeId: String? = null,
    // "positivo" / "negativo" / null (sin feedback aún). Sirve para
    // deshabilitar los botones una vez que el usuario ya opinó.
    val feedback: String? = null
)

// Estado de la UI
data class ChatUiState(
    val mensajes: List<Mensaje> = emptyList(),
    val cargando: Boolean = false,
    val estadoActual: String = "NEUTRO",
    val almaActual: String = "Ninguna",
    val almaActualId: String? = null,
    val almas: List<Alma> = emptyList(),
    val error: String? = null,
    // Antes, si fallaba el envío (ej. Ollama caído a medias), el usuario
    // tenía que re-escribir todo el mensaje a mano. Ahora se guarda para
    // poder reintentar con un botón.
    val ultimoMensajeFallido: String? = null
)

class ChatViewModel(
    private val motor: PersonalityEngine,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private val historial = mutableListOf<Mensaje>()

    init {
        // Si el motor ya tiene alma activa, la reflejamos en la UI.
        // Si no (primera vez que se crea el ViewModel), seleccionamos "neutra"
        // por defecto. Al vivir en el ViewModel esto solo pasa una vez, no en
        // cada recreación de la Activity (rotar pantalla, etc.).
        val yaSeleccionada = motor.obtenerAlmaActual()
        if (yaSeleccionada != null) {
            _uiState.value = _uiState.value.copy(
                almaActual = yaSeleccionada.nombre,
                almaActualId = yaSeleccionada.id
            )
        } else {
            viewModelScope.launch {
                val alma = motor.seleccionarAlma("neutra")
                _uiState.value = _uiState.value.copy(
                    almaActual = alma?.nombre ?: "Ninguna",
                    almaActualId = alma?.id
                )
            }
        }

        // Poda memorias/aprendizajes débiles una vez por sesión de la app.
        viewModelScope.launch {
            motor.limpiarMemoriaDebil()
        }

        // Cargar la lista de almas disponibles (para el selector del Altar)
        cargarAlmas()
    }

    fun cargarAlmas() {
        viewModelScope.launch {
            val lista = motor.listarAlmas()
            _uiState.update { it.copy(almas = lista) }
        }
    }

    fun seleccionarAlma(id: String) {
        viewModelScope.launch {
            val alma = motor.seleccionarAlma(id)
            _uiState.value = _uiState.value.copy(
                almaActual = alma?.nombre ?: "Ninguna",
                almaActualId = alma?.id
            )
        }
    }

    /** Crea o actualiza un alma (mismo id = edición; id nuevo = alma nueva). */
    fun guardarAlma(alma: Alma) {
        viewModelScope.launch {
            motor.guardarAlma(alma)
            cargarAlmas()
        }
    }

    /** Duplica un alma existente como punto de partida para personalizarla. */
    fun duplicarAlma(original: Alma, nuevoId: String, nuevoNombre: String) {
        viewModelScope.launch {
            motor.duplicarAlma(original, nuevoId, nuevoNombre)
            cargarAlmas()
        }
    }

    /** Solo borra almas personalizadas; las 6 originales no se pueden eliminar. */
    fun eliminarAlma(alma: Alma) {
        viewModelScope.launch {
            motor.eliminarAlma(alma)
            cargarAlmas()
        }
    }

    fun actualizarAfinacion(afinacion: Afinacion) {
        motor.actualizarAfinacion(afinacion)
    }

    fun enviarMensaje(textoUsuario: String, esReintento: Boolean = false) {
        if (textoUsuario.isBlank()) return

        // En un reintento, el mensaje del usuario ya está en el historial
        // (de su primer intento fallido) — no lo duplicamos.
        if (!esReintento) {
            val msgUsuario = Mensaje(textoUsuario, esUsuario = true)
            historial.add(msgUsuario)
            _uiState.update { it.copy(mensajes = historial.toList()) }
        }

        viewModelScope.launch {
            // 2. Estado cargando
            _uiState.update { it.copy(cargando = true, error = null) }

            try {
                val contexto = historial.takeLast(5)
                    .joinToString("\n") {
                        if (it.esUsuario) "Usuario: ${it.contenido}"
                        else "IA: ${it.contenido}"
                    }

                val resultado = motor.procesarMensaje(textoUsuario, contexto)

                // Logs de depuración
                android.util.Log.d("IAHumanizada", "Estado detectado: ${resultado.estado.tipo}")
                android.util.Log.d("IAHumanizada", "Generando respuesta...")

                // 3. Obtener respuesta
                val respuestaTexto = resultado.respuestaSilenciosa
                    ?: apiService.enviarMensaje(resultado.prompt)

                // 4. Crear mensaje de la IA
                val msgIA = Mensaje(
                    contenido = respuestaTexto,
                    esUsuario = false,
                    estadoTipo = resultado.estado.tipo.name,
                    feedbackKey = resultado.feedbackKey,
                    feedbackArchetypeId = resultado.almaId
                )

                // 5. ACTUALIZACIÓN FINAL DEL ESTADO — esto va antes de tocar
                // memoria a propósito: la respuesta ya llegó bien, así que el
                // usuario debe verla aunque algo falle guardándola.
                historial.add(msgIA)
                _uiState.update { estadoActual ->
                    estadoActual.copy(
                        mensajes = historial.toList(), // Crea copia nueva para Compose
                        estadoActual = resultado.estado.tipo.name,
                        cargando = false,
                        ultimoMensajeFallido = null
                    )
                }

                // 6. Guardar el intercambio en memoria (best-effort: si falla,
                // no debe tirarse una respuesta que el usuario ya recibió).
                try {
                    motor.registrarIntercambio(textoUsuario, respuestaTexto)
                } catch (e: Exception) {
                    android.util.Log.e("IAHumanizada", "No se pudo guardar el intercambio en memoria: ${e.message}")
                }

            } catch (e: Exception) {
                // Se guarda el mensaje para poder reintentar con un botón,
                // sin que el usuario tenga que volver a escribirlo entero.
                _uiState.update {
                    it.copy(
                        error = "Error: ${e.message}",
                        cargando = false,
                        ultimoMensajeFallido = textoUsuario
                    )
                }
            }
        }
    }

    /** Reintenta el último mensaje que falló, sin duplicar su burbuja en el chat. */
    fun reintentarUltimoMensaje() {
        val texto = _uiState.value.ultimoMensajeFallido ?: return
        enviarMensaje(texto, esReintento = true)
    }

    // Feedback explícito del usuario (👍/👎) sobre una respuesta de la IA.
    fun darFeedback(mensajeId: String, positivo: Boolean) {
        val index = historial.indexOfFirst { it.id == mensajeId }
        if (index == -1) return
        val mensaje = historial[index]
        val key = mensaje.feedbackKey ?: return
        val archetypeId = mensaje.feedbackArchetypeId ?: return

        // Actualización optimista en la UI: se ve al toque, sin esperar a la DB.
        historial[index] = mensaje.copy(feedback = if (positivo) "positivo" else "negativo")
        _uiState.update { it.copy(mensajes = historial.toList()) }

        viewModelScope.launch {
            try {
                motor.registrarFeedback(archetypeId, key, positivo)
            } catch (e: Exception) {
                android.util.Log.e("IAHumanizada", "No se pudo guardar el feedback: ${e.message}")
            }
        }
    }

    fun limpiarChat() {
        historial.clear()
        _uiState.value = ChatUiState(almaActual = _uiState.value.almaActual)
    }
}
