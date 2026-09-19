package com.lmiguel.iahumanizada.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ColorSchemeAltar = darkColorScheme(
    primary = ColorNeutro,
    background = Negro,
    surface = GrisOscuro,
    onPrimary = BlancoCalido,
    onBackground = BlancoCalido,
    onSurface = GrisTexto
)

@Composable
fun IAHumanizadaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColorSchemeAltar,
        typography = Typography,
        content = content
    )
}