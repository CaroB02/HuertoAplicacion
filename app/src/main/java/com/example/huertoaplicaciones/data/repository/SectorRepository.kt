package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.dao.SectorDao
import com.example.huertoaplicaciones.data.entity.Sector
import kotlinx.coroutines.flow.Flow

class SectorRepository(private val sectorDao: SectorDao) {
    fun obtenerTodos(): Flow<List<Sector>> = sectorDao.obtenerTodos()
    suspend fun obtenerPorId(id: Long): Sector? = sectorDao.obtenerPorId(id)
    suspend fun guardar(sector: Sector): Long = sectorDao.insertar(sector)
    suspend fun actualizar(sector: Sector) = sectorDao.actualizar(sector)
}