package com.example.huertoaplicaciones.util

import com.example.huertoaplicaciones.data.entity.Tarea

// "Atrasada" es un estado CALCULADO, no guardado. Si lo guardara en la base, tendría que haber un
// proceso que lo actualice cada día. En vez de eso lo calculo cada vez que muestro la tarea.
// Una tarea se muestra "Atrasada" cuando sigue pendiente y ya pasó el día programado
fun estadoMostrado(tarea: Tarea, ahora: Long = System.currentTimeMillis()): String {
    // "Sin hacer" = todavía Pendiente o En desarrollo (si ya la entregó, no puede estar atrasada).
    val sinHacer = tarea.estado == EstadoTarea.PENDIENTE ||
            tarea.estado == EstadoTarea.EN_DESARROLLO
    // Le doy un día completo de gracia: se atrasa recién cuando pasó el día programado entero.
    return if (sinHacer && tarea.fechaProgramada + MILIS_POR_DIA <= ahora)
        EstadoTarea.ATRASADA else tarea.estado
}
