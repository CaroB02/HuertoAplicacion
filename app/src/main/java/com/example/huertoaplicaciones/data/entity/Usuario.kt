package com.example.huertoaplicaciones.data.entity

// MODELO: un Usuario tal como lo envía y recibe el microservicio ms-usuarios (puerto 8081).
// Antes era una tabla de Room; ahora los datos viven en MySQL y llegan como JSON.
// Gson convierte cada campo del JSON en el atributo con el MISMO nombre.
// Todos los campos tienen un valor por defecto: así Gson puede crear el objeto aunque falte algún dato
// (por ejemplo "clave", que el servidor nunca devuelve por seguridad).
data class Usuario(
    val id: Long = 0,
    val nombre: String = "",
    val correo: String = "",
    // Solo se envía al registrarse; el servidor no la devuelve (@JsonProperty WRITE_ONLY).
    val clave: String = "",
    val perfil: String = "",          // "ADMIN" o "ESTUDIANTE"
    val grupo: String = "",
    // Si activo = false la persona no puede iniciar sesión ni recibir tareas.
    val activo: Boolean = true
)
