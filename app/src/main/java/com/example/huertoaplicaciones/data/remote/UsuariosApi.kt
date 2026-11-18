package com.example.huertoaplicaciones.data.remote

import com.example.huertoaplicaciones.data.entity.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints del microservicio ms-usuarios (http://10.0.2.2:8081/).
// Cada función es una petición HTTP: la anotación indica el método y la ruta.
// "suspend" permite llamarlas desde una corrutina sin congelar la pantalla.
// Response<...> entrega también el código HTTP (200, 401, 404, 409...) para revisarlo con isSuccessful.
interface UsuariosApi {

    @GET("usuarios")
    suspend fun obtenerUsuarios(): List<Usuario>

    @GET("usuarios/{id}")
    suspend fun obtenerUsuario(@Path("id") id: Long): Response<Usuario>

    // GET usuarios/buscar?correo=... -> 404 si no existe
    @GET("usuarios/buscar")
    suspend fun buscarPorCorreo(@Query("correo") correo: String): Response<Usuario>

    @POST("usuarios")
    suspend fun registrar(@Body usuario: UsuarioRequest): Response<Void>

    // 200 con el usuario, o 401 si el correo o la clave no coinciden
    @POST("usuarios/login")
    suspend fun iniciarSesion(@Body login: LoginRequest): Response<Usuario>
}
