package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.dao.LecturaSensorDao
import com.example.huertoaplicaciones.data.entity.LecturaSensor
import kotlinx.coroutines.flow.Flow

// Las lecturas son simuladas (datos de prueba); aquí solo se consultan
class LecturaSensorRepository(private val lecturaSensorDao: LecturaSensorDao) {
    fun obtenerTodas(): Flow<List<LecturaSensor>> = lecturaSensorDao.obtenerTodas()
}
