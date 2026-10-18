package com.example.huertoaplicaciones.util

// Aquí están TODAS las reglas de validación de los formularios.
// Las separé de las pantallas a propósito (arquitectura MVVM): la pantalla solo muestra,
// y la lógica de "esto está bien o mal" vive aquí. Cada función devuelve true si el dato es VÁLIDO.

// El texto no puede estar vacío ni tener solo espacios.
fun validarObligatorio(texto: String): Boolean = texto.isNotBlank()

// Un correo válido tiene "@" y termina en ".cl" (regla que pidió el profe en la actividad de validaciones).
fun validarCorreo(correo: String): Boolean =
    correo.isNotBlank() && correo.contains("@") && correo.endsWith(".cl")

// Convierte el texto a número y exige que sea mayor que 0 (lo uso para la frecuencia de riego).
// toIntOrNull() devuelve null si no es un número (por ejemplo "abc"), y "?: false" lo trata como inválido.
fun validarNumeroPositivo(texto: String): Boolean =
    texto.toIntOrNull()?.let { it > 0 } ?: false

// La fecha es válida si parsearFecha() logra convertirla (formato dd/mm/aaaa y que exista ese día).
fun validarFecha(texto: String): Boolean = parsearFecha(texto) != null

// Largo mínimo de la contraseña al crear una cuenta (regla de la actividad de validaciones del curso).
const val LARGO_MIN_CLAVE = 8

// La contraseña nueva debe tener al menos 8 caracteres.
fun validarClave(clave: String): Boolean = clave.length >= LARGO_MIN_CLAVE

// El nombre completo debe tener al menos dos palabras (nombre y apellido) y no pasar del largo máximo.
// split(" ") parte el texto por espacios; filter descarta los vacíos que quedan si hay espacios dobles.
fun validarNombreCompleto(nombre: String): Boolean =
    validarTexto(nombre, Limites.NOMBRE) &&
            nombre.trim().split(" ").filter { it.isNotBlank() }.size >= 2

// La fecha programada de una tarea no puede ser anterior a HOY: no tiene sentido asignar un trabajo
// para un día que ya pasó. Al EDITAR una tarea se permite conservar su fecha original (aunque ya haya
// pasado), para poder corregir otros datos sin tener que cambiar la fecha.
fun validarFechaProgramada(
    texto: String,
    fechaOriginal: Long? = null,
    ahora: Long = System.currentTimeMillis()
): Boolean {
    val fecha = parsearFecha(texto) ?: return false
    // Comparo solo el DÍA de la fecha original (inicioDelDia), porque la guardada puede traer también la hora
    // y el texto dd/mm/aaaa que escribe el calendario siempre es la medianoche de ese día.
    val conservaLaOriginal = fechaOriginal != null && fecha == inicioDelDia(fechaOriginal)
    return fecha >= inicioDelDia(ahora) || conservaLaOriginal
}

// Texto obligatorio que además no supera la longitud máxima permitida
fun validarTexto(texto: String, maximo: Int): Boolean =
    validarObligatorio(texto) && texto.trim().length <= maximo
