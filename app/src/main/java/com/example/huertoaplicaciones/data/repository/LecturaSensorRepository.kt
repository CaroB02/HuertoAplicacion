package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.entity.LecturaSensor
import com.example.huertoaplicaciones.data.remote.HuertoApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Las lecturas son simuladas (datos de prueba de ms-huerto); la app solo las consulta.
class LecturaSensorRepository(private val api: HuertoApi) {

    private val _lecturas = MutableStateFlow<List<LecturaSensor>>(emptyList())

    // La más reciente primero (el servidor ya las entrega ordenadas)
    fun obtenerTodas(): Flow<List<LecturaSensor>> = _lecturas.asStateFlow()

    // GET /lecturas
    suspend fun recargar() {
        _lecturas.value = api.obtenerLecturas()
    }
}
