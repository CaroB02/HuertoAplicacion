package com.example.huertoaplicaciones.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tareas")
data class Tarea(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,
    val descripcion: String,
    val instrucciones: String,
    val sectorId: Long,
    val cultivoId: Long,
    val fechaCreacion: Long,
    val fechaProgramada: Long,
    val asignadoAId: Long,
    val creadoPorId: Long,
    val estado: String,
    val fechaRealizacion: Long? = null,
    val fotoUri: String? = null,
    val comentarioEstudiante: String = "",
    val comentarioDocente: String = "",
    val validadoPorId: Long? = null,
    val fechaValidacion: Long? = null
)