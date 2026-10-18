package com.example.huertoaplicaciones.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Cada revisión del docente queda registrada: quién, cuándo y con qué resultado.
// Una tarea puede tener varias validaciones (por ejemplo: rechazada, el estudiante corrige,
// y después validada). Así conservo el HISTORIAL completo y no solo el último estado.
@Entity(
    tableName = "validaciones",
    foreignKeys = [
        ForeignKey(Tarea::class, ["id"], ["tareaId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(Usuario::class, ["id"], ["validadorId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("tareaId"), Index("validadorId")]
)
data class Validacion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tareaId: Long,
    val validadorId: Long,       // el docente que revisó
    val resultado: String,       // EstadoTarea.VALIDADA o EstadoTarea.RECHAZADA
    val comentario: String = "", // el motivo (obligatorio cuando se rechaza)
    val fecha: Long
)
