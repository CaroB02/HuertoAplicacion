package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.util.EstadoSector

data class SectorFormUiState(
    val id: Long = 0,
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val estado: String = EstadoSector.DISPONIBLE,
    val responsableId: Long? = null,
    val fechaCreacion: Long = 0,

    val errorCodigo: Boolean = false,
    val errorNombre: Boolean = false,
    val errorDescripcion: Boolean = false,
    val errorUbicacion: Boolean = false,

    val guardado: Boolean = false
)