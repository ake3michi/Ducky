
package com.lmiguel.iahumanizada.data.repository

import androidx.room.*
import com.lmiguel.iahumanizada.data.model.Learning
import com.lmiguel.iahumanizada.data.model.Memory

@Dao
interface MemoryDao {

    // === MEMORY ===
    @Query("SELECT * FROM memories WHERE key = :key LIMIT 1")
    suspend fun getMemory(key: String): Memory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: Memory)

    @Query("UPDATE memories SET content = :content, hits = :hits WHERE key = :key")
    suspend fun updateMemory(key: String, content: String, hits: Int): Int

    @Query("SELECT * FROM memories ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMemories(limit: Int = 10): List<Memory>

    @Query("DELETE FROM memories WHERE hits < 2 AND timestamp < :before")
    suspend fun deleteWeakMemories(before: Long): Int

    // === LEARNING ===
    @Query("SELECT * FROM learnings WHERE key = :key LIMIT 1")
    suspend fun getLearning(key: String): Learning?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearning(learning: Learning)

    @Query("UPDATE learnings SET reinforcementCount = reinforcementCount + 1 WHERE key = :key")
    suspend fun incrementReinforcement(key: String): Int

    // Para feedback negativo explícito del usuario: baja el refuerzo sin
    // dejarlo nunca en negativo (MAX(0, ...) funciona como función escalar
    // en SQLite con 2+ argumentos, no solo como agregado).
    @Query("UPDATE learnings SET reinforcementCount = MAX(0, reinforcementCount - 1) WHERE key = :key")
    suspend fun decrementReinforcement(key: String): Int

    // Nota: antes esto no filtraba por alma, así que los "patrones
    // confirmados" de un alma se mezclaban con los de otra en el prompt.
    @Query("SELECT * FROM learnings WHERE archetypeId = :archetypeId ORDER BY reinforcementCount DESC LIMIT :limit")
    suspend fun getTopReinforced(archetypeId: String, limit: Int = 5): List<Learning>

    @Query("SELECT * FROM learnings WHERE archetypeId = :archetypeId ORDER BY reinforcementCount DESC")
    suspend fun getLearningsByArchetype(archetypeId: String): List<Learning>

    @Query("DELETE FROM learnings WHERE reinforcementCount < 2 AND timestamp < :before")
    suspend fun deleteWeakLearnings(before: Long): Int
}