package com.example.huertoaplicaciones.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Una TAREA es un trabajo del huerto que el docente crea y asigna a un estudiante (riego, siembra...).
// Esta es la tabla central del proyecto: se relaciona con sector, cultivo y dos usuarios distintos.
// Las fotos de una tarea viven en la tabla Evidencia y cada revisión en Validacion.
// Los campos de validación de esta tabla guardan solo el resultado más reciente.
@Entity(
    tableName = "tareas",
    foreignKeys = [
        // En qué sector y en qué cultivo se hace la tarea (no se pueden borrar mientras haya tareas).
        ForeignKey(Sector::class, ["id"], ["sectorId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(Cultivo::class, ["id"], ["cultivoId"], onDelete = ForeignKey.RESTRICT),
        // El MISMO tipo de tabla (Usuario) aparece tres veces porque cumple tres roles distintos:
        // a quién se asignó, quién la creó y quién la revisó.
        ForeignKey(Usuario::class, ["id"], ["asignadoAId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(Usuario::class, ["id"], ["creadoPorId"], onDelete = ForeignKey.RESTRICT),
        // validadoPorId puede ser null (aún no la revisan), por eso aquí uso SET_NULL.
        ForeignKey(Usuario::class, ["id"], ["validadoPorId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [
        Index("sectorId"), Index("cultivoId"), Index("asignadoAId"),
        Index("creadoPorId"), Index("validadoPorId")
    ]
)
data class Tarea(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,                  // Riego, Siembra, Mantenimiento...
    val descripcion: String,
    val instrucciones: String,         // lo que el estudiante debe hacer paso a paso
    val sectorId: Long,
    val cultivoId: Long,
    val fechaCreacion: Long,
    val fechaProgramada: Long,         // día en que debe hacerse (de aquí sale "Atrasada")
    val asignadoAId: Long,             // estudiante responsable
    val creadoPorId: Long,             // docente que la creó
    // Pendiente -> En desarrollo -> Pendiente de validación -> Validada o Rechazada
    val estado: String,
    val fechaRealizacion: Long? = null,         // cuándo el estudiante la entregó
    val comentarioEstudiante: String = "",
    val comentarioDocente: String = "",
    val validadoPorId: Long? = null,            // quién la revisó por última vez
    val fechaValidacion: Long? = null
)
