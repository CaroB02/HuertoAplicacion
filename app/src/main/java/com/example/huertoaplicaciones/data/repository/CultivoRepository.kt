package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.dao.CultivoDao
import com.example.huertoaplicaciones.data.entity.Cultivo
import kotlinx.coroutines.flow.Flow

class CultivoRepository(private val cultivoDao: CultivoDao) {
    fun obtenerTodos(): Flow<List<Cultivo>> = cultivoDao.obtenerTodos()
    suspend fun obtenerPorId(id: Long): Cultivo? = cultivoDao.obtenerPorId(id)
    suspend fun guardar(cultivo: Cultivo): Long = cultivoDao.insertar(cultivo)
    suspend fun actualizar(cultivo: Cultivo) = cultivoDao.actualizar(cultivo)
}
