package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.dao.CultivoDao
import com.example.huertoaplicaciones.data.entity.Cultivo
import kotlinx.coroutines.flow.Flow

// Repositorio de cultivos: igual que el de sectores, con un borrado que avisa si falló.
class CultivoRepository(private val cultivoDao: CultivoDao) {
    fun obtenerTodos(): Flow<List<Cultivo>> = cultivoDao.obtenerTodos()
    fun obtenerPorSector(sectorId: Long): Flow<List<Cultivo>> = cultivoDao.obtenerPorSector(sectorId)
    suspend fun obtenerPorId(id: Long): Cultivo? = cultivoDao.obtenerPorId(id)
    suspend fun guardar(cultivo: Cultivo): Long = cultivoDao.insertar(cultivo)
    suspend fun actualizar(cultivo: Cultivo) = cultivoDao.actualizar(cultivo)

    // Devuelve false si el cultivo aún tiene tareas o reportes asociados
    suspend fun eliminar(cultivo: Cultivo): Boolean = try {
        cultivoDao.eliminar(cultivo)
        true
    } catch (e: Exception) {
        false
    }
}
