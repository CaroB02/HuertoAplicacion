package com.example.huertoaplicaciones.data.remote

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Comprueba que RetrofitClient crea las 4 Api (3 microservicios + clima)
// sin conectarse a internet: crear la Api no hace ninguna llamada HTTP.
class RetrofitClientTest {

    @Test
    fun creaUnaApiPorCadaServicio() {
        assertNotNull(RetrofitClient.usuariosApi)
        assertNotNull(RetrofitClient.huertoApi)
        assertNotNull(RetrofitClient.tareasApi)
        assertNotNull(RetrofitClient.climaApi)
    }

    @Test
    fun lasDireccionesApuntanAlEmuladorYAOpenMeteo() {
        assertTrue(ApiConfig.URL_USUARIOS.endsWith(":8081/"))
        assertTrue(ApiConfig.URL_HUERTO.endsWith(":8082/"))
        assertTrue(ApiConfig.URL_TAREAS.endsWith(":8083/"))
        assertTrue(ApiConfig.URL_CLIMA.startsWith("https://"))
    }
}
