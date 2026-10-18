package com.example.huertoaplicaciones.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Funciones para convertir fechas. En la base guardo las fechas como Long (milisegundos desde 1970),
// pero el usuario las escribe y lee como texto "dd/mm/aaaa". Estas funciones traducen entre las dos.

private const val FORMATO = "dd/MM/yyyy"
private const val FORMATO_HORA = "dd/MM/yyyy HH:mm"
// Expresión regular: exactamente 2 dígitos / 2 dígitos / 4 dígitos.
private val PATRON = Regex("""\d{2}/\d{2}/\d{4}""")

// Cuántos milisegundos tiene un día (24 h x 60 min x 60 s x 1000 ms). Sirve para sumar y restar días.
const val MILIS_POR_DIA = 24L * 60 * 60 * 1000

// Devuelve los milisegundos de la fecha, o null si el texto no es una fecha válida
fun parsearFecha(texto: String): Long? {
    val limpio = texto.trim()
    // Primero reviso el formato con la expresión regular.
    if (!PATRON.matches(limpio)) return null
    return try {
        val formato = SimpleDateFormat(FORMATO, Locale.getDefault())
        // isLenient = false: rechaza fechas que no existen (por ejemplo 31/02/2026).
        formato.isLenient = false
        formato.parse(limpio)?.time
    } catch (e: Exception) {
        null
    }
}

// El CALENDARIO de Material (DatePicker) trabaja en hora UTC: cuando el usuario elige el 20/10/2026,
// entrega la medianoche UTC de ese día. Estas dos funciones traducen entre ese valor y el texto
// "dd/mm/aaaa" que usa el resto de la app. Si usara la hora local, en algunos países la fecha
// podría correrse un día.
fun fechaUtcATexto(millisUtc: Long): String {
    val formato = SimpleDateFormat(FORMATO, Locale.getDefault())
    formato.timeZone = TimeZone.getTimeZone("UTC")
    return formato.format(Date(millisUtc))
}

// Al revés: del texto "dd/mm/aaaa" a la medianoche UTC, para que el calendario abra en esa fecha.
// Devuelve null si el texto todavía no es una fecha válida.
fun textoAFechaUtc(texto: String): Long? {
    val limpio = texto.trim()
    if (!PATRON.matches(limpio)) return null
    return try {
        val formato = SimpleDateFormat(FORMATO, Locale.getDefault())
        formato.timeZone = TimeZone.getTimeZone("UTC")
        formato.isLenient = false
        formato.parse(limpio)?.time
    } catch (e: Exception) {
        null
    }
}

// Hoy a la medianoche UTC, que es como el calendario compara los días.
// Sirve para desactivar en el calendario los días anteriores a hoy.
fun hoyComoFechaUtc(): Long =
    textoAFechaUtc(formatearFecha(System.currentTimeMillis())) ?: 0L

// De milisegundos a texto "dd/mm/aaaa" para mostrar en pantalla.
fun formatearFecha(millis: Long): String =
    SimpleDateFormat(FORMATO, Locale.getDefault()).format(Date(millis))

// Igual, pero incluye la hora ("dd/mm/aaaa HH:mm"): la uso para entregas y revisiones.
fun formatearFechaHora(millis: Long): String =
    SimpleDateFormat(FORMATO_HORA, Locale.getDefault()).format(Date(millis))

// Milisegundos del inicio (00:00) del día al que pertenece la marca de tiempo.
// Sirve para los filtros "Hoy" y "Próximos 7 días": comparo desde el comienzo del día.
fun inicioDelDia(millis: Long): Long {
    val calendario = Calendar.getInstance()
    calendario.timeInMillis = millis
    calendario.set(Calendar.HOUR_OF_DAY, 0)
    calendario.set(Calendar.MINUTE, 0)
    calendario.set(Calendar.SECOND, 0)
    calendario.set(Calendar.MILLISECOND, 0)
    return calendario.timeInMillis
}
