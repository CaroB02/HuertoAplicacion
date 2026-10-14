package com.example.huertoaplicaciones.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lecturas_sensor")
data class LecturaSensor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectorId: Long,
    val fecha: Long,
    val humedadSuelo: Int,
    val temperatura: Double,
    val humedadAmbiente: Int,
    val origen: String = "simulación académica",
    val estado: String
)