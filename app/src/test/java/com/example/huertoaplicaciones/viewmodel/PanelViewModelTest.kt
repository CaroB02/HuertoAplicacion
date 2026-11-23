package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.DatosPrueba
import com.example.huertoaplicaciones.ReglaMainDispatcher
import com.example.huertoaplicaciones.data.repository.CultivoRepository
import com.example.huertoaplicaciones.data.repository.LecturaSensorRepository
import com.example.huertoaplicaciones.data.repository.ProblemaRepository
import com.example.huertoaplicaciones.data.repository.SectorRepository
import com.example.huertoaplicaciones.data.repository.TareaRepository
import com.example.huertoaplicaciones.data.repository.UsuarioRepository
import com.example.huertoaplicaciones.escucharMensajes
import com.example.huertoaplicaciones.observar
import com.example.huertoaplicaciones.util.EstadoTarea
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Pruebas del panel (dashboard): cada rol ve un resumen distinto.
class PanelViewModelTest {

    @get:Rule
    val reglaMain = ReglaMainDispatcher()

    private val tareaRepository = mockk<TareaRepository>(relaxed = true)
    private val cultivoRepository = mockk<CultivoRepository>(relaxed = true)
    private val problemaRepository = mockk<ProblemaRepository>(relaxed = true)
    private val usuarioRepository = mockk<UsuarioRepository>(relaxed = true)
    private val sectorRepository = mockk<SectorRepository>(relaxed = true)
    private val lecturaRepository = mockk<LecturaSensorRepository>(relaxed = true)

    private fun crearViewModel(): PanelViewModel {
        every { tareaRepository.obtenerTodas() } returns MutableStateFlow(
            listOf(
                DatosPrueba.tarea(id = 1, estado = EstadoTarea.VALIDADA, asignadoA = 2),
                DatosPrueba.tarea(id = 2, estado = EstadoTarea.PENDIENTE_VALIDACION, asignadoA = 3),
                DatosPrueba.tarea(id = 3, estado = EstadoTarea.PENDIENTE, asignadoA = 3)
            )
        )
        every { cultivoRepository.obtenerTodos() } returns MutableStateFlow(listOf(DatosPrueba.cultivo))
        every { problemaRepository.obtenerTodos() } returns MutableStateFlow(listOf(DatosPrueba.problema))
        every { usuarioRepository.obtenerTodos() } returns
            MutableStateFlow(listOf(DatosPrueba.docente, DatosPrueba.estudiante))
        every { sectorRepository.obtenerTodos() } returns MutableStateFlow(listOf(DatosPrueba.sector))
        every { lecturaRepository.obtenerTodas() } returns MutableStateFlow(emptyList())
        return PanelViewModel(
            tareaRepository, cultivoRepository, problemaRepository,
            usuarioRepository, sectorRepository, lecturaRepository
        )
    }

    @Test
    fun elDocenteVeElResumenDeTodasLasTareas() = runTest {
        val vm = crearViewModel()
        observar(vm.estado)

        vm.definirUsuario(DatosPrueba.docente)

        val resumen = vm.estado.value.resumen
        assertEquals(3, resumen.total)
        assertEquals(1, resumen.validadas)
        assertEquals(1, resumen.porValidar)
        assertTrue(vm.estado.value.avisos.isNotEmpty())
    }

    @Test
    fun elEstudianteSoloVeSusTareas() = runTest {
        val vm = crearViewModel()
        observar(vm.estado)

        vm.definirUsuario(DatosPrueba.estudiante)

        assertEquals(1, vm.estado.value.resumen.total)
        assertEquals(1, vm.estado.value.resumen.validadas)
    }

    @Test
    fun sinSesionElPanelQuedaVacio() = runTest {
        val vm = crearViewModel()
        observar(vm.estado)

        vm.definirUsuario(null)

        assertEquals(0, vm.estado.value.resumen.total)
    }

    @Test
    fun recargarPideLosDatosDeLosTresMicroservicios() = runTest {
        val mensajes = escucharMensajes()
        val vm = crearViewModel()

        vm.recargar()
        coVerify { tareaRepository.recargar() }
        coVerify { cultivoRepository.recargar() }
        coVerify { problemaRepository.recargar() }
        coVerify { usuarioRepository.recargar() }
        coVerify { sectorRepository.recargar() }
        coVerify { lecturaRepository.recargar() }

        coEvery { tareaRepository.recargar() } throws IOException("sin red")
        vm.recargar()
        runCurrent()
        assertTrue(mensajes.last().esError)
    }
}
