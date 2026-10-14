package com.example.huertoaplicaciones.data.dao

import androidx.room.*
import com.example.huertoaplicaciones.data.entity.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: Usuario): Long

    /*@Query("SELECT * FROM usuarios WHERE usuario = :usuario AND clave = :clave AND activo = 1 LIMIT 1")
    suspend fun iniciarSesion(usuario: String, clave: String): Usuario?*/

    @Query("SELECT * FROM usuarios WHERE correo = :correo AND clave = :clave AND activo = 1 LIMIT 1")
    suspend fun iniciarSesion(correo: String, clave: String): Usuario?
    @Query("SELECT * FROM usuarios WHERE perfil = 'ESTUDIANTE'")
    fun obtenerEstudiantes(): Flow<List<Usuario>>
    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}