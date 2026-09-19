package com.lmiguel.iahumanizada.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lmiguel.iahumanizada.data.model.Alma
import com.lmiguel.iahumanizada.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Antes las 6 almas vivían fijas en res/raw/*.json, así que la única forma
 * de cambiar una personalidad o crear una nueva era editar código y
 * recompilar. Ahora Room es la fuente de verdad: los JSON solo se usan como
 * "semilla" la primera vez que se abre la app, y a partir de ahí todo se
 * lee/escribe en la base de datos, lo que permite editar o crear almas
 * nuevas desde la propia app sin tocar el código.
 */
class AlmaRepository(
    context: Context,
    private val almaDao: AlmaDao
) {
    private val context = context.applicationContext
    private val gson = Gson()

    private val almasSemillaJson = mapOf(
        "alma_poetica" to R.raw.alma_poetica,
        "alma_tecnica" to R.raw.alma_tecnica,
        "alma_intima" to R.raw.alma_intima,
        "alma_ironica" to R.raw.alma_ironica,
        "alma_sensual" to R.raw.alma_sensual,
        "alma_neutra" to R.raw.alma_neutra
    )

    private suspend fun asegurarSemilla() = withContext(Dispatchers.IO) {
        if (almaDao.contar() > 0) return@withContext

        val type = object : TypeToken<Alma>() {}.type
        val almasIniciales = almasSemillaJson.values.mapNotNull { resourceId ->
            try {
                val json = context.resources.openRawResource(resourceId)
                    .bufferedReader()
                    .use { it.readText() }
                gson.fromJson<Alma>(json, type)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        almaDao.insertarTodas(almasIniciales)
    }

    suspend fun cargarTodas(): List<Alma> = withContext(Dispatchers.IO) {
        asegurarSemilla()
        almaDao.getTodas()
    }

    suspend fun obtenerAlmaPorId(id: String): Alma? = withContext(Dispatchers.IO) {
        asegurarSemilla()
        almaDao.getPorId(id)
    }

    /** Crea una alma nueva o actualiza una existente (mismo id = sobrescribe). */
    suspend fun guardarAlma(alma: Alma) = withContext(Dispatchers.IO) {
        almaDao.insertar(alma)
    }

    /** Duplica un alma existente con un nuevo id/nombre, como punto de partida para personalizarla. */
    suspend fun duplicarAlma(original: Alma, nuevoId: String, nuevoNombre: String): Alma =
        withContext(Dispatchers.IO) {
            val copia = original.copy(id = nuevoId, nombre = nuevoNombre, esPersonalizada = true)
            almaDao.insertar(copia)
            copia
        }

    /** Solo se permite borrar almas creadas por el usuario, nunca las 6 originales. */
    suspend fun eliminarAlma(alma: Alma): Boolean = withContext(Dispatchers.IO) {
        if (!alma.esPersonalizada) return@withContext false
        almaDao.eliminar(alma)
        true
    }
}
