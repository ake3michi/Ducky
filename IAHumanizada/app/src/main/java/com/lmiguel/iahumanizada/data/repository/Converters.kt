package com.lmiguel.iahumanizada.data.repository

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lmiguel.iahumanizada.data.model.LearningCategory

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLearningCategory(value: LearningCategory): String {
        return value.name
    }

    @TypeConverter
    fun toLearningCategory(value: String): LearningCategory {
        return LearningCategory.valueOf(value)
    }

    // Para persistir Alma.vocabulario / microgestos / ejemplos en Room
    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // Para persistir Alma.parametros en Room
    @TypeConverter
    fun fromFloatMap(value: Map<String, Float>): String = gson.toJson(value)

    @TypeConverter
    fun toFloatMap(value: String): Map<String, Float> {
        val type = object : TypeToken<Map<String, Float>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }
}
