package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.util.TipoTarea

// Estado del formulario para crear o editar una tarea (lo que escribió el docente + los errores).
data class TareaFormUiState(
    val id: Long = 0,                       // 0 = tarea nueva; otro valor = estoy editando esa tarea
    val tipo: String = TipoTarea.RIEGO,
    val descripcion: String = "",
    val instrucciones: String = "",
    val sectorId: Long? = null,
    val cultivoId: Long? = null,
    val estudianteId: Long? = null,         // a quién se asigna la tarea
    val fechaProgramada: String = "",
    val fechaOriginal: Long? = null,        // al editar: la fecha que ya tenía la tarea (se puede conservar)

    // Una bandera de error por campo obligatorio.
    val errorDescripcion: Boolean = false,
    val errorInstrucciones: Boolean = false,
    val errorSector: Boolean = false,
    val errorCultivo: Boolean = false,
    val errorEstudiante: Boolean = false,
    val errorFecha: Boolean = false,

    val guardado: Boolean = false
)

// Filtros del listado de tareas del docente
// "enum class" = un conjunto fijo de opciones; cada una lleva el texto que se ve en el botón.
enum class FiltroFecha(val etiqueta: String) {
    TODAS("Cualquier fecha"),
    HOY("Hoy"),
    SEMANA("Próximos 7 días")
}

// Los filtros activos. Un valor null significa "sin filtrar por ese criterio".
data class FiltrosTarea(
    val estado: String? = null,
    val responsableId: Long? = null,
    val fecha: FiltroFecha = FiltroFecha.TODAS
) {
    // Propiedad calculada: true si hay al menos un filtro puesto (la pantalla muestra "Limpiar filtros").
    val hayFiltros: Boolean
        get() = estado != null || responsableId != null || fecha != FiltroFecha.TODAS
}

// Errores de la entrega del estudiante (comentario y evidencia son obligatorios)
data class ErrorEntrega(
    val comentario: Boolean = false,
    val foto: Boolean = false
)
