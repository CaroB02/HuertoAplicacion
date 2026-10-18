package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.dao.SectorDao
import com.example.huertoaplicaciones.data.entity.Sector
import kotlinx.coroutines.flow.Flow

// Repositorio de sectores: pasa las operaciones del ViewModel al DAO.
class SectorRepository(private val sectorDao: SectorDao) {
    fun obtenerTodos(): Flow<List<Sector>> = sectorDao.obtenerTodos()
    suspend fun obtenerPorId(id: Long): Sector? = sectorDao.obtenerPorId(id)
    suspend fun guardar(sector: Sector): Long = sectorDao.insertar(sector)
    suspend fun actualizar(sector: Sector) = sectorDao.actualizar(sector)

    // Devuelve false si el sector aún tiene cultivos, tareas o reportes asociados
    // (la base lanza una excepción por la clave foránea y yo la atrapo con try/catch).
    suspend fun eliminar(sector: Sector): Boolean = try {
        sectorDao.eliminar(sector)
        true
    } catch (e: Exception) {
        false
    }
}
