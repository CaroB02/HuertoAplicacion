package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.DatosPrueba
import com.example.huertoaplicaciones.ReglaMainDispatcher
import com.example.huertoaplicaciones.data.entity.LecturaSensor
import com.example.huertoaplicaciones.data.repository.CultivoRepository
import com.example.huertoaplicaciones.data.repository.LecturaSensorRepository
import com.example.huertoaplicaciones.data.repository.SectorRepository
import com.example.huertoaplicaciones.escucharMensajes
import com.example.huertoaplicaciones.observar
import com.example.huertoaplicaciones.util.EstadoSector
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Pruebas del ViewModel de sectores (lista, detalle y formulario único crear/editar).
// Qué se MOCKEA: los tres repositorios (no hay servidor en la prueba).
// Qué se PRUEBA: validaciones, decisión POST vs PUT, estados cargando/guardando/guardado y errores.
class SectorViewModelTest {

    @get:Rule
    val reglaMain = ReglaMainDispatcher()

    private val repository = mockk<SectorRepository>(relaxed = true)
    private val cultivoRepository = mockk<CultivoRepository>(relaxed = true)
    private val lecturaRepository = mockk<LecturaSensorRepository>(relaxed = true)

    private val lectura = LecturaSensor(id = 1, sectorId = 1, fecha = 1L, humedadSuelo = 60,
        temperatura = 20.0, humedadAmbiente = 50, estado = "Normal")

    private fun crearViewModel(): SectorViewModel {
        every { repository.obtenerTodos() } returns MutableStateFlow(listOf(DatosPrueba.sector))
        every { cultivoRepository.obtenerPorSector(any()) } returns MutableStateFlow(listOf(DatosPrueba.cultivo))
        every { lecturaRepository.obtenerTodas() } returns MutableStateFlow(listOf(lectura))
        return SectorViewModel(repository, cultivoRepository, lecturaRepository)
    }

    private fun llenarFormulario(vm: SectorViewModel) {
        vm.cambiarCodigo("S-09")
        vm.cambiarNombre("Huerta Sur")
        vm.cambiarDescripcion("Zona nueva")
        vm.cambiarUbicacion("Patio sur")
        vm.cambiarEstado(EstadoSector.DISPONIBLE)
    }

    @Test
    fun guardarConCamposVaciosMarcaErroresYNoLlamaAlServidor() = runTest {
        val vm = crearViewModel()
        vm.nuevoSector()

        vm.guardar()

        val f = vm.form.value
        assertTrue(f.errorCodigo && f.errorNombre && f.errorDescripcion && f.errorUbicacion)
        coVerify(exactly = 0) { repository.guardar(any()) }
    }

    @Test
    fun sectorNuevoSeEnviaConPost() = runTest {
        val vm = crearViewModel()
        vm.nuevoSector()
        llenarFormulario(vm)

        vm.guardar()

        coVerify { repository.guardar(match { it.id == 0L && it.nombre == "Huerta Sur" && it.codigo == "S-09" }) }
        assertTrue(vm.form.value.guardado)
        assertFalse(vm.form.value.guardando)
    }

    @Test
    fun editarCargaElSectorYSeEnviaConPut() = runTest {
        coEvery { repository.obtenerPorId(1) } returns DatosPrueba.sector
        val vm = crearViewModel()

        vm.cargarSector(1)
        assertEquals("Huerta Norte", vm.form.value.nombre)
        assertFalse(vm.form.value.cargando)

        vm.cambiarNombre("Huerta Norte Ampliada")
        vm.guardar()

        coVerify { repository.actualizar(match { it.id == 1L && it.nombre == "Huerta Norte Ampliada" }) }
        coVerify(exactly = 0) { repository.guardar(any()) }
    }

    @Test
    fun siElServidorFallaNoSeMarcaGuardadoYSeAvisa() = runTest {
        coEvery { repository.guardar(any()) } throws IOException("sin red")
        val mensajes = escucharMensajes()
        val vm = crearViewModel()
        vm.nuevoSector()
        llenarFormulario(vm)

        vm.guardar()
        runCurrent()

        assertFalse(vm.form.value.guardado)
        assertFalse(vm.form.value.guardando)
        assertTrue(mensajes.last().esError)
        assertTrue(mensajes.last().texto.contains("No se pudo guardar el sector"))
    }

    @Test
    fun cargarUnSectorInexistenteOSinConexion() = runTest {
        coEvery { repository.obtenerPorId(50) } returns null
        coEvery { repository.obtenerPorId(60) } throws IOException("sin red")
        val vm = crearViewModel()

        vm.cargarSector(50)
        assertFalse(vm.form.value.cargando)

        vm.cargarSector(60)
        assertFalse(vm.form.value.cargando)
    }

    @Test
    fun eliminarSectorConAsociadosDevuelveFalse() = runTest {
        coEvery { repository.eliminar(any()) } returns false
        val vm = crearViewModel()
        var resultado: Boolean? = null

        vm.eliminarSector(DatosPrueba.sector) { resultado = it }

        assertEquals(false, resultado)
    }

    @Test
    fun eliminarSectorCorrectamenteOConError() = runTest {
        coEvery { repository.eliminar(DatosPrueba.sector) } returns true
        val vm = crearViewModel()
        var resultado: Boolean? = null
        vm.eliminarSector(DatosPrueba.sector) { resultado = it }
        assertEquals(true, resultado)

        coEvery { repository.eliminar(any()) } throws IOException("sin red")
        vm.eliminarSector(DatosPrueba.sector) { resultado = it }
        assertEquals(false, resultado)
    }

    @Test
    fun elDetalleMuestraSectorCultivosYLectura() = runTest {
        val vm = crearViewModel()
        observar(vm.sectorDetalle)
        observar(vm.cultivosDelSector)
        observar(vm.lecturaDelSector)

        vm.seleccionarSector(1)

        assertEquals(DatosPrueba.sector, vm.sectorDetalle.value)
        assertEquals(listOf(DatosPrueba.cultivo), vm.cultivosDelSector.value)
        assertEquals(lectura, vm.lecturaDelSector.value)
    }

    @Test
    fun recargarPideLosDatosYAvisaSiNoHayConexion() = runTest {
        val mensajes = escucharMensajes()
        val vm = crearViewModel()
        observar(vm.sectores)

        vm.recargar()
        coVerify { repository.recargar() }
        coVerify { cultivoRepository.recargar() }
        coVerify { lecturaRepository.recargar() }
        assertEquals(1, vm.sectores.value.size)

        coEvery { repository.recargar() } throws IOException("sin red")
        vm.recargar()
        runCurrent()
        assertTrue(mensajes.last().esError)
    }
}
