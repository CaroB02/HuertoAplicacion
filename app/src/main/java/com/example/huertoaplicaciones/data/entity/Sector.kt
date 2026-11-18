package com.example.huertoaplicaciones.data.entity

// Un SECTOR es una zona del huerto (por ejemplo "Huerta Norte" o "Invernadero").
// Viene del microservicio ms-huerto (GET /sectores). El id lo asigna MySQL.
data class Sector(
    val id: Long = 0,
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val estado: String = "",                // Disponible, En uso, En mantenimiento o Fuera de uso
    val responsableId: Long? = null,        // el "?" significa que puede ser null (sector sin responsable)
    // Las fechas se guardan como Long = milisegundos desde 1970 (igual en la app y en el servidor).
    val fechaCreacion: Long = 0,
    val fechaActualizacion: Long = 0
)
