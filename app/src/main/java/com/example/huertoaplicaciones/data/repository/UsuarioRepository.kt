package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.data.remote.LoginRequest
import com.example.huertoaplicaciones.data.remote.UsuariosApi
import com.example.huertoaplicaciones.data.remote.aRequest
import com.example.huertoaplicaciones.data.remote.comprobarRespuesta
import com.example.huertoaplicaciones.data.remote.cuerpoONulo
import com.example.huertoaplicaciones.util.Perfil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

// REPOSITORY = el intermediario entre los ViewModels y la fuente de datos.
// Antes hablaba con Room (DAO); ahora habla con el microservicio ms-usuarios por Retrofit.
// Los ViewModels casi no cambiaron: siguen llamando a las mismas funciones del repositorio.
// Cadena completa: Pantalla -> ViewModel -> Repository -> Retrofit (UsuariosApi) -> ms-usuarios -> MySQL.
class UsuarioRepository(private val api: UsuariosApi) {

    // Copia en memoria de la última lista recibida del servidor. Es un StateFlow:
    // cuando llega una lista nueva (recargar), todas las pantallas que la observan se actualizan.
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())

    fun obtenerTodos(): Flow<List<Usuario>> = _usuarios.asStateFlow()

    // Solo los estudiantes (para asignar tareas)
    fun obtenerEstudiantes(): Flow<List<Usuario>> =
        _usuarios.map { lista -> lista.filter { it.perfil == Perfil.ESTUDIANTE } }

    // GET /usuarios: pide la lista actualizada al servidor
    suspend fun recargar() {
        _usuarios.value = api.obtenerUsuarios()
    }

    suspend fun obtenerPorId(id: Long): Usuario? = cuerpoONulo(api.obtenerUsuario(id))

    suspend fun obtenerPorCorreo(correo: String): Usuario? = cuerpoONulo(api.buscarPorCorreo(correo))

    // POST /usuarios (409 si el correo ya existe)
    suspend fun registrar(usuario: Usuario) {
        comprobarRespuesta(api.registrar(usuario.aRequest()))
    }

    // POST /usuarios/login: devuelve el usuario, o null si el correo o la clave no coinciden (401)
    suspend fun iniciarSesion(correo: String, clave: String): Usuario? {
        val respuesta = api.iniciarSesion(LoginRequest(correo, clave))
        if (respuesta.code() == 401) return null
        comprobarRespuesta(respuesta)
        return respuesta.body()
    }
}
