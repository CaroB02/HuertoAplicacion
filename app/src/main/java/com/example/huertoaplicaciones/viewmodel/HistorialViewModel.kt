package com.example.huertoaplicaciones.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertoaplicaciones.data.AppDatabase
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.data.repository.ProblemaRepository
import com.example.huertoaplicaciones.data.repository.TareaRepository
import com.example.huertoaplicaciones.data.repository.UsuarioRepository
import com.example.huertoaplicaciones.util.EstadoTarea
import com.example.huertoaplicaciones.util.Perfil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// Tipos de eventos del historial: la pantalla los usa para elegir el ícono y el color.
enum class TipoEvento { TAREA_CREADA, TAREA_ENTREGADA, VALIDACION, RECHAZO, PROBLEMA, PROBLEMA_ATENDIDO }

// Una línea del historial: cuándo pasó, de qué tipo es, el título y el detalle.
data class EventoHistorial(
    val fecha: Long,
    val tipo: TipoEvento,
    val titulo: String,
    val detalle: String
)

// Línea de tiempo con tareas, entregas, validaciones y problemas.
// El docente ve todo el huerto; cada estudiante solo lo que le corresponde.
// No hay una tabla "historial": lo ARMO juntando datos de varias tablas (tareas, validaciones, problemas).
class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val tareaRepository = TareaRepository(db)
    private val problemaRepository = ProblemaRepository(db.problemaDao())
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())
    private val usuario = MutableStateFlow<Usuario?>(null)

    fun definirUsuario(u: Usuario?) { usuario.value = u }

    // Junto 5 flujos; cualquier cambio en alguno recalcula la línea de tiempo completa.
    val eventos: StateFlow<List<EventoHistorial>> = combine(
        usuario,
        tareaRepository.obtenerTodas(),
        tareaRepository.todasLasValidaciones(),
        problemaRepository.obtenerTodos(),
        usuarioRepository.obtenerTodos()
    ) { u, todasTareas, todasValidaciones, todosProblemas, usuarios ->
        // Sin usuario no hay historial. "return@combine" sale solo de este bloque.
        if (u == null) return@combine emptyList()
        val esDocente = u.perfil == Perfil.ADMIN
        // Función local: dado un id devuelve el nombre de la persona (para "Asignada a Fulano").
        fun nombre(id: Long?) = usuarios.firstOrNull { it.id == id }?.nombre ?: "Usuario"

        // PRIVILEGIO MÍNIMO: el estudiante solo ve sus propias tareas, revisiones y reportes.
        val tareas = if (esDocente) todasTareas else todasTareas.filter { it.asignadoAId == u.id }
        val idsTareas = tareas.map { it.id }.toSet()
        val validaciones = todasValidaciones.filter { it.tareaId in idsTareas }
        val problemas = if (esDocente) todosProblemas
        else todosProblemas.filter { it.reportadoPorId == u.id }

        val eventos = mutableListOf<EventoHistorial>()

        // De cada tarea saco hasta dos eventos: cuándo se creó y cuándo se entregó.
        tareas.forEach { t ->
            eventos += EventoHistorial(
                t.fechaCreacion, TipoEvento.TAREA_CREADA,
                "Tarea creada: ${t.tipo}",
                "Asignada a ${nombre(t.asignadoAId)} por ${nombre(t.creadoPorId)}"
            )
            // "?.let" = solo ejecuto el bloque si fechaRealizacion NO es null (si ya la entregó).
            t.fechaRealizacion?.let { fecha ->
                val comentario = t.comentarioEstudiante.takeIf { it.isNotBlank() }
                eventos += EventoHistorial(
                    fecha, TipoEvento.TAREA_ENTREGADA,
                    "Tarea entregada: ${t.tipo}",
                    "Por ${nombre(t.asignadoAId)}" + (comentario?.let { ". \"$it\"" } ?: "")
                )
            }
        }
        // Cada fila de la tabla validaciones es un evento: validada o rechazada.
        validaciones.forEach { v ->
            val tarea = tareas.firstOrNull { it.id == v.tareaId }
            val rechazada = v.resultado == EstadoTarea.RECHAZADA
            eventos += EventoHistorial(
                v.fecha,
                if (rechazada) TipoEvento.RECHAZO else TipoEvento.VALIDACION,
                (if (rechazada) "Tarea rechazada: " else "Tarea validada: ") + (tarea?.tipo ?: ""),
                "Por ${nombre(v.validadorId)}" +
                        (v.comentario.takeIf { it.isNotBlank() }?.let { ". \"$it\"" } ?: "")
            )
        }
        // De cada problema: el reporte y, si ya se atendió, el cierre.
        problemas.forEach { p ->
            eventos += EventoHistorial(
                p.fechaReporte, TipoEvento.PROBLEMA,
                "Problema reportado: ${p.tipo}",
                "Por ${nombre(p.reportadoPorId)}. ${p.descripcion}"
            )
            p.fechaCierre?.let { fecha ->
                eventos += EventoHistorial(
                    fecha, TipoEvento.PROBLEMA_ATENDIDO,
                    "Problema ${p.estado.lowercase()}: ${p.tipo}",
                    p.comentarioDocente
                )
            }
        }
        // Ordeno del más reciente al más antiguo: eso hace que sea una línea de tiempo cronológica.
        eventos.sortedByDescending { it.fecha }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
