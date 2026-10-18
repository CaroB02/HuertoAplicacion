package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.dao.ProblemaDao
import com.example.huertoaplicaciones.data.entity.Problema
import kotlinx.coroutines.flow.Flow

// Repositorio de reportes de problemas.
class ProblemaRepository(private val problemaDao: ProblemaDao) {
    fun obtenerTodos(): Flow<List<Problema>> = problemaDao.obtenerTodos()
    fun obtenerPorUsuario(id: Long): Flow<List<Problema>> = problemaDao.obtenerPorUsuario(id)
    suspend fun guardar(problema: Problema): Long = problemaDao.insertar(problema)
    suspend fun actualizar(problema: Problema) = problemaDao.actualizar(problema)
}
