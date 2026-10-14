package com.example.huertoaplicaciones.data.dao

import androidx.room.*
import com.example.huertoaplicaciones.data.entity.LecturaSensor
import kotlinx.coroutines.flow.Flow

@Dao
interface LecturaSensorDao {
    @Insert suspend fun insertar(lectura: LecturaSensor): Long

    @Query("SELECT * FROM lecturas_sensor ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<LecturaSensor>>

    @Query("SELECT COUNT(*) FROM lecturas_sensor")
    suspend fun contar(): Int
}