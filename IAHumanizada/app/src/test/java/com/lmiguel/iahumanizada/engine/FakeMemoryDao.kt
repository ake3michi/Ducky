package com.lmiguel.iahumanizada.engine

import com.lmiguel.iahumanizada.data.model.Learning
import com.lmiguel.iahumanizada.data.model.Memory
import com.lmiguel.iahumanizada.data.repository.MemoryDao

/**
 * Implementación en memoria de MemoryDao, solo para tests. Se comporta como
 * las queries reales de Room pero sin tocar disco ni necesitar un emulador.
 */
class FakeMemoryDao : MemoryDao {

    val memories = mutableMapOf<String, Memory>()
    val learnings = mutableMapOf<String, Learning>()

    override suspend fun getMemory(key: String): Memory? = memories[key]

    override suspend fun insertMemory(memory: Memory) {
        memories[memory.key] = memory
    }

    override suspend fun updateMemory(key: String, content: String, hits: Int): Int {
        val existing = memories[key] ?: return 0
        memories[key] = existing.copy(content = content, hits = hits)
        return 1
    }

    override suspend fun getRecentMemories(limit: Int): List<Memory> =
        memories.values.sortedByDescending { it.timestamp }.take(limit)

    override suspend fun deleteWeakMemories(before: Long): Int {
        val aBorrar = memories.values.filter { it.hits < 2 && it.timestamp < before }
        aBorrar.forEach { memories.remove(it.key) }
        return aBorrar.size
    }

    override suspend fun getLearning(key: String): Learning? = learnings[key]

    override suspend fun insertLearning(learning: Learning) {
        learnings[learning.key] = learning
    }

    override suspend fun incrementReinforcement(key: String): Int {
        val existing = learnings[key] ?: return 0
        learnings[key] = existing.copy(reinforcementCount = existing.reinforcementCount + 1)
        return 1
    }

    override suspend fun decrementReinforcement(key: String): Int {
        val existing = learnings[key] ?: return 0
        learnings[key] = existing.copy(reinforcementCount = maxOf(0, existing.reinforcementCount - 1))
        return 1
    }

    override suspend fun getTopReinforced(archetypeId: String, limit: Int): List<Learning> =
        learnings.values.filter { it.archetypeId == archetypeId }
            .sortedByDescending { it.reinforcementCount }
            .take(limit)

    override suspend fun getLearningsByArchetype(archetypeId: String): List<Learning> =
        learnings.values.filter { it.archetypeId == archetypeId }
            .sortedByDescending { it.reinforcementCount }

    override suspend fun deleteWeakLearnings(before: Long): Int {
        val aBorrar = learnings.values.filter { it.reinforcementCount < 2 && it.timestamp < before }
        aBorrar.forEach { learnings.remove(it.key) }
        return aBorrar.size
    }
}
