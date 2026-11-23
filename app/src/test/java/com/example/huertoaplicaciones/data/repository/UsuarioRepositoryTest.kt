package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.DatosPrueba
import com.example.huertoaplicaciones.data.remote.LoginRequest
import com.example.huertoaplicaciones.data.remote.UsuarioRequest
import com.example.huertoaplicaciones.data.remote.UsuariosApi
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class UsuarioRepositoryTest {

    private val api = mockk<UsuariosApi>()
    private val repository = UsuarioRepository(api)

    @Test
    fun loginCorrectoDevuelveElUsuario() = runTest {
        coEvery { api.iniciarSesion(LoginRequest("matias.fuentes@huerta.cl", "123456")) } returns
            Response.success(DatosPrueba.estudiante)

        val usuario = repository.iniciarSesion("matias.fuentes@huerta.cl", "123456")

        assertEquals(DatosPrueba.estudiante, usuario)
    }

    @Test
    fun loginIncorrectoDevuelveNull() = runTest {
        // 401 = el correo o la clave no coinciden: no es un error de conexión, es un login fallido
        coEvery { api.iniciarSesion(any()) } returns DatosPrueba.error(401)

        assertNull(repository.iniciarSesion("x@huerta.cl", "mala"))
    }

    @Test(expected = HttpException::class)
    fun errorDelServidorEnElLoginLanzaExcepcion() = runTest {
        coEvery { api.iniciarSesion(any()) } returns DatosPrueba.error(500)

        repository.iniciarSesion("x@huerta.cl", "123456")
    }

    @Test
    fun obtenerEstudiantesFiltraPorPerfil() = runTest {
        coEvery { api.obtenerUsuarios() } returns
            listOf(DatosPrueba.docente, DatosPrueba.estudiante, DatosPrueba.otroEstudiante)
        repository.recargar()

        val estudiantes = repository.obtenerEstudiantes().first()

        assertEquals(listOf(DatosPrueba.estudiante, DatosPrueba.otroEstudiante), estudiantes)
        assertEquals(3, repository.obtenerTodos().first().size)
    }

    @Test
    fun registrarEnviaLaClave() = runTest {
        val enviado = slot<UsuarioRequest>()
        coEvery { api.registrar(capture(enviado)) } returns DatosPrueba.exito()

        repository.registrar(DatosPrueba.estudiante.copy(clave = "claveSegura1"))

        assertEquals("claveSegura1", enviado.captured.clave)
        assertEquals("ESTUDIANTE", enviado.captured.perfil)
    }

    @Test
    fun buscarPorCorreoYPorId() = runTest {
        coEvery { api.buscarPorCorreo("nadie@huerta.cl") } returns DatosPrueba.error(404)
        coEvery { api.obtenerUsuario(1) } returns Response.success(DatosPrueba.docente)

        assertNull(repository.obtenerPorCorreo("nadie@huerta.cl"))
        assertEquals(DatosPrueba.docente, repository.obtenerPorId(1))
    }
}
