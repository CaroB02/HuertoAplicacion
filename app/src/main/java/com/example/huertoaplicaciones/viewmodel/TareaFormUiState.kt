package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.util.TipoTarea

data class TareaFormUiState(
    val tipo: String = TipoTarea.RIEGO,
    val descripcion: String = "",
    val instrucciones: String = "",
    val sectorId: Long? = null,
    val cultivoId: Long? = null,
    val estudianteId: Long? = null,
    val fechaProgramada: String = "",

    val errorDescripcion: Boolean = false,
    val errorInstrucciones: Boolean = false,
    val errorSector: Boolean = false,
    val errorCultivo: Boolean = false,
    val errorEstudiante: Boolean = false,
    val errorFecha: Boolean = false,

    val guardado: Boolean = false
)