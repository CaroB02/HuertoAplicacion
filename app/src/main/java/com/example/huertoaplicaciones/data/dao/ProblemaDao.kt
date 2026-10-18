package com.example.huertoaplicaciones.data.dao

import androidx.room.*
import com.example.huertoaplicaciones.data.entity.Problema
import kotlinx.coroutines.flow.Flow

// Consultas de la tabla "problemas" (reportes de problemas del huerto).
@Dao
interface ProblemaDao {
    @Insert suspend fun insertar(problema: Problema): Long
    // El docente actualiza el estado y el comentario de seguimiento con este @Update.
    @Update suspend fun actualizar(problema: Problema)

    // Todos los reportes (los ve el docente), el más reciente primero.
    @Query("SELECT * FROM problemas ORDER BY fechaReporte DESC")
    fun obtenerTodos(): Flow<List<Problema>>

    // Solo los reportes que hizo un usuario: cada estudiante ve únicamente los suyos.
    @Query("SELECT * FROM problemas WHERE reportadoPorId = :usuarioId ORDER BY fechaReporte DESC")
    fun obtenerPorUsuario(usuarioId: Long): Flow<List<Problema>>
}
