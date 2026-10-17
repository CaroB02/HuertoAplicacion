package com.example.huertoaplicaciones.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val FORMATO = "dd/MM/yyyy"
private val PATRON = Regex("""\d{2}/\d{2}/\d{4}""")

// Devuelve los milisegundos de la fecha, o null si el texto no es una fecha válida
fun parsearFecha(texto: String): Long? {
    val limpio = texto.trim()
    if (!PATRON.matches(limpio)) return null
    return try {
        val formato = SimpleDateFormat(FORMATO, Locale.getDefault())
        formato.isLenient = false
        formato.parse(limpio)?.time
    } catch (e: Exception) {
        null
    }
}

fun formatearFecha(millis: Long): String =
    SimpleDateFormat(FORMATO, Locale.getDefault()).format(Date(millis))
