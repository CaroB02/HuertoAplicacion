package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.DatosPrueba
import com.example.huertoaplicaciones.ReglaMainDispatcher
import com.example.huertoaplicaciones.data.repository.UsuarioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Pruebas del login.
// Qué se MOCKEA: UsuarioRepository (así no se necesita ms-usuarios encendido).
// Qué se PRUEBA: las validaciones del formulario y cómo cambia el estado según la respuesta.
class AuthViewModelTest {

    @get:Rule
    val reglaMain = ReglaMainDispatcher()

    private val repository = mockk<UsuarioRepository>()

    @Test
    fun correoInvalidoMuestraErrorYNoConsultaAlServidor() = runTest {
        val vm = AuthViewModel(repository)
        vm.onEmailChanged("correo-sin-arroba")
        vm.onPasswordChanged("123456")

        vm.iniciarSesion()

        assertNotNull(vm.uiState.value.emailError)
        coVerify(exactly = 0) { repository.iniciarSesion(any(), any()) }
    }

    @Test
    fun camposVaciosMuestranSusErrores() = runTest {
        val vm = AuthViewModel(repository)
        vm.onEmailChanged("")
        vm.onPasswordChanged("")
        assertEquals("El correo electrónico no puede estar vacío", vm.uiState.value.emailError)
        assertEquals("La contraseña no puede estar vacía", vm.uiState.value.passwordError)

        vm.onEmailChanged("matias.fuentes@huerta.cl")
        vm.iniciarSesion()

        assertEquals("Ingrese la contraseña", vm.uiState.value.passwordError)
    }

    @Test
    fun loginCorrectoGuardaElUsuarioYUsaElCorreoEnMinuscula() = runTest {
        coEvery { repository.iniciarSesion("matias.fuentes@huerta.cl", "123456") } returns DatosPrueba.estudiante
        val vm = AuthViewModel(repository)
        vm.onEmailChanged("Matias.Fuentes@huerta.cl")
        vm.onPasswordChanged("123456")

        vm.iniciarSesion()

        assertEquals(DatosPrueba.estudiante, vm.uiState.value.usuarioLogueado)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun loginIncorrectoMuestraMensaje() = runTest {
        coEvery { repository.iniciarSesion(any(), any()) } returns null
        val vm = AuthViewModel(repository)
        vm.cargarCuentaPrueba("matias.fuentes@huerta.cl")

        vm.iniciarSesion()

        assertEquals("Correo o contraseña incorrectos", vm.uiState.value.errorMessage)
        assertNull(vm.uiState.value.usuarioLogueado)
    }

    @Test
    fun sinConexionAvisaYNoSeQuedaCargando() = runTest {
        coEvery { repository.iniciarSesion(any(), any()) } throws IOException("sin red")
        val vm = AuthViewModel(repository)
        vm.cargarCuentaPrueba("matias.fuentes@huerta.cl")

        vm.iniciarSesion()

        assertTrue(vm.uiState.value.errorMessage!!.contains("No se pudo conectar"))
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun cerrarSesionLimpiaElEstado() = runTest {
        coEvery { repository.iniciarSesion(any(), any()) } returns DatosPrueba.docente
        val vm = AuthViewModel(repository)
        vm.cargarCuentaPrueba("carolina.munoz@huerta.cl")
        vm.iniciarSesion()

        vm.cerrarSesion()

        assertNull(vm.uiState.value.usuarioLogueado)
        assertEquals("", vm.uiState.value.emailInput)
    }

    @Test
    fun laFabricaCreaElViewModel() {
        val vm = AuthViewModelFactory(repository).create(AuthViewModel::class.java)
        assertNotNull(vm)
    }
}
