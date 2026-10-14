package com.example.huertoaplicaciones.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "problemas")
data class Problema(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectorId: Long,
    val cultivoId: Long,
    val tipo: String,
    val descripcion: String,
    val fechaReporte: Long,
    val reportadoPorId: Long,
    val fotoUri: String? = null,
    val estado: String,
    val comentarioDocente: String = "",
    val fechaCierre: Long? = null
)