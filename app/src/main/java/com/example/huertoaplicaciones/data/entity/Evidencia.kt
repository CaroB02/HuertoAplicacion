package com.example.huertoaplicaciones.data.entity

// Fotografía que un estudiante adjunta al entregar una tarea (una tarea puede tener varias: 1 a N).
// Viene de ms-tareas (GET /tareas/{id}/evidencias).
data class Evidencia(
    val id: Long = 0,
    val tareaId: Long = 0,       // a qué tarea pertenece esta foto
    val uri: String = "",        // ruta del archivo de la foto dentro del teléfono
    val fecha: Long = 0,
    val subidaPorId: Long = 0    // quién la subió
)
