package com.lmiguel.iahumanizada.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "almas")
data class Alma(
    @PrimaryKey val id: String,
    val nombre: String,
    val descripcion: String,
    val parametros: Map<String, Float>,
    val vocabulario: List<String>,
    val microgestos: List<String>,
    val ejemplos: List<String>,
    // true para almas creadas/duplicadas por el usuario desde la app (a
    // diferencia de las 6 originales, que vienen precargadas de fábrica).
    val esPersonalizada: Boolean = false
)

data class Afinacion(
    val tono: Float = 0.5f,
    val ritmo: Float = 0.5f,
    val humor: Float = 0.5f,
    val microgestos: Float = 0.5f,
    val profundidad: Float = 0.5f,
    val presencia: Float = 0.5f
)

enum class EstadoTipo {
    NEUTRO, CALIDO, TECNICO, JUGUETON, SILENCIOSO, REFLEXIVO
}

data class Estado(
    val tipo: EstadoTipo = EstadoTipo.NEUTRO,
    val modificadores: Map<String, Float> = emptyMap()
)