package com.example.huertoaplicaciones.util

// Lo que la app muestra del clima. Es un modelo PROPIO de la app: así la pantalla no depende
// del formato de Open-Meteo (si mañana cambiamos de API externa, solo cambia el repositorio).
data class ClimaHuerto(
    val temperatura: Double,
    val humedad: Int,
    val viento: Double,
    val descripcion: String,
    val lluviaHoyMm: Double,
    val probabilidadLluviaHoy: Int,
    val dias: List<DiaPronostico>,
    val recomendacion: String
)

data class DiaPronostico(
    val etiqueta: String,           // "Hoy", "Mañana", "Pasado mañana"
    val maxima: Double,
    val minima: Double,
    val lluviaMm: Double,
    val probabilidadLluvia: Int,
    val descripcion: String
)

// Traduce el código del clima (estándar WMO que usa Open-Meteo) a un texto en español.
fun describirCodigoClima(codigo: Int): String = when (codigo) {
    0 -> "Despejado"
    1 -> "Mayormente despejado"
    2 -> "Parcialmente nublado"
    3 -> "Nublado"
    45, 48 -> "Niebla"
    51, 53, 55, 56, 57 -> "Llovizna"
    61, 63, 65, 66, 67 -> "Lluvia"
    71, 73, 75, 77, 85, 86 -> "Nieve"
    80, 81, 82 -> "Chubascos"
    95, 96, 99 -> "Tormenta eléctrica"
    else -> "Sin información"
}

// Recomendación de riego según el pronóstico de HOY. Así la API externa aporta algo útil al huerto.
fun recomendacionRiego(lluviaMm: Double, probabilidadLluvia: Int, temperaturaMaxima: Double): String = when {
    lluviaMm >= 5.0 || probabilidadLluvia >= 70 ->
        "Se espera lluvia hoy: pueden posponer el riego de los cultivos al aire libre."
    temperaturaMaxima >= 30.0 ->
        "Día caluroso: rieguen temprano o al atardecer y revisen la humedad del suelo."
    temperaturaMaxima <= 3.0 ->
        "Riesgo de helada: protejan los cultivos sensibles y eviten regar de noche."
    else ->
        "Sin lluvia importante: mantengan el riego según la frecuencia de cada cultivo."
}

// Nombre del día según su posición en el pronóstico
fun etiquetaDia(posicion: Int): String = when (posicion) {
    0 -> "Hoy"
    1 -> "Mañana"
    2 -> "Pasado mañana"
    else -> "Día ${posicion + 1}"
}
