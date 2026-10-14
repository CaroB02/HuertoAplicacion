package com.example.huertoaplicaciones.data.dao

import androidx.room.*
import com.example.huertoaplicaciones.data.entity.Tarea
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {
    @Insert suspend fun insertar(tarea: Tarea): Long
    @Update suspend fun actualizar(tarea: Tarea)

    @Query("SELECT * FROM tareas ORDER BY fechaProgramada")
    fun obtenerTodas(): Flow<List<Tarea>>

    @Query("SELECT * FROM tareas WHERE asignadoAId = :estudianteId ORDER BY fechaProgramada")
    fun obtenerPorEstudiante(estudianteId: Long): Flow<List<Tarea>>

    @Query("SELECT * FROM tareas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Tarea?

    @Query("SELECT * FROM tareas WHERE estado IN ('Pendiente', 'En desarrollo') AND fechaProgramada < :ahora")
    fun obtenerAtrasadas(ahora: Long): Flow<List<Tarea>>
}