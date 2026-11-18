package com.example.huertoaplicaciones.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// API EXTERNA: pronóstico del clima de Open-Meteo (https://open-meteo.com). Es gratuita y sin clave.
// A diferencia de los microservicios, esta API NO es nuestra: solo la consultamos (GET) y
// no podemos cambiar su formato. Por eso usamos @SerializedName para traducir sus nombres
// en inglés con guion bajo (temperature_2m) a nombres en español.
//
// Ejemplo de la URL que arma Retrofit:
// https://api.open-meteo.com/v1/forecast?latitude=-33.45&longitude=-70.66&current=temperature_2m,...
interface ClimaApi {

    @GET("v1/forecast")
    suspend fun obtenerPronostico(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") actual: String =
            "temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m",
        @Query("daily") diario: String =
            "temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max,weather_code",
        @Query("timezone") zonaHoraria: String = "America/Santiago",
        @Query("forecast_days") dias: Int = 3
    ): ClimaRespuesta
}

// Respuesta completa de Open-Meteo (solo los campos que usamos)
data class ClimaRespuesta(
    val current: ClimaActual? = null,
    val daily: ClimaDiario? = null
)

// El clima de este momento
data class ClimaActual(
    val time: String = "",
    @SerializedName("temperature_2m") val temperatura: Double = 0.0,
    @SerializedName("relative_humidity_2m") val humedad: Int = 0,
    @SerializedName("precipitation") val precipitacion: Double = 0.0,
    @SerializedName("weather_code") val codigoClima: Int = 0,
    @SerializedName("wind_speed_10m") val viento: Double = 0.0
)

// El pronóstico por día: cada lista tiene un valor por día (posición 0 = hoy, 1 = mañana...)
data class ClimaDiario(
    val time: List<String> = emptyList(),
    @SerializedName("temperature_2m_max") val maximas: List<Double?> = emptyList(),
    @SerializedName("temperature_2m_min") val minimas: List<Double?> = emptyList(),
    @SerializedName("precipitation_sum") val lluvia: List<Double?> = emptyList(),
    @SerializedName("precipitation_probability_max") val probabilidadLluvia: List<Int?> = emptyList(),
    @SerializedName("weather_code") val codigos: List<Int?> = emptyList()
)
