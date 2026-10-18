package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.util.EstadoCultivo

// Estado del formulario de cultivos. Las fechas y la frecuencia de riego son String porque
// así las escribe el usuario; recién al guardar las convierto a Long e Int (y valido que sean correctas).
data class CultivoFormUiState(
    val id: Long = 0,                       // 0 = cultivo nuevo; otro valor = edición
    val nombre: String = "",
    val descripcion: String = "",
    val sectorId: Long? = null,             // null = todavía no eligió sector
    val fechaSiembra: String = "",
    val fechaTrasplante: String = "",       // opcional
    val fechaCosecha: String = "",
    val frecuenciaRiego: String = "",
    val estado: String = EstadoCultivo.PLANIFICADO,
    val observaciones: String = "",
    val fotoUri: String? = null,

    // Una bandera de error por campo (true = se pinta en rojo con su mensaje).
    val errorNombre: Boolean = false,
    val errorDescripcion: Boolean = false,
    val errorSector: Boolean = false,
    val errorFechaSiembra: Boolean = false,
    val errorFechaTrasplante: Boolean = false,
    val errorFechaCosecha: Boolean = false,
    val errorFrecuencia: Boolean = false,

    // true cuando se guardó bien: la pantalla detecta el cambio y vuelve a la lista.
    val guardado: Boolean = false
)
