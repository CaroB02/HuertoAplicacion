package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.entity.Evidencia
import com.example.huertoaplicaciones.data.entity.Tarea
import com.example.huertoaplicaciones.data.entity.Validacion
import com.example.huertoaplicaciones.data.remote.EntregaRequest
import com.example.huertoaplicaciones.data.remote.TareasApi
import com.example.huertoaplicaciones.data.remote.ValidacionRequest
import com.example.huertoaplicaciones.data.remote.aRequest
import com.example.huertoaplicaciones.data.remote.comprobarRespuesta
import com.example.huertoaplicaciones.data.remote.cuerpoONulo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// Repositorio de tareas: el más completo, porque una tarea involucra tres recursos de ms-tareas:
// la tarea, sus evidencias (fotos) y sus validaciones (revisiones del docente).
class TareaRepository(private val api: TareasApi) {

    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    private val _validaciones = MutableStateFlow<List<Validacion>>(emptyList())

    // Contador que sube cada vez que algo cambia: así las fotos y revisiones de la tarea abierta
    // se vuelven a pedir al servidor después de entregar o validar.
    private val _cambios = MutableStateFlow(0)

    fun obtenerTodas(): Flow<List<Tarea>> = _tareas.asStateFlow()

    // Solo las tareas de un estudiante: así cada estudiante ve únicamente las suyas
    fun obtenerPorEstudiante(id: Long): Flow<List<Tarea>> =
        _tareas.map { lista -> lista.filter { it.asignadoAId == id } }

    fun todasLasValidaciones(): Flow<List<Validacion>> = _validaciones.asStateFlow()

    // GET /tareas y GET /validaciones
    suspend fun recargar() {
        _tareas.value = api.obtenerTareas()
        _validaciones.value = api.obtenerValidaciones()
        _cambios.update { it + 1 }
    }

    suspend fun obtenerPorId(id: Long): Tarea? = cuerpoONulo(api.obtenerTarea(id))

    // POST /tareas
    suspend fun guardar(tarea: Tarea) {
        comprobarRespuesta(api.crearTarea(tarea.aRequest()))
        recargar()
    }

    // PUT /tareas/{id} (editar o cambiar de estado, por ejemplo al iniciarla)
    suspend fun actualizar(tarea: Tarea) {
        comprobarRespuesta(api.actualizarTarea(tarea.id, tarea.aRequest()))
        recargar()
    }

    // DELETE /tareas/{id}. false si el servidor responde 409 (la tarea ya se inició).
    suspend fun eliminar(tarea: Tarea): Boolean {
        val respuesta = api.eliminarTarea(tarea.id)
        if (respuesta.code() == 409) return false
        comprobarRespuesta(respuesta)
        recargar()
        return true
    }

    // GET /tareas/{id}/evidencias. Se vuelve a pedir cada vez que cambia el contador.
    fun evidenciasDe(tareaId: Long): Flow<List<Evidencia>> =
        _cambios.map { listaSegura { api.obtenerEvidencias(tareaId) } }

    // GET /tareas/{id}/validaciones
    fun validacionesDe(tareaId: Long): Flow<List<Validacion>> =
        _cambios.map { listaSegura { api.obtenerValidacionesDeTarea(tareaId) } }

    // POST /tareas/{id}/entrega. El servidor cambia el estado y guarda las fotos en UNA transacción
    // (@Transactional): o se guarda todo o no se guarda nada.
    suspend fun entregar(tarea: Tarea, fotos: List<String>, estudianteId: Long) {
        val entrega = EntregaRequest(
            estudianteId = estudianteId,
            comentario = tarea.comentarioEstudiante,
            fotos = fotos,
            fechaRealizacion = tarea.fechaRealizacion ?: System.currentTimeMillis()
        )
        comprobarRespuesta(api.entregarTarea(tarea.id, entrega))
        recargar()
    }

    // POST /tareas/{id}/validaciones. El servidor actualiza la tarea y guarda la revisión
    // en el historial, también en una sola transacción.
    suspend fun registrarValidacion(tarea: Tarea, validacion: Validacion) {
        val revision = ValidacionRequest(
            validadorId = validacion.validadorId,
            resultado = validacion.resultado,
            comentario = validacion.comentario,
            fecha = validacion.fecha
        )
        comprobarRespuesta(api.validarTarea(tarea.id, revision))
        recargar()
    }

    // Si falla la conexión al pedir fotos o revisiones, muestro la lista vacía en vez de cerrar la app
    private suspend fun <T> listaSegura(llamada: suspend () -> List<T>): List<T> =
        try {
            llamada()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
}
