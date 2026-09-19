package com.lmiguel.iahumanizada.engine

import com.lmiguel.iahumanizada.data.model.Estado
import com.lmiguel.iahumanizada.data.model.EstadoTipo
import java.text.Normalizer

class StateDetector {

    /**
     * Antes se comparaba con `in texto` (substring), lo que daba falsos
     * positivos: por ejemplo "api" aparece dentro de "capital", o "dia"
     * dentro de "diagnostico". Ahora usamos límites de palabra reales para
     * las claves de una sola palabra, y seguimos con substring solo para
     * frases de varias palabras (donde sí tiene sentido).
     */
    private fun contieneAlguna(texto: String, claves: List<String>): Boolean {
        return claves.any { clave ->
            if (clave.contains(" ")) {
                clave in texto
            } else {
                Regex("\\b${Regex.escape(clave)}\\b").containsMatchIn(texto)
            }
        }
    }

    /** Quita tildes para que "reflexion"/"reflexión", "que"/"qué" etc. den igual. */
    private fun normalizar(texto: String): String {
        val descompuesto = Normalizer.normalize(texto, Normalizer.Form.NFD)
        return descompuesto.replace(Regex("\\p{Mn}+"), "")
    }

    fun detectar(mensaje: String, pausaSegundos: Long = 0): Estado {
        val texto = normalizar(mensaje.lowercase())
        val puntuacion = mutableMapOf(
            EstadoTipo.NEUTRO to 0,
            EstadoTipo.CALIDO to 0,
            EstadoTipo.TECNICO to 0,
            EstadoTipo.JUGUETON to 0,
            EstadoTipo.SILENCIOSO to 0,
            EstadoTipo.REFLEXIVO to 0
        )

        // Sensor 1: pausas (Solo si el usuario explícitamente parece querer distancia)
        when {
            pausaSegundos > 300 && (texto.length < 10) -> puntuacion[EstadoTipo.SILENCIOSO] = puntuacion[EstadoTipo.SILENCIOSO]!! + 3
            pausaSegundos > 60 -> {
                puntuacion[EstadoTipo.CALIDO] = puntuacion[EstadoTipo.CALIDO]!! + 1
                puntuacion[EstadoTipo.REFLEXIVO] = puntuacion[EstadoTipo.REFLEXIVO]!! + 1
            }
        }

        // Sensor 2: longitud (Evitar que mensajes cortos sean siempre silenciosos)
        val palabras = texto.split(" ").filter { it.isNotBlank() }
        when {
            palabras.isEmpty() -> puntuacion[EstadoTipo.SILENCIOSO] = puntuacion[EstadoTipo.SILENCIOSO]!! + 2
            palabras.size > 20 -> puntuacion[EstadoTipo.REFLEXIVO] = puntuacion[EstadoTipo.REFLEXIVO]!! + 2
        }

        // Sensor 3: palabras clave emocionales
        if (contieneAlguna(texto, listOf("hola", "buen", "dia", "tarde", "noche", "gracias", "que tal", "como vas")))
            puntuacion[EstadoTipo.CALIDO] = puntuacion[EstadoTipo.CALIDO]!! + 2

        if (contieneAlguna(texto, listOf("uff", "cansado", "agobiado", "triste", "mal", "ayuda", "necesito")))
            puntuacion[EstadoTipo.CALIDO] = puntuacion[EstadoTipo.CALIDO]!! + 5

        // Sensor 4: palabras técnicas
        if (contieneAlguna(texto, listOf("codigo", "error", "bug", "funcion", "clase", "kotlin", "api", "gradle", "xml")))
            puntuacion[EstadoTipo.TECNICO] = puntuacion[EstadoTipo.TECNICO]!! + 5

        // Sensor 5: humor
        if (contieneAlguna(texto, listOf("jajaja", "jeje", "xd", "😂", "🤣")))
            puntuacion[EstadoTipo.JUGUETON] = puntuacion[EstadoTipo.JUGUETON]!! + 4

        // Sensor 6: reflexión
        if (contieneAlguna(texto, listOf("por que", "sentido", "reflexion", "pienso", "creo que")))
            puntuacion[EstadoTipo.REFLEXIVO] = puntuacion[EstadoTipo.REFLEXIVO]!! + 4

        // Sensor 7: puntuación especial
        if (texto.contains("..."))
            puntuacion[EstadoTipo.REFLEXIVO] = puntuacion[EstadoTipo.REFLEXIVO]!! + 2
        if (texto.count { it == '!' } > 1)
            puntuacion[EstadoTipo.JUGUETON] = puntuacion[EstadoTipo.JUGUETON]!! + 2

        // Caso especial silencio
        if ("solo quiero estar" in texto || "no quiero hablar" in texto)
            return Estado(EstadoTipo.SILENCIOSO)

        // Seleccionar estado dominante
        val max = puntuacion.maxByOrNull { it.value }!!
        val estadoFinal = if (max.value == 0) EstadoTipo.NEUTRO else max.key

        return Estado(tipo = estadoFinal)
    }
}
