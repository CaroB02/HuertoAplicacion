package com.example.huertoaplicaciones.data.dao

import androidx.room.*
import com.example.huertoaplicaciones.data.entity.Evidencia
import kotlinx.coroutines.flow.Flow

// Consultas de la tabla "evidencias" (las fotos que se adjuntan a una tarea).
@Dao
interface EvidenciaDao {
    @Insert suspend fun insertar(evidencia: Evidencia): Long

    // Las fotos de UNA tarea, la más reciente primero. Como es Flow, si llega una foto nueva
    // la galería del detalle de la tarea se actualiza sola.
    @Query("SELECT * FROM evidencias WHERE tareaId = :tareaId ORDER BY fecha DESC")
    fun obtenerPorTarea(tareaId: Long): Flow<List<Evidencia>>
}
