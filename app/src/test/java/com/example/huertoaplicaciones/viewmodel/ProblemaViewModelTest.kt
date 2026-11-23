package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.DatosPrueba
import com.example.huertoaplicaciones.ReglaMainDispatcher
import com.example.huertoaplicaciones.data.repository.CultivoRepository
import com.example.huertoaplicaciones.data.repository.ProblemaRepository
import com.example.huertoaplicaciones.data.repository.SectorRepository
import com.example.huertoaplicaciones.data.repository.UsuarioRepository
import com.example.huertoaplicaciones.observar
import com.example.huertoaplicaciones.util.EstadoProblema
import com.example.huertoaplicaciones.util.TipoProblema
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ProblemaViewModelTest {

    @get:Rule
    val reglaMain = ReglaMainDispatcher()

    private val repository = mockk<ProblemaRepository>(relaxed = true)
    private val sectorRepository = mockk<SectorRepository>(relaxed = true)
    private val cultivoRepository = mockk<CultivoRepository>(relaxed = true)
    private val usuarioRepository = mockk<UsuarioRepository>(relaxed = true)

    private fun crearViewModel(): ProblemaViewModel {
        every { repository.obtenerTodos() } returns MutableStateFlow(listOf(DatosPrueba.problema))
        every { repository.obtenerPorUsuario(2) } returns MutableStateFlow(listOf(DatosPrueba.problema))
        every { sectorRepository.obtenerTodos() } returns MutableStateFlow(listOf(DatosPrueba.sector))
        every { cultivoRepository.obtenerTodos() } returns MutableStateFlow(listOf(DatosPrueba.cultivo))
        every { usuarioRepository.obtenerTodos() } returns MutableStateFlow(listOf(DatosPrueba.estudiante))
        coEvery { cultivoRepository.obtenerPorId(1) } returns DatosPrueba.cultivo
        return ProblemaViewModel(repository, sectorRepository, cultivoRepository, usuarioRepository)
    }

    private fun llenarFormulario(vm: ProblemaViewModel) {
        vm.nuevoReporte()
        vm.cambiarTipo(TipoProblema.PLAGA)
        vm.cambiarDescripcion("Pulgones en las hojas")
        vm.cambiarSector(1)
        vm.cambiarCultivo(1)
    }

    @Test
    fun reporteIncompletoNoSeEnvia() = runTest {
        val vm = crearViewModel()
        vm.nuevoReporte()

        vm.guardar(DatosPrueba.estudiante)

        val f = vm.form.value
        assertTrue(f.errorDescripcion && f.errorSector && f.errorCultivo)
        coVerify(exactly = 0) { repository.guardar(any()) }
    }

    @Test
    fun elCultivoDebePertenecerAlSector() = runTest {
        val vm = crearViewModel()
        coEvery { cultivoRepository.obtenerPorId(1) } returns DatosPrueba.cultivo.copy(sectorId = 9)
        llenarFormulario(vm)

        vm.guardar(DatosPrueba.estudiante)

        assertTrue(vm.form.value.errorCultivo)
        coVerify(exactly = 0) { repository.guardar(any()) }
    }

    @Test
    fun reporteValidoQuedaInformadoConSuFoto() = runTest {
        val vm = crearViewModel()
        llenarFormulario(vm)
        vm.agregarFoto("primera.jpg")
        vm.agregarFoto("segunda.jpg")   // solo se permite una: reemplaza a la anterior
        assertEquals(listOf("segunda.jpg"), vm.form.value.fotos)

        vm.guardar(DatosPrueba.estudiante)

        coVerify {
            repository.guardar(match {
                it.estado == EstadoProblema.INFORMADO && it.reportadoPorId == 2L && it.fotoUri == "segunda.jpg"
            })
        }
        assertTrue(vm.form.value.guardado)
    }

    @Test
    fun quitarFotoYErrorDeConexion() = runTest {
        coEvery { repository.guardar(any()) } throws IOException("sin red")
        val vm = crearViewModel()
        llenarFormulario(vm)
        vm.agregarFoto("foto.jpg")
        vm.quitarFoto("foto.jpg")
        assertTrue(vm.form.value.fotos.isEmpty())

        vm.guardar(DatosPrueba.estudiante)

        assertFalse(vm.form.value.guardado)
    }

    @Test
    fun soloElDocenteCambiaElEstadoYCerrarExigeComentario() = runTest {
        val vm = crearViewModel()

        assertFalse(vm.cambiarEstadoAtencion(DatosPrueba.problema, DatosPrueba.estudiante, EstadoProblema.CERRADO, "ok"))
        assertFalse(vm.cambiarEstadoAtencion(DatosPrueba.problema, DatosPrueba.docente, EstadoProblema.CERRADO, " "))
        assertTrue(vm.cambiarEstadoAtencion(DatosPrueba.problema, DatosPrueba.docente, EstadoProblema.CERRADO, "Se aplicó jabón potásico"))
        assertTrue(vm.cambiarEstadoAtencion(DatosPrueba.problema, DatosPrueba.docente, EstadoProblema.EN_REVISION, ""))

        coVerify { repository.actualizar(match { it.estado == EstadoProblema.CERRADO && it.fechaCierre != null }) }
        coVerify { repository.actualizar(match { it.estado == EstadoProblema.EN_REVISION && it.fechaCierre == null }) }
    }

    @Test
    fun errorAlActualizarNoCierraLaApp() = runTest {
        coEvery { repository.actualizar(any()) } throws IOException("sin red")
        val vm = crearViewModel()

        assertTrue(vm.cambiarEstadoAtencion(DatosPrueba.problema, DatosPrueba.docente, EstadoProblema.ATENDIDO, "Listo"))
    }

    @Test
    fun listasYRecarga() = runTest {
        val vm = crearViewModel()
        observar(vm.reportes)
        observar(vm.sectores)
        observar(vm.cultivos)
        observar(vm.usuarios)

        vm.recargar()

        assertEquals(1, vm.reportes.value.size)
        assertEquals(1, vm.sectores.value.size)
        assertEquals(1, vm.cultivos.value.size)
        assertEquals(1, vm.usuarios.value.size)
        assertEquals(listOf(DatosPrueba.problema), vm.reportesDe(2).first())
        coVerify { repository.recargar() }

        coEvery { usuarioRepository.recargar() } throws IOException("sin red")
        vm.recargar()
    }
}
