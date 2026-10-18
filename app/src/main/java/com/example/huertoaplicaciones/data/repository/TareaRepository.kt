package com.example.huertoaplicaciones.data.repository

import androidx.room.withTransaction
import com.example.huertoaplicaciones.data.AppDatabase
import com.example.huertoaplicaciones.data.entity.Evidencia
import com.example.huertoaplicaciones.data.entity.Tarea
import com.example.huertoaplicaciones.data.entity.Validacion
import kotlinx.coroutines.flow.Flow

// Repositorio de tareas. Es el más completo porque una tarea toca tres tablas:
// tareas, evidencias (fotos) y validaciones (revisiones del docente).
// Por eso recibe la AppDatabase entera y no un solo DAO: necesito los tres y también las transacciones.
class TareaRepository(private val db: AppDatabase) {
    private val tareaDao = db.tareaDao()
    private val evidenciaDao = db.evidenciaDao()
    private val validacionDao = db.validacionDao()

    fun obtenerTodas(): Flow<List<Tarea>> = tareaDao.obtenerTodas()
    fun obtenerPorEstudiante(id: Long): Flow<List<Tarea>> = tareaDao.obtenerPorEstudiante(id)
    suspend fun obtenerPorId(id: Long): Tarea? = tareaDao.obtenerPorId(id)
    suspend fun guardar(tarea: Tarea): Long = tareaDao.insertar(tarea)
    suspend fun actualizar(tarea: Tarea) = tareaDao.actualizar(tarea)

    suspend fun eliminar(tarea: Tarea): Boolean = try {
        tareaDao.eliminar(tarea)
        true
    } catch (e: Exception) {
        false
    }

    fun evidenciasDe(tareaId: Long): Flow<List<Evidencia>> = evidenciaDao.obtenerPorTarea(tareaId)
    fun validacionesDe(tareaId: Long): Flow<List<Validacion>> = validacionDao.obtenerPorTarea(tareaId)
    fun todasLasValidaciones(): Flow<List<Validacion>> = validacionDao.obtenerTodas()

    // Entrega del estudiante: cambia el estado y guarda sus fotos en una sola operación.
    // TRANSACCIÓN = o se guardan TODOS los cambios o no se guarda NINGUNO.
    // Así no puede quedar una tarea "entregada" sin sus fotos si algo falla a la mitad.
    suspend fun entregar(tarea: Tarea, fotos: List<String>, estudianteId: Long) {
        db.withTransaction {
            tareaDao.actualizar(tarea)
            // Por cada foto creo una fila en la tabla evidencias, enlazada a la tarea.
            fotos.forEach { uri ->
                evidenciaDao.insertar(
                    Evidencia(
                        tareaId = tarea.id, uri = uri,
                        fecha = tarea.fechaRealizacion ?: System.currentTimeMillis(),
                        subidaPorId = estudianteId
                    )
                )
            }
        }
    }

    // Revisión del docente: actualiza la tarea y deja el registro en el historial de validaciones.
    // También en transacción: el estado de la tarea y su registro de revisión siempre van juntos.
    suspend fun registrarValidacion(tarea: Tarea, validacion: Validacion) {
        db.withTransaction {
            tareaDao.actualizar(tarea)
            validacionDao.insertar(validacion)
        }
    }
}
