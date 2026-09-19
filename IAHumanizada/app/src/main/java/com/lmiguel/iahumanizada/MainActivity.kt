package com.lmiguel.iahumanizada


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.lmiguel.iahumanizada.data.repository.AlmaRepository
import com.lmiguel.iahumanizada.data.repository.ApiService
import com.lmiguel.iahumanizada.data.repository.AppDatabase
import com.lmiguel.iahumanizada.engine.MemoryManager
import com.lmiguel.iahumanizada.engine.PersonalityEngine
import com.lmiguel.iahumanizada.ui.chat.ChatScreen
import com.lmiguel.iahumanizada.ui.chat.ChatViewModelFactory
import com.lmiguel.iahumanizada.ui.theme.IAHumanizadaTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.lmiguel.iahumanizada.ui.altar.AltarScreen

class MainActivity : ComponentActivity() {

    // El ViewModel se pide a través de ViewModelProvider (con nuestra Factory,
    // porque ChatViewModel necesita dependencias que no tienen constructor
    // vacío). Así sobrevive a cambios de configuración: si giras el teléfono,
    // onCreate se vuelve a ejecutar pero `by viewModels` devuelve la MISMA
    // instancia en vez de crear una nueva, y el chat no se pierde.
    private val viewModel by viewModels<com.lmiguel.iahumanizada.ui.chat.ChatViewModel> {
        // Base de datos
        val database = AppDatabase.getInstance(this)
        val memoryDao = database.memoryDao()
        val almaDao = database.almaDao()

        // Repositorios y servicios
        val almaRepository = AlmaRepository(this, almaDao)
        val memoryManager = MemoryManager(memoryDao, this)

        // Nota: ApiService habla con Ollama local (no con Gemini), por eso ya
        // no se le pasa ninguna API key. BuildConfig.GEMINI_API_KEY queda
        // como legado por si se vuelve a usar Gemini más adelante.
        val apiService = ApiService()

        val motor = PersonalityEngine(almaRepository, memoryManager)

        ChatViewModelFactory(motor, apiService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            IAHumanizadaTheme {
                var pantallaActual by rememberSaveable { mutableStateOf("chat") }

                when (pantallaActual) {
                    "chat" -> ChatScreen(
                        viewModel = viewModel,
                        onAbrirAltar = { pantallaActual = "altar" }
                    )
                    "altar" -> AltarScreen(
                        viewModel = viewModel,
                        onVolver = { pantallaActual = "chat" }
                    )
                }
            }
        }
    }
}