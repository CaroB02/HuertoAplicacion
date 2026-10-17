package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.dao.TareaDao
import com.example.huertoaplicaciones.data.entity.Tarea
import kotlinx.coroutines.flow.Flow

class TareaRepository(private val tareaDao: TareaDao) {
    fun obtenerTodas(): Flow<List<Tarea>> = tareaDao.obtenerTodas()
    fun obtenerPorEstudiante(id: Long): Flow<List<Tarea>> = tareaDao.obtenerPorEstudiante(id)
    suspend fun obtenerPorId(id: Long): Tarea? = tareaDao.obtenerPorId(id)
    suspend fun guardar(tarea: Tarea): Long = tareaDao.insertar(tarea)
    suspend fun actualizar(tarea: Tarea) = tareaDao.actualizar(tarea)
}