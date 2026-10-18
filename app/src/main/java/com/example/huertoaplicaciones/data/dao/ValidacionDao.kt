package com.example.huertoaplicaciones.data.dao

import androidx.room.*
import com.example.huertoaplicaciones.data.entity.Validacion
import kotlinx.coroutines.flow.Flow

// Consultas de la tabla "validaciones" (el registro de cada revisión del docente).
// No tiene @Update ni @Delete a propósito: un historial no se modifica, solo se agrega.
@Dao
interface ValidacionDao {
    @Insert suspend fun insertar(validacion: Validacion): Long

    // Todas las revisiones: la usa el historial de actividades.
    @Query("SELECT * FROM validaciones ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<Validacion>>

    // Las revisiones de una tarea: la usa el detalle ("Historial de revisiones").
    @Query("SELECT * FROM validaciones WHERE tareaId = :tareaId ORDER BY fecha DESC")
    fun obtenerPorTarea(tareaId: Long): Flow<List<Validacion>>
}
