package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.util.EstadoCultivo

data class CultivoFormUiState(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String = "",
    val sectorId: Long? = null,
    val fechaSiembra: String = "",
    val fechaCosecha: String = "",
    val frecuenciaRiego: String = "",
    val estado: String = EstadoCultivo.PLANIFICADO,
    val observaciones: String = "",
    val fechaTrasplante: Long? = null,
    val fotoUri: String? = null,

    val errorNombre: Boolean = false,
    val errorDescripcion: Boolean = false,
    val errorSector: Boolean = false,
    val errorFechaSiembra: Boolean = false,
    val errorFechaCosecha: Boolean = false,
    val errorFrecuencia: Boolean = false,

    val guardado: Boolean = false
)