package com.example.huertoaplicaciones.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Fotografía que un estudiante adjunta al entregar una tarea.
// La separé de Tarea porque una tarea puede tener VARIAS fotos (relación 1 a N).
// Si pusiera las fotos dentro de Tarea tendría columnas repetidas, y eso rompe la normalización (3FN).
@Entity(
    tableName = "evidencias",
    foreignKeys = [
        // CASCADE = si borro la tarea, sus evidencias se borran automáticamente con ella.
        ForeignKey(Tarea::class, ["id"], ["tareaId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(Usuario::class, ["id"], ["subidaPorId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("tareaId"), Index("subidaPorId")]
)
data class Evidencia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tareaId: Long,       // a qué tarea pertenece esta foto
    val uri: String,         // ruta del archivo de la foto dentro del almacenamiento de la app
    val fecha: Long,
    val subidaPorId: Long    // quién la subió
)
