package com.example.huertoaplicaciones.data.dao

import androidx.room.*
import com.example.huertoaplicaciones.data.entity.Tarea
import kotlinx.coroutines.flow.Flow

// Consultas de la tabla "tareas".
@Dao
interface TareaDao {
    @Insert suspend fun insertar(tarea: Tarea): Long
    // Cada vez que cambia el estado de una tarea (iniciar, entregar, validar...) uso este @Update.
    @Update suspend fun actualizar(tarea: Tarea)
    // Al borrar una tarea, sus evidencias y validaciones se borran solas (CASCADE).
    @Delete suspend fun eliminar(tarea: Tarea)

    // Todas las tareas, las más próximas primero. Las ve el docente.
    @Query("SELECT * FROM tareas ORDER BY fechaProgramada")
    fun obtenerTodas(): Flow<List<Tarea>>

    // Solo las tareas de un estudiante: así cada estudiante ve únicamente las suyas (privilegio mínimo).
    @Query("SELECT * FROM tareas WHERE asignadoAId = :estudianteId ORDER BY fechaProgramada")
    fun obtenerPorEstudiante(estudianteId: Long): Flow<List<Tarea>>

    @Query("SELECT * FROM tareas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Tarea?

    // Tareas sin hacer cuya fecha ya pasó. "ahora" lo recibo como parámetro para poder probarla.
    @Query("SELECT * FROM tareas WHERE estado IN ('Pendiente', 'En desarrollo') AND fechaProgramada < :ahora")
    fun obtenerAtrasadas(ahora: Long): Flow<List<Tarea>>
}
