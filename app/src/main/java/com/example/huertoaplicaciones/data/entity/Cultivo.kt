package com.example.huertoaplicaciones.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Un CULTIVO es una planta que se cultiva dentro de un sector (lechuga, tomate, etc.).
// Relación: un Sector tiene muchos Cultivos (1 a N). Cada cultivo guarda el id de su sector.
@Entity(
    tableName = "cultivos",
    foreignKeys = [
        ForeignKey(
            entity = Sector::class,
            parentColumns = ["id"],
            childColumns = ["sectorId"],
            // RESTRICT = la base NO me deja borrar un sector que todavía tiene cultivos.
            // Por eso en la app el borrado de sectores muestra "No se puede eliminar...".
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("sectorId")]
)
data class Cultivo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val descripcion: String,
    val sectorId: Long,                     // clave foránea hacia Sector
    val fechaSiembra: Long,
    val fechaTrasplante: Long? = null,      // opcional: no todos los cultivos se trasplantan
    val fechaCosecha: Long,                 // fecha estimada de cosecha
    val frecuenciaRiegoDias: Int,           // cada cuántos días hay que regar
    val estado: String,                     // Planificado, Sembrado, En crecimiento, etc.
    val observaciones: String = "",
    val fotoUri: String? = null             // ruta de la foto referencial (puede no tener)
)
