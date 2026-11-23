package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.entity.Problema
import com.example.huertoaplicaciones.data.remote.TareasApi
import com.example.huertoaplicaciones.data.remote.aRequest
import com.example.huertoaplicaciones.data.remote.comprobarRespuesta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

// Repositorio de reportes de problemas (viven en ms-tareas).
class ProblemaRepository(private val api: TareasApi) {

    private val _problemas = MutableStateFlow<List<Problema>>(emptyList())

    // Todos los reportes (los ve el docente), el más reciente primero
    fun obtenerTodos(): Flow<List<Problema>> = _problemas.asStateFlow()

    // Solo los reportes de un usuario: cada estudiante ve únicamente los suyos
    fun obtenerPorUsuario(id: Long): Flow<List<Problema>> =
        _problemas.map { lista -> lista.filter { it.reportadoPorId == id } }

    // GET /problemas
    suspend fun recargar() {
        _problemas.value = api.obtenerProblemas()
    }

    // POST /problemas
    suspend fun guardar(problema: Problema) {
        comprobarRespuesta(api.crearProblema(problema.aRequest()))
        recargar()
    }

    // PUT /problemas/{id}: el docente cambia el estado de atención
    suspend fun actualizar(problema: Problema) {
        comprobarRespuesta(api.actualizarProblema(problema.id, problema.aRequest()))
        recargar()
    }
}
