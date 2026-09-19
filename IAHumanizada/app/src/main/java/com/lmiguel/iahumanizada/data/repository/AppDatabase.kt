package com.lmiguel.iahumanizada.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lmiguel.iahumanizada.data.model.Alma
import com.lmiguel.iahumanizada.data.model.Learning
import com.lmiguel.iahumanizada.data.model.Memory

@Database(
    entities = [Memory::class, Learning::class, Alma::class],
    version = 2, // v2: se añadió la tabla "almas" (antes vivían solo en res/raw JSON)
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun memoryDao(): MemoryDao
    abstract fun almaDao(): AlmaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "iahumanizada_db"
                )
                    // Sin esto, cualquier cambio futuro al esquema (nuevo
                    // campo en Memory/Learning, etc.) hace que la app
                    // crashee al abrir en vez de manejarlo. Como es memoria
                    // "reconstruible" (no datos críticos del usuario que no
                    // se puedan perder), recrear la base es aceptable aquí.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
