package com.example.huertoaplicaciones.data.entity

// Cada revisión del docente queda registrada: quién, cuándo y con qué resultado.
// Una tarea puede tener varias validaciones (rechazada, el estudiante corrige, y después validada).
// Viene de ms-tareas (GET /validaciones y GET /tareas/{id}/validaciones).
data class Validacion(
    val id: Long = 0,
    val tareaId: Long = 0,
    val validadorId: Long = 0,       // el docente que revisó
    val resultado: String = "",      // EstadoTarea.VALIDADA o EstadoTarea.RECHAZADA
    val comentario: String = "",     // el motivo (obligatorio cuando se rechaza)
    val fecha: Long = 0
)
