package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.remote.ApiConfig
import com.example.huertoaplicaciones.data.remote.ClimaApi
import com.example.huertoaplicaciones.data.remote.ClimaRespuesta
import com.example.huertoaplicaciones.util.ClimaHuerto
import com.example.huertoaplicaciones.util.DiaPronostico
import com.example.huertoaplicaciones.util.describirCodigoClima
import com.example.huertoaplicaciones.util.etiquetaDia
import com.example.huertoaplicaciones.util.recomendacionRiego

// Repositorio de la API EXTERNA de clima. Es independiente de los microservicios propios:
// si Open-Meteo falla, el resto de la app sigue funcionando con normalidad.
class ClimaRepository(private val api: ClimaApi) {

    suspend fun obtenerClima(
        latitud: Double = ApiConfig.LATITUD,
        longitud: Double = ApiConfig.LONGITUD
    ): ClimaHuerto = convertirClima(api.obtenerPronostico(latitud, longitud))
}

// Convierte la respuesta de Open-Meteo en el modelo propio de la app (ClimaHuerto).
// Es una función aparte para poder probarla con un JSON de ejemplo, sin internet.
fun convertirClima(respuesta: ClimaRespuesta): ClimaHuerto {
    val actual = respuesta.current
        ?: throw IllegalStateException("La API de clima no envió el clima actual")
    val diario = respuesta.daily

    // Un DiaPronostico por cada fecha del pronóstico (posición 0 = hoy)
    val dias = if (diario == null) emptyList() else diario.time.indices.map { i ->
        DiaPronostico(
            etiqueta = etiquetaDia(i),
            maxima = diario.maximas.getOrNull(i) ?: 0.0,
            minima = diario.minimas.getOrNull(i) ?: 0.0,
            lluviaMm = diario.lluvia.getOrNull(i) ?: 0.0,
            probabilidadLluvia = diario.probabilidadLluvia.getOrNull(i) ?: 0,
            descripcion = describirCodigoClima(diario.codigos.getOrNull(i) ?: -1)
        )
    }

    val hoy = dias.firstOrNull()
    return ClimaHuerto(
        temperatura = actual.temperatura,
        humedad = actual.humedad,
        viento = actual.viento,
        descripcion = describirCodigoClima(actual.codigoClima),
        lluviaHoyMm = hoy?.lluviaMm ?: actual.precipitacion,
        probabilidadLluviaHoy = hoy?.probabilidadLluvia ?: 0,
        dias = dias,
        recomendacion = recomendacionRiego(
            lluviaMm = hoy?.lluviaMm ?: actual.precipitacion,
            probabilidadLluvia = hoy?.probabilidadLluvia ?: 0,
            temperaturaMaxima = hoy?.maxima ?: actual.temperatura
        )
    )
}
