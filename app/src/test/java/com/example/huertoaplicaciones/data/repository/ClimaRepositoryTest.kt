package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.remote.ClimaActual
import com.example.huertoaplicaciones.data.remote.ClimaApi
import com.example.huertoaplicaciones.data.remote.ClimaDiario
import com.example.huertoaplicaciones.data.remote.ClimaRespuesta
import com.example.huertoaplicaciones.util.describirCodigoClima
import com.example.huertoaplicaciones.util.etiquetaDia
import com.example.huertoaplicaciones.util.recomendacionRiego
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de la API EXTERNA de clima: la conversión de la respuesta de Open-Meteo al modelo propio
// de la app y la recomendación de riego. No se usa internet: el JSON es un ejemplo fijo.
class ClimaRepositoryTest {

    // Un JSON con el mismo formato que entrega Open-Meteo
    private val jsonEjemplo = """
        {
          "current": {"time": "2026-11-14T13:00", "temperature_2m": 24.6, "relative_humidity_2m": 38,
                      "precipitation": 0.0, "weather_code": 1, "wind_speed_10m": 12.4},
          "daily": {"time": ["2026-11-14", "2026-11-15", "2026-11-16"],
                    "temperature_2m_max": [27.1, 29.0, 31.5],
                    "temperature_2m_min": [11.2, 12.0, 14.3],
                    "precipitation_sum": [0.0, 6.2, 0.0],
                    "precipitation_probability_max": [5, 80, null],
                    "weather_code": [1, 61, 0]}
        }
    """.trimIndent()

    @Test
    fun gsonLeeLosNombresDeOpenMeteoConSerializedName() {
        val respuesta = Gson().fromJson(jsonEjemplo, ClimaRespuesta::class.java)

        assertEquals(24.6, respuesta.current!!.temperatura, 0.001)
        assertEquals(38, respuesta.current!!.humedad)
        assertEquals(3, respuesta.daily!!.time.size)
    }

    @Test
    fun convertirClimaArmaElModeloDeLaApp() {
        val respuesta = Gson().fromJson(jsonEjemplo, ClimaRespuesta::class.java)

        val clima = convertirClima(respuesta)

        assertEquals("Mayormente despejado", clima.descripcion)
        assertEquals(3, clima.dias.size)
        assertEquals("Hoy", clima.dias[0].etiqueta)
        assertEquals("Lluvia", clima.dias[1].descripcion)
        // Un valor null en el JSON se muestra como 0 en vez de cerrar la app
        assertEquals(0, clima.dias[2].probabilidadLluvia)
        assertEquals(5, clima.probabilidadLluviaHoy)
    }

    @Test(expected = IllegalStateException::class)
    fun sinClimaActualSeConsideraUnError() {
        convertirClima(ClimaRespuesta(current = null, daily = null))
    }

    @Test
    fun sinPronosticoDiarioIgualMuestraElClimaActual() {
        val clima = convertirClima(ClimaRespuesta(current = ClimaActual(temperatura = 18.0, precipitacion = 7.0)))

        assertTrue(clima.dias.isEmpty())
        assertEquals(7.0, clima.lluviaHoyMm, 0.001)
        assertTrue(clima.recomendacion.contains("lluvia"))
    }

    @Test
    fun elRepositorioUsaLaApiYConvierte() = runTest {
        val api = mockk<ClimaApi>()
        coEvery { api.obtenerPronostico(any(), any(), any(), any(), any(), any()) } returns
            ClimaRespuesta(
                current = ClimaActual(temperatura = 20.0, codigoClima = 3),
                daily = ClimaDiario(time = listOf("2026-11-14"), maximas = listOf(22.0))
            )

        val clima = ClimaRepository(api).obtenerClima(-33.45, -70.66)

        assertEquals("Nublado", clima.descripcion)
        assertEquals(22.0, clima.dias[0].maxima, 0.001)
    }

    @Test
    fun recomendacionDeRiegoSegunElPronostico() {
        assertTrue(recomendacionRiego(8.0, 10, 20.0).contains("lluvia"))
        assertTrue(recomendacionRiego(0.0, 75, 20.0).contains("lluvia"))
        assertTrue(recomendacionRiego(0.0, 0, 32.0).contains("caluroso"))
        assertTrue(recomendacionRiego(0.0, 0, 2.0).contains("helada"))
        assertTrue(recomendacionRiego(0.0, 0, 20.0).contains("frecuencia"))
    }

    @Test
    fun codigosDelClimaYEtiquetasDeDias() {
        assertEquals("Despejado", describirCodigoClima(0))
        assertEquals("Niebla", describirCodigoClima(45))
        assertEquals("Llovizna", describirCodigoClima(53))
        assertEquals("Nieve", describirCodigoClima(73))
        assertEquals("Chubascos", describirCodigoClima(81))
        assertEquals("Tormenta eléctrica", describirCodigoClima(95))
        assertEquals("Sin información", describirCodigoClima(-1))
        assertEquals("Mañana", etiquetaDia(1))
        assertEquals("Pasado mañana", etiquetaDia(2))
        assertEquals("Día 4", etiquetaDia(3))
    }
}
