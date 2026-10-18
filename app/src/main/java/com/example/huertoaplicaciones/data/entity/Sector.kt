package com.example.huertoaplicaciones.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Un SECTOR es una zona del huerto (por ejemplo "Huerta Norte" o "Invernadero").
// Dentro de un sector se plantan varios cultivos.
@Entity(
    tableName = "sectores",
    // CLAVE FORÁNEA: responsableId apunta al id de un Usuario (el docente a cargo del sector).
    // SET_NULL = si borran a ese usuario, el sector NO se borra, solo queda sin responsable.
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["responsableId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    // Room recomienda indexar las columnas que son claves foráneas para que las búsquedas sean rápidas.
    indices = [Index("responsableId")]
)
data class Sector(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val ubicacion: String,
    val estado: String,                 // Disponible, En uso, En mantenimiento o Fuera de uso
    val responsableId: Long? = null,    // el "?" significa que puede ser null (sector sin responsable)
    // Las fechas las guardo como Long = milisegundos desde 1970 (así las compara fácil la base de datos).
    val fechaCreacion: Long,
    val fechaActualizacion: Long
)
