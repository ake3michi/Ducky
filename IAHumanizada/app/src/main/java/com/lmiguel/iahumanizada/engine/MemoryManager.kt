package com.lmiguel.iahumanizada.engine

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lmiguel.iahumanizada.data.model.Learning
import com.lmiguel.iahumanizada.data.model.LearningCategory
import com.lmiguel.iahumanizada.data.model.Memory
import com.lmiguel.iahumanizada.data.repository.MemoryDao
import kotlinx.coroutines.flow.first

// DataStore para el Soul de cada arquetipo
val Context.soulDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "soul_store")

class MemoryManager(
    private val dao: MemoryDao,
    private val context: Context
) {

    // === MEMORIA DECLARATIVA ===

    suspend fun store(key: String, content: String) {
        val existing = dao.getMemory(key)
        if (existing != null) {
            dao.updateMemory(key, content, existing.hits + 1)
        } else {
            dao.insertMemory(Memory(key = key, content = content))
        }
    }

    suspend fun getRecentContext(limit: Int = 5): String {
        val memories = dao.getRecentMemories(limit)
        if (memories.isEmpty()) return ""
        return memories.joinToString("\n") { "- ${it.content}" }
    }

    // === MEMORIA DE APRENDIZAJE ===

    suspend fun learn(
        key: String,
        content: String,
        category: LearningCategory,
        archetypeId: String,
        source: String = "observation"
    ) {
        val existing = dao.getLearning(key)
        if (existing != null) {
            dao.incrementReinforcement(key)
            // Verificar si hay que promover al Soul
            val updated = dao.getLearning(key)
            if ((updated?.reinforcementCount ?: 0) >= 5) {
                promoteToSoul(updated!!, archetypeId)
            }
        } else {
            dao.insertLearning(
                Learning(
                    key = key,
                    category = category,
                    content = content,
                    source = source,
                    archetypeId = archetypeId
                )
            )
        }
    }

    suspend fun reinforce(key: String, archetypeId: String) {
        dao.incrementReinforcement(key)
        val learning = dao.getLearning(key)
        if ((learning?.reinforcementCount ?: 0) >= 5) {
            learning?.let { promoteToSoul(it, archetypeId) }
        }
    }

    // Feedback negativo explícito del usuario ("esto no estuvo bien"):
    // no hay forma de "des-promover" del Soul una vez consolidado, pero al
    // menos evita que un patrón que no le gustó siga acumulando refuerzo.
    suspend fun debilitar(key: String) {
        dao.decrementReinforcement(key)
    }

    // === SOUL / SYSTEM PROMPT ===

    private suspend fun promoteToSoul(learning: Learning, archetypeId: String) {
        val key = stringPreferencesKey("soul_$archetypeId")
        context.soulDataStore.edit { prefs ->
            val current = prefs[key] ?: ""
            if (!current.contains(learning.content)) {
                prefs[key] = "$current\n- ${learning.content}"
            }
        }
    }

    suspend fun getSoul(archetypeId: String): String {
        val key = stringPreferencesKey("soul_$archetypeId")
        val prefs = context.soulDataStore.data.first()
        return prefs[key] ?: ""
    }

    suspend fun buildContext(archetypeId: String): String {
        val soul = getSoul(archetypeId)
        val recentMemories = getRecentContext(5)
        val topLearnings = dao.getTopReinforced(archetypeId, 5)
            .joinToString("\n") { "- ${it.content}" }

        return buildString {
            if (soul.isNotBlank()) {
                appendLine("[SOUL — Aprendizajes consolidados]")
                appendLine(soul)
                appendLine()
            }
            if (recentMemories.isNotBlank()) {
                appendLine("[CONTEXTO RECIENTE]")
                appendLine(recentMemories)
                appendLine()
            }
            if (topLearnings.isNotBlank()) {
                appendLine("[PATRONES CONFIRMADOS]")
                appendLine(topLearnings)
            }
        }.trim()
    }

    // === OLVIDO SELECTIVO ===

    suspend fun olvidarDebiles() {
        val hace21Dias = System.currentTimeMillis() - (21L * 24 * 60 * 60 * 1000)
        dao.deleteWeakMemories(hace21Dias)
        dao.deleteWeakLearnings(hace21Dias)
    }
}

