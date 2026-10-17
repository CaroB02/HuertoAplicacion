package com.example.huertoaplicaciones.util

fun validarObligatorio(texto: String): Boolean = texto.isNotBlank()

fun validarCorreo(correo: String): Boolean =
    correo.isNotBlank() && correo.contains("@") && correo.endsWith(".cl")

fun validarNumeroPositivo(texto: String): Boolean =
    texto.toIntOrNull()?.let { it > 0 } ?: false

fun validarFecha(texto: String): Boolean = parsearFecha(texto) != null