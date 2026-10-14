package com.example.huertoaplicaciones.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sectores")
data class Sector(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val ubicacion: String,
    val estado: String,
    val responsableId: Long? = null,
    val fechaCreacion: Long,
    val fechaActualizacion: Long
)