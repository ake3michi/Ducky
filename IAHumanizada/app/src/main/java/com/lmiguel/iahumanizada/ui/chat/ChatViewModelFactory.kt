package com.lmiguel.iahumanizada.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lmiguel.iahumanizada.data.repository.ApiService
import com.lmiguel.iahumanizada.engine.PersonalityEngine

/**
 * Factory para que ChatViewModel se cree a través de ViewModelProvider
 * (vía `by viewModels { ... }` en la Activity) en vez de instanciarse a mano
 * en onCreate. Así el ViewModel sobrevive a cambios de configuración
 * (rotar pantalla, modo oscuro, etc.) sin perder el historial de chat.
 */
class ChatViewModelFactory(
    private val motor: PersonalityEngine,
    private val apiService: ApiService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(motor, apiService) as T
        }
        throw IllegalArgumentException("Clase de ViewModel desconocida: ${modelClass.name}")
    }
}
