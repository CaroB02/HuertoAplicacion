package com.example.huertoaplicaciones.data.dao

import androidx.room.*
import com.example.huertoaplicaciones.data.entity.Problema
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemaDao {
    @Insert suspend fun insertar(problema: Problema): Long
    @Update suspend fun actualizar(problema: Problema)

    @Query("SELECT * FROM problemas ORDER BY fechaReporte DESC")
    fun obtenerTodos(): Flow<List<Problema>>
}