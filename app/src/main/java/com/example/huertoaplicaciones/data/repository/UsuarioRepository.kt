package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.dao.UsuarioDao
import com.example.huertoaplicaciones.data.entity.Usuario
import kotlinx.coroutines.flow.Flow

// REPOSITORY = el intermediario entre los ViewModels y los DAOs.
// El ViewModel nunca habla con la base directamente: le pide los datos al repositorio.
// Así, si algún día cambio de dónde vienen los datos (por ejemplo una API), solo toco el repositorio.
// Cadena completa: Pantalla -> ViewModel -> Repository -> DAO -> base de datos.
class UsuarioRepository(private val usuarioDao: UsuarioDao) {
    fun obtenerTodos(): Flow<List<Usuario>> = usuarioDao.obtenerTodos()
    fun obtenerEstudiantes(): Flow<List<Usuario>> = usuarioDao.obtenerEstudiantes()
    suspend fun obtenerPorId(id: Long): Usuario? = usuarioDao.obtenerPorId(id)
    suspend fun obtenerPorCorreo(correo: String): Usuario? = usuarioDao.obtenerPorCorreo(correo)
    suspend fun registrar(usuario: Usuario): Long = usuarioDao.registrar(usuario)
    suspend fun iniciarSesion(correo: String, clave: String): Usuario? =
        usuarioDao.iniciarSesion(correo, clave)
}
