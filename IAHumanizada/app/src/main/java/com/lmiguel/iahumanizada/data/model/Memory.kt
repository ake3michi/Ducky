package com.lmiguel.iahumanizada.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class Memory(
    @PrimaryKey val key: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    var hits: Int = 1
)

enum class LearningCategory {
    LEARNING, ERROR, PREFERENCE
}

@Entity(tableName = "learnings")
data class Learning(
    @PrimaryKey val key: String,
    val category: LearningCategory,
    val content: String,
    val source: String,
    val archetypeId: String,
    var reinforcementCount: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

