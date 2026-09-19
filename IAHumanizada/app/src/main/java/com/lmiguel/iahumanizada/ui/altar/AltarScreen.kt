package com.lmiguel.iahumanizada.ui.altar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmiguel.iahumanizada.data.model.Afinacion
import com.lmiguel.iahumanizada.data.model.Alma
import com.lmiguel.iahumanizada.ui.chat.ChatViewModel
import com.lmiguel.iahumanizada.ui.chat.obtenerColorEstado
import com.lmiguel.iahumanizada.ui.theme.*

@Composable
fun AltarScreen(
    viewModel: ChatViewModel,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var tono by remember { mutableFloatStateOf(0.5f) }
    var ritmo by remember { mutableFloatStateOf(0.5f) }
    var humor by remember { mutableFloatStateOf(0.5f) }
    var microgestos by remember { mutableFloatStateOf(0.5f) }
    var profundidad by remember { mutableFloatStateOf(0.5f) }
    var presencia by remember { mutableFloatStateOf(0.5f) }

    val colorAlma by animateColorAsState(
        targetValue = obtenerColorEstado(uiState.estadoActual),
        animationSpec = tween(800),
        label = "colorAlma"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Negro)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "✦ El Altar ✦",
            color = colorAlma,
            fontSize = 22.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 4.sp
        )
        Text(
            text = "Afina tu presencia",
            color = GrisTexto,
            fontSize = 13.sp,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Alma actual
        Text(
            text = "Alma: ${uiState.almaActual}",
            color = colorAlma.copy(alpha = 0.7f),
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- SELECTOR DE ALMAS ---
        // Antes no existía ninguna forma de cambiar de alma desde la app.
        var editando by remember { mutableStateOf<Alma?>(null) }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(uiState.almas) { alma ->
                ChipAlma(
                    alma = alma,
                    seleccionada = alma.id == uiState.almaActualId,
                    color = colorAlma,
                    onSeleccionar = { viewModel.seleccionarAlma(alma.id) },
                    onEditar = { editando = alma }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = {
            val actual = uiState.almas.find { it.id == uiState.almaActualId } ?: return@TextButton
            val nuevoId = "custom_${System.currentTimeMillis()}"
            viewModel.duplicarAlma(actual, nuevoId, "${actual.nombre} (copia)")
        }) {
            Text("+ Duplicar alma actual", color = GrisTexto, fontSize = 12.sp)
        }

        editando?.let { alma ->
            EditorAlmaDialog(
                alma = alma,
                onDismiss = { editando = null },
                onGuardar = { actualizada ->
                    viewModel.guardarAlma(actualizada)
                    editando = null
                },
                onEliminar = if (alma.esPersonalizada) {
                    {
                        viewModel.eliminarAlma(alma)
                        editando = null
                    }
                } else null
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sliders de afinación
        SliderAfinacion(
            label = "Tono",
            descripcion = "frío ←→ cálido",
            valor = tono,
            color = colorAlma,
            onChange = { tono = it }
        )
        SliderAfinacion(
            label = "Ritmo",
            descripcion = "lento ←→ rápido",
            valor = ritmo,
            color = colorAlma,
            onChange = { ritmo = it }
        )
        SliderAfinacion(
            label = "Humor",
            descripcion = "serio ←→ juguetón",
            valor = humor,
            color = colorAlma,
            onChange = { humor = it }
        )
        SliderAfinacion(
            label = "Microgestos",
            descripcion = "silencio ←→ expresivo",
            valor = microgestos,
            color = colorAlma,
            onChange = { microgestos = it }
        )
        SliderAfinacion(
            label = "Profundidad",
            descripcion = "superficial ←→ profundo",
            valor = profundidad,
            color = colorAlma,
            onChange = { profundidad = it }
        )
        SliderAfinacion(
            label = "Presencia",
            descripcion = "distante ←→ presente",
            valor = presencia,
            color = colorAlma,
            onChange = { presencia = it }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón aplicar
        Button(
            onClick = {
                viewModel.actualizarAfinacion(
                    Afinacion(
                        tono = tono,
                        ritmo = ritmo,
                        humor = humor,
                        microgestos = microgestos,
                        profundidad = profundidad,
                        presencia = presencia
                    )
                )
                onVolver()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorAlma.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Aplicar afinación",
                color = colorAlma,
                fontSize = 15.sp,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón volver
        TextButton(onClick = onVolver) {
            Text(
                text = "← Volver",
                color = GrisTexto,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ChipAlma(
    alma: Alma,
    seleccionada: Boolean,
    color: Color,
    onSeleccionar: () -> Unit,
    onEditar: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (seleccionada) color.copy(alpha = 0.25f) else GrisMedio,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onSeleccionar) {
                Text(
                    text = alma.nombre,
                    color = if (seleccionada) color else GrisTexto,
                    fontSize = 12.sp,
                    fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal
                )
            }
            IconButton(onClick = onEditar, modifier = Modifier.size(28.dp)) {
                Text("✎", color = GrisTexto, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Editor mínimo de un alma: nombre, descripción y vocabulario (separado por
 * comas). Suficiente para personalizar el tono de una personalidad sin
 * tocar código ni recompilar. Si el alma es personalizada, también permite
 * borrarla; las 6 originales no se pueden eliminar.
 */
@Composable
fun EditorAlmaDialog(
    alma: Alma,
    onDismiss: () -> Unit,
    onGuardar: (Alma) -> Unit,
    onEliminar: (() -> Unit)?
) {
    var nombre by remember(alma.id) { mutableStateOf(alma.nombre) }
    var descripcion by remember(alma.id) { mutableStateOf(alma.descripcion) }
    var vocabularioTexto by remember(alma.id) { mutableStateOf(alma.vocabulario.joinToString(", ")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GrisOscuro,
        title = { Text("Editar alma", color = BlancoCalido) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = vocabularioTexto,
                    onValueChange = { vocabularioTexto = it },
                    label = { Text("Vocabulario (separado por comas)") },
                    minLines = 2
                )
                if (!alma.esPersonalizada) {
                    Text(
                        "Esta es una de las 6 almas originales: se puede editar, " +
                            "pero no borrar. Usa \"Duplicar\" para crear tu propia versión.",
                        color = GrisTexto,
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onGuardar(
                    alma.copy(
                        nombre = nombre,
                        descripcion = descripcion,
                        vocabulario = vocabularioTexto.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                    )
                )
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row {
                if (onEliminar != null) {
                    TextButton(onClick = onEliminar) {
                        Text("Eliminar", color = Color(0xFFE57373))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = GrisTexto)
                }
            }
        }
    )
}

@Composable
fun SliderAfinacion(
    label: String,
    descripcion: String,
    valor: Float,
    color: Color,
    onChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = BlancoCalido,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = String.format("%.2f", valor),
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = descripcion,
            color = GrisTexto,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = valor,
            onValueChange = onChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color.copy(alpha = 0.7f),
                inactiveTrackColor = GrisMedio
            )
        )
    }
}