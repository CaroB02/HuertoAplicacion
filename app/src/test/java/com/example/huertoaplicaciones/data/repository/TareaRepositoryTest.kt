package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.DatosPrueba
import com.example.huertoaplicaciones.data.entity.Evidencia
import com.example.huertoaplicaciones.data.entity.Validacion
import com.example.huertoaplicaciones.data.remote.EntregaRequest
import com.example.huertoaplicaciones.data.remote.TareasApi
import com.example.huertoaplicaciones.data.remote.ValidacionRequest
import com.example.huertoaplicaciones.util.EstadoTarea
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
import retrofit2.Response
import java.io.IOException

class TareaRepositoryTest {

    private val api = mockk<TareasApi>()
    private val repository = TareaRepository(api)

    private fun servidorConTareas() {
        coEvery { api.obtenerTareas() } returns listOf(
            DatosPrueba.tarea(id = 1, asignadoA = 2),
            DatosPrueba.tarea(id = 2, asignadoA = 3)
        )
        coEvery { api.obtenerValidaciones() } returns emptyList()
    }

    @Test
    fun recargarTraeTareasYValidaciones() = runTest {
        servidorConTareas()

        repository.recargar()

        assertEquals(2, repository.obtenerTodas().first().size)
        assertTrue(repository.todasLasValidaciones().first().isEmpty())
    }

    @Test
    fun cadaEstudianteVeSoloSusTareas() = runTest {
        servidorConTareas()
        repository.recargar()

        val deMatias = repository.obtenerPorEstudiante(2).first()

        assertEquals(1, deMatias.size)
        assertEquals(2L, deMatias.first().asignadoAId)
    }

    @Test
    fun entregarEnviaComentarioFotosYEstudiante() = runTest {
        servidorConTareas()
        val enviada = slot<EntregaRequest>()
        coEvery { api.entregarTarea(10, capture(enviada)) } returns DatosPrueba.exito()
        val tarea = DatosPrueba.tarea(estado = EstadoTarea.EN_DESARROLLO)
            .copy(comentarioEstudiante = "Listo", fechaRealizacion = 123L)

        repository.entregar(tarea, listOf("foto1.jpg", "foto2.jpg"), 2)

        assertEquals(listOf("foto1.jpg", "foto2.jpg"), enviada.captured.fotos)
        assertEquals("Listo", enviada.captured.comentario)
        assertEquals(2L, enviada.captured.estudianteId)
        assertEquals(123L, enviada.captured.fechaRealizacion)
    }

    @Test
    fun registrarValidacionEnviaElResultado() = runTest {
        servidorConTareas()
        val enviada = slot<ValidacionRequest>()
        coEvery { api.validarTarea(10, capture(enviada)) } returns DatosPrueba.exito()
        val validacion = Validacion(
            tareaId = 10, validadorId = 1, resultado = EstadoTarea.RECHAZADA,
            comentario = "Falta una foto", fecha = 5L
        )

        repository.registrarValidacion(DatosPrueba.tarea(), validacion)

        assertEquals(EstadoTarea.RECHAZADA, enviada.captured.resultado)
        assertEquals("Falta una foto", enviada.captured.comentario)
        coVerify { api.obtenerTareas() }
    }

    @Test
    fun guardarActualizarYEliminar() = runTest {
        servidorConTareas()
        coEvery { api.crearTarea(any()) } returns DatosPrueba.exito()
        coEvery { api.actualizarTarea(10, any()) } returns DatosPrueba.exito()
        coEvery { api.eliminarTarea(10) } returns DatosPrueba.exito()
        coEvery { api.eliminarTarea(11) } returns DatosPrueba.error(409)

        repository.guardar(DatosPrueba.tarea())
        repository.actualizar(DatosPrueba.tarea())
        assertTrue(repository.eliminar(DatosPrueba.tarea(id = 10)))
        assertFalse(repository.eliminar(DatosPrueba.tarea(id = 11)))
    }

    @Test
    fun evidenciasYValidacionesDeUnaTarea() = runTest {
        val foto = Evidencia(id = 1, tareaId = 10, uri = "foto.jpg", fecha = 1L, subidaPorId = 2)
        coEvery { api.obtenerEvidencias(10) } returns listOf(foto)
        coEvery { api.obtenerValidacionesDeTarea(10) } returns emptyList()

        assertEquals(listOf(foto), repository.evidenciasDe(10).first())
        assertTrue(repository.validacionesDe(10).first().isEmpty())
    }

    @Test
    fun sinConexionLasFotosSeMuestranVaciasEnVezDeCerrarLaApp() = runTest {
        coEvery { api.obtenerEvidencias(10) } throws IOException("sin red")

        assertTrue(repository.evidenciasDe(10).first().isEmpty())
    }

    @Test
    fun obtenerPorId() = runTest {
        coEvery { api.obtenerTarea(10) } returns Response.success(DatosPrueba.tarea())
        coEvery { api.obtenerTarea(99) } returns DatosPrueba.error(404)

        assertEquals(10L, repository.obtenerPorId(10)?.id)
        assertNull(repository.obtenerPorId(99))
    }
}
