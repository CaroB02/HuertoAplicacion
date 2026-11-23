package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.DatosPrueba
import com.example.huertoaplicaciones.ReglaMainDispatcher
import com.example.huertoaplicaciones.data.entity.Validacion
import com.example.huertoaplicaciones.data.repository.ProblemaRepository
import com.example.huertoaplicaciones.data.repository.TareaRepository
import com.example.huertoaplicaciones.data.repository.UsuarioRepository
import com.example.huertoaplicaciones.observar
import com.example.huertoaplicaciones.util.EstadoProblema
import com.example.huertoaplicaciones.util.EstadoTarea
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Pruebas del historial: se arma juntando tareas, revisiones y reportes, del más reciente al más antiguo.
class HistorialViewModelTest {

    @get:Rule
    val reglaMain = ReglaMainDispatcher()

    private val tareaRepository = mockk<TareaRepository>(relaxed = true)
    private val problemaRepository = mockk<ProblemaRepository>(relaxed = true)
    private val usuarioRepository = mockk<UsuarioRepository>(relaxed = true)

    private fun crearViewModel(): HistorialViewModel {
        val entregada = DatosPrueba.tarea(id = 1, estado = EstadoTarea.RECHAZADA, asignadoA = 2)
            .copy(fechaCreacion = 100L, fechaRealizacion = 200L, comentarioEstudiante = "Listo")
        val deOtro = DatosPrueba.tarea(id = 2, asignadoA = 3).copy(fechaCreacion = 150L)
        every { tareaRepository.obtenerTodas() } returns MutableStateFlow(listOf(entregada, deOtro))
        every { tareaRepository.todasLasValidaciones() } returns MutableStateFlow(
            listOf(
                Validacion(id = 1, tareaId = 1, validadorId = 1, resultado = EstadoTarea.RECHAZADA,
                    comentario = "Falta una foto", fecha = 300L),
                Validacion(id = 2, tareaId = 2, validadorId = 1, resultado = EstadoTarea.VALIDADA, fecha = 50L)
            )
        )
        every { problemaRepository.obtenerTodos() } returns MutableStateFlow(
            listOf(DatosPrueba.problema.copy(fechaReporte = 400L, estado = EstadoProblema.CERRADO,
                fechaCierre = 500L, comentarioDocente = "Resuelto"))
        )
        every { usuarioRepository.obtenerTodos() } returns
            MutableStateFlow(listOf(DatosPrueba.docente, DatosPrueba.estudiante))
        return HistorialViewModel(tareaRepository, problemaRepository, usuarioRepository)
    }

    @Test
    fun elDocenteVeTodoOrdenadoDelMasRecienteAlMasAntiguo() = runTest {
        val vm = crearViewModel()
        observar(vm.eventos)

        vm.definirUsuario(DatosPrueba.docente)

        val fechas = vm.eventos.value.map { it.fecha }
        assertEquals(fechas.sortedDescending(), fechas)
        assertEquals(TipoEvento.PROBLEMA_ATENDIDO, vm.eventos.value.first().tipo)
        assertTrue(vm.eventos.value.any { it.tipo == TipoEvento.RECHAZO })
        assertTrue(vm.eventos.value.any { it.tipo == TipoEvento.VALIDACION })
    }

    @Test
    fun elEstudianteSoloVeLoSuyo() = runTest {
        val vm = crearViewModel()
        observar(vm.eventos)

        vm.definirUsuario(DatosPrueba.estudiante)

        // Su tarea creada y entregada, su rechazo, y su reporte con el cierre (no ve la tarea 2 de otro)
        val tipos = vm.eventos.value.map { it.tipo }
        assertEquals(5, tipos.size)
        assertTrue(TipoEvento.VALIDACION !in tipos)
    }

    @Test
    fun sinSesionNoHayHistorialYRecargar() = runTest {
        val vm = crearViewModel()
        observar(vm.eventos)
        vm.definirUsuario(null)
        assertTrue(vm.eventos.value.isEmpty())

        vm.recargar()
        coVerify { tareaRepository.recargar() }
        coVerify { problemaRepository.recargar() }
        coVerify { usuarioRepository.recargar() }

        coEvery { problemaRepository.recargar() } throws IOException("sin red")
        vm.recargar()
    }
}
