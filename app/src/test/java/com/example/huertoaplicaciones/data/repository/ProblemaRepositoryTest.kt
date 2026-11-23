package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.DatosPrueba
import com.example.huertoaplicaciones.data.remote.ProblemaRequest
import com.example.huertoaplicaciones.data.remote.TareasApi
import com.example.huertoaplicaciones.util.EstadoProblema
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProblemaRepositoryTest {

    private val api = mockk<TareasApi>()
    private val repository = ProblemaRepository(api)

    @Test
    fun cadaUsuarioVeSoloSusReportes() = runTest {
        coEvery { api.obtenerProblemas() } returns listOf(
            DatosPrueba.problema,
            DatosPrueba.problema.copy(id = 6, reportadoPorId = 3)
        )
        repository.recargar()

        assertEquals(2, repository.obtenerTodos().first().size)
        assertEquals(listOf(DatosPrueba.problema), repository.obtenerPorUsuario(2).first())
    }

    @Test
    fun guardarYActualizarEnvianElReporte() = runTest {
        val enviado = slot<ProblemaRequest>()
        coEvery { api.crearProblema(any()) } returns DatosPrueba.exito()
        coEvery { api.actualizarProblema(5, capture(enviado)) } returns DatosPrueba.exito()
        coEvery { api.obtenerProblemas() } returns emptyList()

        repository.guardar(DatosPrueba.problema)
        repository.actualizar(DatosPrueba.problema.copy(estado = EstadoProblema.CERRADO))

        assertEquals(EstadoProblema.CERRADO, enviado.captured.estado)
        coVerify(exactly = 2) { api.obtenerProblemas() }
    }
}
