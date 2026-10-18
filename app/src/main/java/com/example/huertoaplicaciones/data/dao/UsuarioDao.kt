package com.example.huertoaplicaciones.data.dao

import androidx.room.*
import com.example.huertoaplicaciones.data.entity.Usuario
import kotlinx.coroutines.flow.Flow

// DAO = "Data Access Object": aquí escribo QUÉ consultas se pueden hacer sobre la tabla usuarios.
// Es una interfaz: yo solo declaro las funciones y Room genera el código SQL real por mí.
// Reglas que debo recordar:
//  - Las funciones "suspend" se llaman desde una corrutina y NO bloquean la pantalla.
//  - Las que devuelven Flow avisan solas cada vez que la tabla cambia (la pantalla se actualiza sola).
@Dao
interface UsuarioDao {
    // REPLACE: si inserto un usuario con un id que ya existe, lo reemplaza en vez de fallar.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: Usuario): Long

    // Para REGISTRAR una cuenta nueva. ABORT = si el correo ya existe, falla en vez de pisar la cuenta
    // existente (con REPLACE, alguien podría sobrescribir la cuenta del docente registrándose con su correo).
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrar(usuario: Usuario): Long

    // Busca por correo: sirve para avisar "ese correo ya está registrado" antes de intentar guardar.
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun obtenerPorCorreo(correo: String): Usuario?

    // Para el login: busco un usuario ACTIVO con ese correo y esa clave. Si no existe devuelve null.
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND clave = :clave AND activo = 1 LIMIT 1")
    suspend fun iniciarSesion(correo: String, clave: String): Usuario?

    // Lista de estudiantes (para elegir a quién asignar una tarea).
    @Query("SELECT * FROM usuarios WHERE perfil = 'ESTUDIANTE'")
    fun obtenerEstudiantes(): Flow<List<Usuario>>

    // Todos los usuarios: la uso para mostrar nombres ("Asignada a Estudiante Uno").
    @Query("SELECT * FROM usuarios")
    fun obtenerTodos(): Flow<List<Usuario>>

    // Busca uno por id; devuelve null si no existe (así valido que el responsable de una tarea exista).
    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Usuario?

    // Cuántos usuarios hay: los datos de prueba solo se cargan si la tabla está vacía.
    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}
