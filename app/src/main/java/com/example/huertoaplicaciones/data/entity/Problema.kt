package com.example.huertoaplicaciones.data.entity

// Un PROBLEMA es un reporte sobre algo que va mal en el huerto: una plaga, falta de agua...
// El docente le da seguimiento hasta cerrarlo. Viene de ms-tareas (GET /problemas).
data class Problema(
    val id: Long = 0,
    val sectorId: Long = 0,
    val cultivoId: Long = 0,
    val tipo: String = "",
    val descripcion: String = "",
    val fechaReporte: Long = 0,
    val reportadoPorId: Long = 0,
    val fotoUri: String? = null,            // foto opcional del problema
    val estado: String = "",                // Informado, En revisión, Atendido o Cerrado
    val comentarioDocente: String = "",     // el seguimiento que escribe el docente
    val fechaCierre: Long? = null           // se llena cuando pasa a Atendido o Cerrado
)
