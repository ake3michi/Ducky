package com.lmiguel.iahumanizada.ui.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmiguel.iahumanizada.ui.theme.BlancoCalido
import com.lmiguel.iahumanizada.ui.theme.ColorCalido
import com.lmiguel.iahumanizada.ui.theme.ColorJugueton
import com.lmiguel.iahumanizada.ui.theme.ColorNeutro
import com.lmiguel.iahumanizada.ui.theme.ColorReflexivo
import com.lmiguel.iahumanizada.ui.theme.ColorSilencioso
import com.lmiguel.iahumanizada.ui.theme.ColorTecnico
import com.lmiguel.iahumanizada.ui.theme.GrisMedio
import com.lmiguel.iahumanizada.ui.theme.GrisOscuro
import com.lmiguel.iahumanizada.ui.theme.GrisTexto
import com.lmiguel.iahumanizada.ui.theme.Negro
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onAbrirAltar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var textoInput by remember { mutableStateOf("") }

    // Scroll automático al último mensaje cuando cambia el tamaño de la lista
    LaunchedEffect(uiState.mensajes.size) {
        if (uiState.mensajes.isNotEmpty()) {
            listState.animateScrollToItem(uiState.mensajes.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Negro)
    ) {
        // --- CABECERA ---
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PuntoQueRespira(
                    estadoColor = obtenerColorEstado(uiState.estadoActual),
                    cargando = uiState.cargando
                )

                // Estado actual animado
                AnimatedContent(
                    targetState = uiState.estadoActual,
                    transitionSpec = {
                        fadeIn(tween(600)) togetherWith fadeOut(tween(600))
                    },
                    label = "estado"
                ) { estado ->
                    Text(
                        text = obtenerEtiquetaEstado(estado),
                        color = obtenerColorEstado(estado).copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        letterSpacing = 3.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // --- INDICADOR DE ERROR ---
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = Color.Red.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (uiState.ultimoMensajeFallido != null) {
                        androidx.compose.material3.TextButton(
                            onClick = { viewModel.reintentarUltimoMensaje() },
                            enabled = !uiState.cargando
                        ) {
                            Text("↻ Reintentar", color = ColorNeutro, fontSize = 13.sp)
                        }
                    }
                }
            }

            IconButton(
                onClick = onAbrirAltar,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Text(text = "✦", color = GrisTexto, fontSize = 20.sp)
            }
        }

        // --- LISTA DE MENSAJES (ESTO FALTABA) ---
        androidx.compose.foundation.lazy.LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f) // Ocupa el espacio disponible
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
        ) {
            items(uiState.mensajes.size) { index ->
                val mensaje = uiState.mensajes[index]
                BurbujaMensaje(
                    mensaje = mensaje,
                    onFeedback = { positivo -> viewModel.darFeedback(mensaje.id, positivo) }
                )
            }
        }

        // --- CAJA DE ENTRADA ---
        CajaEntrada(
            texto = textoInput,
            onTextoChange = { textoInput = it },
            cargando = uiState.cargando,
            onEnviar = {
                if (textoInput.isNotBlank()) {
                    val textoAEnviar = textoInput
                    textoInput = "" // Limpiar input inmediatamente
                    viewModel.enviarMensaje(textoAEnviar)
                }
            }
        )
    }
}

@Composable
fun PuntoQueRespira(estadoColor: Color, cargando: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "respira")

    // Animación de escala — respira
    val escala by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (cargando) 1.4f else 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (cargando) 500 else 1800,
                easing = EaseInOutCubic
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala"
    )

    // Animación de color — transición suave entre estados
    val colorAnimado by animateColorAsState(
        targetValue = estadoColor,
        animationSpec = tween(
            durationMillis = 1200,
            easing = EaseInOutCubic
        ),
        label = "color"
    )

    // Animación de opacidad del halo exterior
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        // Halo exterior difuso
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(escala * 1.3f)
                .background(
                    color = colorAnimado.copy(alpha = haloAlpha),
                    shape = CircleShape
                )
        )

        // Punto principal
        Box(
            modifier = Modifier
                .size(70.dp)
                .scale(escala)
                .shadow(24.dp, CircleShape)
                .background(colorAnimado, CircleShape)
        )
    }
}

@Composable
fun BurbujaMensaje(mensaje: Mensaje, onFeedback: (Boolean) -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (mensaje.esUsuario) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (mensaje.esUsuario)
                Arrangement.End else Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        color = if (mensaje.esUsuario) GrisMedio else GrisOscuro,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (mensaje.esUsuario) 16.dp else 4.dp,
                            bottomEnd = if (mensaje.esUsuario) 4.dp else 16.dp
                        )
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = mensaje.contenido,
                    color = if (mensaje.esUsuario) GrisTexto else BlancoCalido,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }

        // Feedback solo tiene sentido en mensajes de la IA que traen su
        // propia key de aprendizaje (feedbackKey). Una vez que el usuario ya
        // opinó, se muestra cuál eligió en vez de dejar que vuelva a tocar.
        if (!mensaje.esUsuario && mensaje.feedbackKey != null) {
            Row(
                modifier = Modifier.padding(top = 2.dp, start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when (mensaje.feedback) {
                    null -> {
                        BotonFeedback(emoji = "👍") { onFeedback(true) }
                        BotonFeedback(emoji = "👎") { onFeedback(false) }
                    }
                    "positivo" -> Text("👍", fontSize = 13.sp, color = GrisTexto.copy(alpha = 0.6f))
                    "negativo" -> Text("👎", fontSize = 13.sp, color = GrisTexto.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun BotonFeedback(emoji: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(28.dp)
    ) {
        Text(text = emoji, fontSize = 13.sp)
    }
}

@Composable
fun CajaEntrada(
    texto: String,
    onTextoChange: (String) -> Unit,
    cargando: Boolean,
    onEnviar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GrisOscuro)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = texto,
            onValueChange = onTextoChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("Habla...", color = ColorSilencioso)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GrisMedio,
                unfocusedContainerColor = GrisMedio,
                focusedTextColor = BlancoCalido,
                unfocusedTextColor = BlancoCalido,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            enabled = !cargando,
            maxLines = 4
        )

        // Botón enviar
        IconButton(
            onClick = onEnviar,
            enabled = texto.isNotBlank() && !cargando,
            modifier = Modifier
                .size(48.dp)
                .background(ColorNeutro, CircleShape)
        ) {
            Text(
                text = "→",
                color = Negro,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun obtenerColorEstado(estado: String): Color {
    return when (estado) {
        "CALIDO" -> ColorCalido
        "TECNICO" -> ColorTecnico
        "JUGUETON" -> ColorJugueton
        "SILENCIOSO" -> ColorSilencioso
        "REFLEXIVO" -> ColorReflexivo
        else -> ColorNeutro
    }
}
fun obtenerEtiquetaEstado(estado: String): String {
    return when (estado) {
        "CALIDO" -> "· cálido ·"
        "TECNICO" -> "· técnico ·"
        "JUGUETON" -> "· juguetón ·"
        "SILENCIOSO" -> "· silencio ·"
        "REFLEXIVO" -> "· reflexivo ·"
        else -> "· presente ·"
    }
}
