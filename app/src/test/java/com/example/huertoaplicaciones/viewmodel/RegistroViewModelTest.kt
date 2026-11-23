package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.DatosPrueba
import com.example.huertoaplicaciones.ReglaMainDispatcher
import com.example.huertoaplicaciones.data.repository.UsuarioRepository
import com.example.huertoaplicaciones.util.CODIGO_DOCENTE
import com.example.huertoaplicaciones.util.Perfil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class RegistroViewModelTest {

    @get:Rule
    val reglaMain = ReglaMainDispatcher()

    private val repository = mockk<UsuarioRepository>(relaxed = true)

    private fun formularioEstudiante(vm: RegistroViewModel) {
        vm.nuevo()
        vm.cambiarNombre("Ana  Rojas")
        vm.cambiarCorreo("Ana.Rojas@huerta.cl")
        vm.cambiarClave("claveSegura")
        vm.cambiarRepetirClave("claveSegura")
        vm.cambiarGrupo("Grupo D")
    }

    @Test
    fun formularioVacioMarcaErroresYNoRegistra() = runTest {
        val vm = RegistroViewModel(repository)
        vm.registrar()

        val e = vm.estado.value
        assertTrue(e.errorNombre && e.errorCorreo && e.errorClave && e.errorRepetir && e.errorGrupo)
        coVerify(exactly = 0) { repository.registrar(any()) }
    }

    @Test
    fun lasClavesDebenCoincidir() = runTest {
        val vm = RegistroViewModel(repository)
        vm.cambiarRepetirClave("otra")
        vm.cambiarClave("claveSegura")
        assertTrue(vm.estado.value.errorRepetir)
    }

    @Test
    fun docenteSinCodigoCorrectoNoSeRegistra() = runTest {
        val vm = RegistroViewModel(repository)
        formularioEstudiante(vm)
        vm.cambiarPerfil(Perfil.ADMIN)
        vm.cambiarCodigo("CUALQUIERA")

        vm.registrar()

        assertTrue(vm.estado.value.errorCodigo)
        coVerify(exactly = 0) { repository.registrar(any()) }
    }

    @Test
    fun correoRepetidoNoSeRegistra() = runTest {
        coEvery { repository.obtenerPorCorreo("ana.rojas@huerta.cl") } returns DatosPrueba.estudiante
        val vm = RegistroViewModel(repository)
        formularioEstudiante(vm)

        vm.registrar()

        assertTrue(vm.estado.value.correoRepetido)
        assertFalse(vm.estado.value.guardando)
        coVerify(exactly = 0) { repository.registrar(any()) }
    }

    @Test
    fun registroCorrectoNormalizaNombreYCorreo() = runTest {
        coEvery { repository.obtenerPorCorreo(any()) } returns null
        val vm = RegistroViewModel(repository)
        formularioEstudiante(vm)

        vm.registrar()

        coVerify {
            repository.registrar(match {
                it.correo == "ana.rojas@huerta.cl" && it.nombre == "Ana Rojas" && it.perfil == Perfil.ESTUDIANTE
            })
        }
        assertTrue(vm.estado.value.registrado)
    }

    @Test
    fun docenteConCodigoCorrectoRecibeGrupoPorDefecto() = runTest {
        coEvery { repository.obtenerPorCorreo(any()) } returns null
        val vm = RegistroViewModel(repository)
        formularioEstudiante(vm)
        vm.cambiarGrupo("")
        vm.cambiarPerfil(Perfil.ADMIN)
        vm.cambiarCodigo(CODIGO_DOCENTE)

        vm.registrar()

        coVerify { repository.registrar(match { it.perfil == Perfil.ADMIN && it.grupo == "Taller Huerto" }) }
    }

    @Test
    fun sinConexionNoSeQuedaGuardando() = runTest {
        coEvery { repository.obtenerPorCorreo(any()) } throws IOException("sin red")
        val vm = RegistroViewModel(repository)
        formularioEstudiante(vm)

        vm.registrar()

        assertFalse(vm.estado.value.guardando)
        assertFalse(vm.estado.value.registrado)
    }

    @Test
    fun errorDelServidorAlRegistrar() = runTest {
        coEvery { repository.obtenerPorCorreo(any()) } returns null
        coEvery { repository.registrar(any()) } throws IOException("sin red")
        val vm = RegistroViewModel(repository)
        formularioEstudiante(vm)

        vm.registrar()

        assertFalse(vm.estado.value.registrado)
        assertFalse(vm.estado.value.guardando)
    }
}
