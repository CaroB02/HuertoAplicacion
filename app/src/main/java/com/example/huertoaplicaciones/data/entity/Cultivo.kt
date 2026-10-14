package com.example.huertoaplicaciones.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cultivos")
data class Cultivo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val descripcion: String,
    val sectorId: Long,
    val fechaSiembra: Long,
    val fechaTrasplante: Long? = null,
    val fechaCosecha: Long,
    val frecuenciaRiegoDias: Int,
    val estado: String,
    val observaciones: String = "",
    val fotoUri: String? = null
)