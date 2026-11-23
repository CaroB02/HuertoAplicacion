package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.DatosPrueba
import com.example.huertoaplicaciones.data.remote.HuertoApi
import com.example.huertoaplicaciones.data.remote.SectorRequest
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
import retrofit2.HttpException
import java.io.IOException

// Pruebas del repositorio de sectores.
// Qué se MOCKEA: la interfaz HuertoApi (Retrofit). Con MockK creo una API falsa que responde lo que
// yo le indico, sin levantar el microservicio ni usar internet.
// Qué se PRUEBA: la lógica del repositorio (revisar el código HTTP, recargar la lista, 404 = null, 409 = false).
class SectorRepositoryTest {

    private val api = mockk<HuertoApi>()
    private val repository = SectorRepository(api)

    @Test
    fun recargarActualizaLaListaQueObservanLasPantallas() = runTest {
        coEvery { api.obtenerSectores() } returns listOf(DatosPrueba.sector)

        repository.recargar()

        assertEquals(listOf(DatosPrueba.sector), repository.obtenerTodos().first())
    }

    @Test
    fun guardarEnviaElRequestSinIdYRecargaLaLista() = runTest {
        val enviado = slot<SectorRequest>()
        coEvery { api.crearSector(capture(enviado)) } returns DatosPrueba.exito()
        coEvery { api.obtenerSectores() } returns listOf(DatosPrueba.sector)

        repository.guardar(DatosPrueba.sector)

        // Lo que viajó al servidor tiene los datos del sector (el id no se envía al crear)
        assertEquals("Huerta Norte", enviado.captured.nombre)
        assertEquals("S-01", enviado.captured.codigo)
        coVerify(exactly = 1) { api.obtenerSectores() }
        assertEquals(1, repository.obtenerTodos().first().size)
    }

    @Test
    fun actualizarUsaElIdDelSectorEnLaUrl() = runTest {
        coEvery { api.actualizarSector(1, any()) } returns DatosPrueba.exito()
        coEvery { api.obtenerSectores() } returns emptyList()

        repository.actualizar(DatosPrueba.sector)

        coVerify { api.actualizarSector(1, any()) }
    }

    @Test(expected = HttpException::class)
    fun siElServidorRespondeErrorSeLanzaHttpException() = runTest {
        coEvery { api.crearSector(any()) } returns DatosPrueba.error(400)

        repository.guardar(DatosPrueba.sector)
    }

    @Test
    fun eliminarDevuelveFalseCuandoElServidorRespondeConflicto() = runTest {
        coEvery { api.eliminarSector(1) } returns DatosPrueba.error(409)

        assertFalse(repository.eliminar(DatosPrueba.sector))
        // Si no se borró, no hace falta recargar
        coVerify(exactly = 0) { api.obtenerSectores() }
    }

    @Test
    fun eliminarDevuelveTrueYRecargaCuandoSeBorra() = runTest {
        coEvery { api.eliminarSector(1) } returns DatosPrueba.exito()
        coEvery { api.obtenerSectores() } returns emptyList()

        assertTrue(repository.eliminar(DatosPrueba.sector))
        coVerify(exactly = 1) { api.obtenerSectores() }
    }

    @Test
    fun obtenerPorIdDevuelveNullSiNoExiste() = runTest {
        coEvery { api.obtenerSector(99) } returns DatosPrueba.error(404)

        assertNull(repository.obtenerPorId(99))
    }

    @Test
    fun obtenerPorIdDevuelveElSector() = runTest {
        coEvery { api.obtenerSector(1) } returns retrofit2.Response.success(DatosPrueba.sector)

        assertEquals(DatosPrueba.sector, repository.obtenerPorId(1))
    }

    @Test(expected = IOException::class)
    fun sinConexionLaExcepcionLlegaAlViewModel() = runTest {
        coEvery { api.obtenerSectores() } throws IOException("sin red")

        repository.recargar()
    }
}
