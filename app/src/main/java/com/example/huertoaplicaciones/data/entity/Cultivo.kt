package com.example.huertoaplicaciones.data.entity

// Un CULTIVO es una planta que se cultiva dentro de un sector (lechuga, tomate, etc.).
// Relación: un Sector tiene muchos Cultivos (1 a N). Cada cultivo guarda el id de su sector.
// Viene del microservicio ms-huerto (GET /cultivos).
data class Cultivo(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String = "",
    val sectorId: Long = 0,                 // id del sector al que pertenece
    val fechaSiembra: Long = 0,
    val fechaTrasplante: Long? = null,      // opcional: no todos los cultivos se trasplantan
    val fechaCosecha: Long = 0,             // fecha estimada de cosecha
    val frecuenciaRiegoDias: Int = 1,       // cada cuántos días hay que regar
    val estado: String = "",                // Planificado, Sembrado, En crecimiento, etc.
    val observaciones: String = "",
    val fotoUri: String? = null             // ruta de la foto referencial (puede no tener)
)
