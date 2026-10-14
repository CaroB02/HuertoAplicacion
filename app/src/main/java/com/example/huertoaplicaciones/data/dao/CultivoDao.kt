package com.example.huertoaplicaciones.data.dao

import androidx.room.*
import com.example.huertoaplicaciones.data.entity.Cultivo
import kotlinx.coroutines.flow.Flow

@Dao
interface CultivoDao {
    @Insert suspend fun insertar(cultivo: Cultivo): Long
    @Update suspend fun actualizar(cultivo: Cultivo)

    @Query("SELECT * FROM cultivos ORDER BY nombre")
    fun obtenerTodos(): Flow<List<Cultivo>>

    @Query("SELECT * FROM cultivos WHERE sectorId = :sectorId")
    fun obtenerPorSector(sectorId: Long): Flow<List<Cultivo>>

    @Query("SELECT * FROM cultivos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Cultivo?
}