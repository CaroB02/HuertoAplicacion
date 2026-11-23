package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.DatosPrueba
import com.example.huertoaplicaciones.data.remote.CultivoRequest
import com.example.huertoaplicaciones.data.remote.HuertoApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CultivoRepositoryTest {

    private val api = mockk<HuertoApi>()
    private val repository = CultivoRepository(api)

    private val tomate = DatosPrueba.cultivo.copy(id = 2, nombre = "Tomate", sectorId = 2)

    @Test
    fun obtenerPorSectorFiltraLosCultivosDeEseSector() = runTest {
        coEvery { api.obtenerCultivos() } returns listOf(DatosPrueba.cultivo, tomate)
        repository.recargar()

        val delSector2 = repository.obtenerPorSector(2).first()

        assertEquals(listOf(tomate), delSector2)
        assertEquals(2, repository.obtenerTodos().first().size)
    }

    @Test
    fun guardarEnviaLasFechasYLaFrecuencia() = runTest {
        val enviado = slot<CultivoRequest>()
        coEvery { api.crearCultivo(capture(enviado)) } returns DatosPrueba.exito()
        coEvery { api.obtenerCultivos() } returns emptyList()

        repository.guardar(DatosPrueba.cultivo)

        assertEquals(DatosPrueba.cultivo.fechaSiembra, enviado.captured.fechaSiembra)
        assertEquals(2, enviado.captured.frecuenciaRiegoDias)
        assertNull(enviado.captured.fechaTrasplante)
    }

    @Test
    fun actualizarLlamaAlPut() = runTest {
        coEvery { api.actualizarCultivo(1, any()) } returns DatosPrueba.exito()
        coEvery { api.obtenerCultivos() } returns emptyList()

        repository.actualizar(DatosPrueba.cultivo)

        coVerify { api.actualizarCultivo(1, any()) }
    }

    @Test
    fun eliminarRespetaElConflictoDelServidor() = runTest {
        coEvery { api.eliminarCultivo(1) } returns DatosPrueba.error(409)
        coEvery { api.eliminarCultivo(2) } returns DatosPrueba.exito()
        coEvery { api.obtenerCultivos() } returns emptyList()

        assertFalse(repository.eliminar(DatosPrueba.cultivo))
        assertTrue(repository.eliminar(tomate))
    }

    @Test
    fun obtenerPorIdDevuelveNullSiNoExiste() = runTest {
        coEvery { api.obtenerCultivo(7) } returns DatosPrueba.error(404)

        assertNull(repository.obtenerPorId(7))
    }
}
