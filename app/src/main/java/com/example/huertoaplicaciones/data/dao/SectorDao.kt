package com.example.huertoaplicaciones.data.dao

import androidx.room.*
import com.example.huertoaplicaciones.data.entity.Sector
import kotlinx.coroutines.flow.Flow

// Consultas de la tabla "sectores". Tiene el CRUD completo:
// Create (insertar), Read (obtener...), Update (actualizar) y Delete (eliminar).
@Dao
interface SectorDao {
    // @Insert devuelve el id que la base le asignó al nuevo sector.
    @Insert suspend fun insertar(sector: Sector): Long
    // @Update busca la fila por su id y reemplaza los demás campos.
    @Update suspend fun actualizar(sector: Sector)
    // @Delete borra la fila. Si el sector tiene cultivos, la clave foránea RESTRICT lanza un error.
    @Delete suspend fun eliminar(sector: Sector)

    // Devuelve un Flow: la lista de sectores en pantalla se refresca sola al crear o editar uno.
    @Query("SELECT * FROM sectores ORDER BY nombre")
    fun obtenerTodos(): Flow<List<Sector>>

    @Query("SELECT * FROM sectores WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Sector?
}
