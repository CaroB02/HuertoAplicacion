package com.example.huertoaplicaciones.data.entity

// Una TAREA es un trabajo del huerto que el docente crea y asigna a un estudiante (riego, siembra...).
// Viene del microservicio ms-tareas (GET /tareas). Las fotos de una tarea son Evidencias
// y cada revisión del docente es una Validacion (también en ms-tareas).
data class Tarea(
    val id: Long = 0,
    val tipo: String = "",                  // Riego, Siembra, Mantenimiento...
    val descripcion: String = "",
    val instrucciones: String = "",         // lo que el estudiante debe hacer paso a paso
    val sectorId: Long = 0,
    val cultivoId: Long = 0,
    val fechaCreacion: Long = 0,
    val fechaProgramada: Long = 0,          // día en que debe hacerse (de aquí sale "Atrasada")
    val asignadoAId: Long = 0,              // estudiante responsable
    val creadoPorId: Long = 0,              // docente que la creó
    // Pendiente -> En desarrollo -> Pendiente de validación -> Validada o Rechazada
    val estado: String = "",
    val fechaRealizacion: Long? = null,     // cuándo el estudiante la entregó
    val comentarioEstudiante: String = "",
    val comentarioDocente: String = "",
    val validadoPorId: Long? = null,        // quién la revisó por última vez
    val fechaValidacion: Long? = null
)
