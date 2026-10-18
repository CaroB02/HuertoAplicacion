package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.util.EstadoSector

// Estado del formulario de sectores: lo que el usuario escribió + qué campos tienen error.
// Los campos de texto se guardan como String porque así los entrega el teclado.
data class SectorFormUiState(
    val id: Long = 0,                       // 0 = sector nuevo; distinto de 0 = estoy editando uno existente
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val estado: String = EstadoSector.DISPONIBLE,
    val responsableId: Long? = null,
    val fechaCreacion: Long = 0,

    // Una bandera de error por cada campo: true = el campo se pinta en rojo con su mensaje.
    val errorCodigo: Boolean = false,
    val errorNombre: Boolean = false,
    val errorDescripcion: Boolean = false,
    val errorUbicacion: Boolean = false,

    // Pasa a true cuando se guardó bien: la pantalla lo detecta y vuelve a la lista.
    val guardado: Boolean = false
)
