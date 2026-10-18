package com.example.huertoaplicaciones.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Un PROBLEMA es un reporte que hace un estudiante (o el docente) sobre algo que va mal en el huerto:
// una plaga, falta de agua, una planta dañada... El docente le da seguimiento hasta cerrarlo.
@Entity(
    tableName = "problemas",
    foreignKeys = [
        // Se relaciona con el sector y el cultivo afectados, y con quien lo reportó.
        ForeignKey(Sector::class, ["id"], ["sectorId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(Cultivo::class, ["id"], ["cultivoId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(Usuario::class, ["id"], ["reportadoPorId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("sectorId"), Index("cultivoId"), Index("reportadoPorId")]
)
data class Problema(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectorId: Long,
    val cultivoId: Long,
    val tipo: String,
    val descripcion: String,
    val fechaReporte: Long,
    val reportadoPorId: Long,
    val fotoUri: String? = null,            // foto opcional del problema
    val estado: String,                     // Informado, En revisión, Atendido o Cerrado
    val comentarioDocente: String = "",     // el seguimiento que escribe el docente
    val fechaCierre: Long? = null           // se llena cuando pasa a Atendido o Cerrado
)
