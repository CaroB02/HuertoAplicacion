package com.example.huertoaplicaciones.data.dao

import androidx.room.*
import com.example.huertoaplicaciones.data.entity.Sector
import kotlinx.coroutines.flow.Flow

@Dao
interface SectorDao {
    @Insert suspend fun insertar(sector: Sector): Long
    @Update suspend fun actualizar(sector: Sector)

    @Query("SELECT * FROM sectores ORDER BY nombre")
    fun obtenerTodos(): Flow<List<Sector>>

    @Query("SELECT * FROM sectores WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Sector?
}