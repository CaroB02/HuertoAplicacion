package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.entity.Sector
import com.example.huertoaplicaciones.data.remote.HuertoApi
import com.example.huertoaplicaciones.data.remote.aRequest
import com.example.huertoaplicaciones.data.remote.comprobarRespuesta
import com.example.huertoaplicaciones.data.remote.cuerpoONulo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Repositorio de sectores: CRUD completo contra ms-huerto.
class SectorRepository(private val api: HuertoApi) {

    private val _sectores = MutableStateFlow<List<Sector>>(emptyList())

    // La pantalla observa esta lista; se actualiza cada vez que se llama a recargar()
    fun obtenerTodos(): Flow<List<Sector>> = _sectores.asStateFlow()

    // GET /sectores
    suspend fun recargar() {
        _sectores.value = api.obtenerSectores()
    }

    // GET /sectores/{id} (null si no existe)
    suspend fun obtenerPorId(id: Long): Sector? = cuerpoONulo(api.obtenerSector(id))

    // POST /sectores y después recargo para que la lista muestre el nuevo sector
    suspend fun guardar(sector: Sector) {
        comprobarRespuesta(api.crearSector(sector.aRequest()))
        recargar()
    }

    // PUT /sectores/{id}
    suspend fun actualizar(sector: Sector) {
        comprobarRespuesta(api.actualizarSector(sector.id, sector.aRequest()))
        recargar()
    }

    // DELETE /sectores/{id}. Devuelve false si el servidor responde 409 (tiene cultivos, tareas o reportes).
    suspend fun eliminar(sector: Sector): Boolean {
        val respuesta = api.eliminarSector(sector.id)
        if (respuesta.code() == 409) return false
        comprobarRespuesta(respuesta)
        recargar()
        return true
    }
}
