package com.example.huertoaplicaciones.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Una LECTURA SENSOR es una medición de humedad y temperatura de un sector.
// OJO: son datos SIMULADOS (de prueba), no vienen de un sensor real. Por eso el campo "origen"
// dice "simulación académica", y en la pantalla los muestro con un estilo distinto.
@Entity(
    tableName = "lecturas_sensor",
    foreignKeys = [
        // CASCADE: si se borra el sector, sus lecturas ya no sirven y se borran también.
        ForeignKey(Sector::class, ["id"], ["sectorId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("sectorId")]
)
data class LecturaSensor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectorId: Long,
    val fecha: Long,
    val humedadSuelo: Int,          // porcentaje
    val temperatura: Double,        // grados Celsius
    val humedadAmbiente: Int,       // porcentaje
    val origen: String = "simulación académica",
    val estado: String              // Normal, Bajo, Alto o Pendiente de revisión
)
