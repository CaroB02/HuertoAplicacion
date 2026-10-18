package com.example.huertoaplicaciones.data.dao

import androidx.room.*
import com.example.huertoaplicaciones.data.entity.LecturaSensor
import kotlinx.coroutines.flow.Flow

// Consultas de las lecturas SIMULADAS de sensores. No hay sensores reales conectados:
// los datos se cargan como datos de prueba, por eso aquí casi solo se consulta.
@Dao
interface LecturaSensorDao {
    @Insert suspend fun insertar(lectura: LecturaSensor): Long

    // Todas las lecturas, la más reciente primero. Después filtro por sector en el ViewModel.
    @Query("SELECT * FROM lecturas_sensor ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<LecturaSensor>>

    @Query("SELECT COUNT(*) FROM lecturas_sensor")
    suspend fun contar(): Int
}
