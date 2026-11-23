package com.example.huertoaplicaciones.data.remote

import com.example.huertoaplicaciones.DatosPrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

// Pruebas de las funciones que revisan las respuestas HTTP y arman los mensajes de error.
class RespuestasTest {

    @Test
    fun usaElMensajeQueEnviaElServidor() {
        val error = HttpException(DatosPrueba.error<Void>(409, """{"mensaje":"Solo se pueden eliminar tareas pendientes"}"""))

        assertEquals("Solo se pueden eliminar tareas pendientes", describirError(error))
    }

    @Test
    fun sinMensajeDelServidorUsaUnTextoSegunElCodigo() {
        assertTrue(describirError(HttpException(DatosPrueba.error<Void>(400))).contains("rechazó"))
        assertEquals("No autorizado", describirError(HttpException(DatosPrueba.error<Void>(401))))
        assertTrue(describirError(HttpException(DatosPrueba.error<Void>(404))).contains("No se encontró"))
        assertTrue(describirError(HttpException(DatosPrueba.error<Void>(409))).contains("conflicto"))
        assertTrue(describirError(HttpException(DatosPrueba.error<Void>(500))).contains("500"))
        // Un cuerpo que no es JSON no debe cerrar la app
        assertTrue(describirError(HttpException(DatosPrueba.error<Void>(500, "texto plano"))).contains("500"))
    }

    @Test
    fun sinConexionExplicaQueRevisenLosMicroservicios() {
        assertTrue(describirError(IOException("timeout")).contains("No se pudo conectar"))
        assertEquals("algo raro", describirError(IllegalStateException("algo raro")))
        assertEquals("Error inesperado", describirError(RuntimeException()))
    }

    @Test
    fun cuerpoONuloYComprobarRespuesta() {
        assertNull(cuerpoONulo(DatosPrueba.error<String>(404)))
        assertEquals("hola", cuerpoONulo(Response.success("hola")))
        comprobarRespuesta(DatosPrueba.exito())   // no lanza nada
    }

    @Test(expected = HttpException::class)
    fun comprobarRespuestaLanzaConErrores() {
        comprobarRespuesta(DatosPrueba.error<Void>(503))
    }
}
