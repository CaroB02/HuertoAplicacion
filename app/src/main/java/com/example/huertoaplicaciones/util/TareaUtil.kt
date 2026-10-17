package com.example.huertoaplicaciones.util

import com.example.huertoaplicaciones.data.entity.Tarea

private const val UN_DIA = 24L * 60 * 60 * 1000

// Una tarea se muestra "Atrasada" cuando sigue pendiente y ya pasó el día programado
fun estadoMostrado(tarea: Tarea, ahora: Long = System.currentTimeMillis()): String {
    val sinHacer = tarea.estado == EstadoTarea.PENDIENTE ||
            tarea.estado == EstadoTarea.EN_DESARROLLO
    return if (sinHacer && tarea.fechaProgramada + UN_DIA <= ahora)
        EstadoTarea.ATRASADA else tarea.estado
}
