package com.lmiguel.iahumanizada.data.repository

import androidx.room.*
import com.lmiguel.iahumanizada.data.model.Alma

@Dao
interface AlmaDao {

    @Query("SELECT * FROM almas")
    suspend fun getTodas(): List<Alma>

    @Query("SELECT * FROM almas WHERE id = :id LIMIT 1")
    suspend fun getPorId(id: String): Alma?

    @Query("SELECT COUNT(*) FROM almas")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(alma: Alma)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(almas: List<Alma>)

    @Delete
    suspend fun eliminar(alma: Alma)
}
