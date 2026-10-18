package com.example.huertoaplicaciones.data.dao

import androidx.room.*
import com.example.huertoaplicaciones.data.entity.Cultivo
import kotlinx.coroutines.flow.Flow

// Consultas de la tabla "cultivos" (CRUD completo).
@Dao
interface CultivoDao {
    @Insert suspend fun insertar(cultivo: Cultivo): Long
    @Update suspend fun actualizar(cultivo: Cultivo)
    // Falla si el cultivo ya tiene tareas o reportes (clave foránea RESTRICT).
    @Delete suspend fun eliminar(cultivo: Cultivo)

    @Query("SELECT * FROM cultivos ORDER BY nombre")
    fun obtenerTodos(): Flow<List<Cultivo>>

    // Solo los cultivos de un sector: lo uso en el detalle del sector.
    @Query("SELECT * FROM cultivos WHERE sectorId = :sectorId")
    fun obtenerPorSector(sectorId: Long): Flow<List<Cultivo>>

    @Query("SELECT * FROM cultivos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Cultivo?
}
