package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.entity.LecturaSensor
import com.example.huertoaplicaciones.data.remote.HuertoApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LecturaSensorRepositoryTest {

    @Test
    fun recargarTraeLasLecturasSimuladas() = runTest {
        val api = mockk<HuertoApi>()
        val lectura = LecturaSensor(id = 1, sectorId = 1, fecha = 1L, humedadSuelo = 60,
            temperatura = 20.5, humedadAmbiente = 50, estado = "Normal")
        coEvery { api.obtenerLecturas() } returns listOf(lectura)
        val repository = LecturaSensorRepository(api)

        repository.recargar()

        assertEquals(listOf(lectura), repository.obtenerTodas().first())
    }
}
