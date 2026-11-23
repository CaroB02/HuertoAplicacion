package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.DatosPrueba
import com.example.huertoaplicaciones.ReglaMainDispatcher
import com.example.huertoaplicaciones.data.repository.CultivoRepository
import com.example.huertoaplicaciones.data.repository.SectorRepository
import com.example.huertoaplicaciones.observar
import com.example.huertoaplicaciones.util.EstadoCultivo
import com.example.huertoaplicaciones.util.Limites
import com.example.huertoaplicaciones.util.formatearFecha
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Pruebas del ViewModel de cultivos: las validaciones de fechas son las más delicadas del proyecto.
class CultivoViewModelTest {

    @get:Rule
    val reglaMain = ReglaMainDispatcher()

    private val repository = mockk<CultivoRepository>(relaxed = true)
    private val sectorRepository = mockk<SectorRepository>(relaxed = true)

    private fun crearViewModel(): CultivoViewModel {
        every { repository.obtenerTodos() } returns MutableStateFlow(listOf(DatosPrueba.cultivo))
        every { sectorRepository.obtenerTodos() } returns MutableStateFlow(listOf(DatosPrueba.sector))
        return CultivoViewModel(repository, sectorRepository)
    }

    private fun llenarFormulario(vm: CultivoViewModel, siembra: String = "01/09/2026", cosecha: String = "01/12/2026") {
        vm.nuevoCultivo()
        vm.cambiarNombre("Espinaca")
        vm.cambiarDescripcion("Espinaca de hoja lisa")
        vm.cambiarSector(1)
        vm.cambiarFechaSiembra(siembra)
        vm.cambiarFechaCosecha(cosecha)
        vm.cambiarFrecuencia("3")
        vm.cambiarEstado(EstadoCultivo.SEMBRADO)
    }

    @Test
    fun laCosechaNoPuedeSerAntesDeLaSiembra() = runTest {
        val vm = crearViewModel()
        llenarFormulario(vm, siembra = "10/10/2026", cosecha = "01/10/2026")

        vm.guardar()

        assertTrue(vm.form.value.errorFechaCosecha)
        coVerify(exactly = 0) { repository.guardar(any()) }
    }

    @Test
    fun elTrasplanteDebeQuedarEntreSiembraYCosecha() = runTest {
        val vm = crearViewModel()
        llenarFormulario(vm)

        vm.cambiarFechaTrasplante("15/12/2026")
        assertTrue(vm.form.value.errorFechaTrasplante)

        vm.cambiarFechaTrasplante("01/08/2026")
        assertTrue(vm.form.value.errorFechaTrasplante)

        vm.cambiarFechaTrasplante("01/10/2026")
        assertFalse(vm.form.value.errorFechaTrasplante)

        vm.cambiarFechaTrasplante("no-es-fecha")
        assertTrue(vm.form.value.errorFechaTrasplante)
    }

    @Test
    fun cambiarLaSiembraRevisaLasFechasQueDependenDeElla() = runTest {
        val vm = crearViewModel()
        llenarFormulario(vm)

        vm.cambiarFechaSiembra("15/12/2026")

        assertTrue(vm.form.value.errorFechaCosecha)
    }

    @Test
    fun frecuenciaYCamposObligatorios() = runTest {
        val vm = crearViewModel()
        vm.nuevoCultivo()
        vm.cambiarFrecuencia("0")
        assertTrue(vm.form.value.errorFrecuencia)

        vm.guardar()

        val f = vm.form.value
        assertTrue(f.errorNombre && f.errorDescripcion && f.errorSector && f.errorFechaSiembra)
    }

    @Test
    fun cultivoValidoSeEnviaConLasFechasConvertidas() = runTest {
        val vm = crearViewModel()
        llenarFormulario(vm)
        vm.cambiarObservaciones("x".repeat(400))
        vm.cambiarFoto("foto.jpg")

        vm.guardar()

        coVerify {
            repository.guardar(match {
                it.nombre == "Espinaca" && it.frecuenciaRiegoDias == 3 && it.fechaTrasplante == null &&
                    it.observaciones.length == Limites.COMENTARIO && it.fotoUri == "foto.jpg"
            })
        }
        assertTrue(vm.form.value.guardado)
    }

    @Test
    fun editarCargaElCultivoComoTextoYHacePut() = runTest {
        coEvery { repository.obtenerPorId(1) } returns DatosPrueba.cultivo
        val vm = crearViewModel()

        vm.cargarCultivo(1)

        assertEquals("Lechuga", vm.form.value.nombre)
        assertEquals(formatearFecha(DatosPrueba.cultivo.fechaSiembra), vm.form.value.fechaSiembra)
        assertEquals("", vm.form.value.fechaTrasplante)
        assertEquals("2", vm.form.value.frecuenciaRiego)

        vm.guardar()
        coVerify { repository.actualizar(match { it.id == 1L }) }
    }

    @Test
    fun errorAlCargarOGuardarNoCierraLaApp() = runTest {
        coEvery { repository.obtenerPorId(any()) } throws IOException("sin red")
        coEvery { repository.guardar(any()) } throws IOException("sin red")
        val vm = crearViewModel()

        vm.cargarCultivo(1)
        llenarFormulario(vm)
        vm.guardar()

        assertFalse(vm.form.value.guardado)
    }

    @Test
    fun eliminarCultivo() = runTest {
        coEvery { repository.obtenerPorId(1) } returns DatosPrueba.cultivo
        coEvery { repository.eliminar(DatosPrueba.cultivo) } returns true
        coEvery { repository.obtenerPorId(2) } returns null
        coEvery { repository.obtenerPorId(3) } throws IOException("sin red")
        val vm = crearViewModel()
        val resultados = mutableListOf<Boolean>()

        vm.eliminarCultivo(1) { resultados.add(it) }
        vm.eliminarCultivo(2) { resultados.add(it) }
        vm.eliminarCultivo(3) { resultados.add(it) }

        assertEquals(listOf(true, false, false), resultados)
    }

    @Test
    fun recargarYListas() = runTest {
        val vm = crearViewModel()
        observar(vm.cultivos)
        observar(vm.sectores)

        vm.recargar()

        coVerify { repository.recargar() }
        coVerify { sectorRepository.recargar() }
        assertEquals(1, vm.cultivos.value.size)
        assertEquals(1, vm.sectores.value.size)

        coEvery { sectorRepository.recargar() } throws IOException("sin red")
        vm.recargar()   // no debe lanzar
    }
}
