package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.data.remote.HuertoApi
import com.example.huertoaplicaciones.data.remote.aRequest
import com.example.huertoaplicaciones.data.remote.comprobarRespuesta
import com.example.huertoaplicaciones.data.remote.cuerpoONulo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

// Repositorio de cultivos: CRUD completo contra ms-huerto.
class CultivoRepository(private val api: HuertoApi) {

    private val _cultivos = MutableStateFlow<List<Cultivo>>(emptyList())

    fun obtenerTodos(): Flow<List<Cultivo>> = _cultivos.asStateFlow()

    // Solo los cultivos de un sector (detalle del sector): filtro la lista que ya tengo
    fun obtenerPorSector(sectorId: Long): Flow<List<Cultivo>> =
        _cultivos.map { lista -> lista.filter { it.sectorId == sectorId } }

    // GET /cultivos
    suspend fun recargar() {
        _cultivos.value = api.obtenerCultivos()
    }

    suspend fun obtenerPorId(id: Long): Cultivo? = cuerpoONulo(api.obtenerCultivo(id))

    // POST /cultivos
    suspend fun guardar(cultivo: Cultivo) {
        comprobarRespuesta(api.crearCultivo(cultivo.aRequest()))
        recargar()
    }

    // PUT /cultivos/{id}
    suspend fun actualizar(cultivo: Cultivo) {
        comprobarRespuesta(api.actualizarCultivo(cultivo.id, cultivo.aRequest()))
        recargar()
    }

    // DELETE /cultivos/{id}. false si el cultivo tiene tareas o reportes (409).
    suspend fun eliminar(cultivo: Cultivo): Boolean {
        val respuesta = api.eliminarCultivo(cultivo.id)
        if (respuesta.code() == 409) return false
        comprobarRespuesta(respuesta)
        recargar()
        return true
    }
}
