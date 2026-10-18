package com.example.huertoaplicaciones.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Un mensaje para el usuario: el texto y si es un error (se muestra en rojo) o un éxito.
data class MensajeUi(val texto: String, val esError: Boolean = false)

// Canal único de confirmaciones y errores de operación.
// Los ViewModels publican aquí y AppNavigation los muestra en un Snackbar,
// de modo que el mensaje sobrevive al cambio de pantalla.
// (Ejemplo: al guardar un sector la app vuelve a la lista, y el mensaje "Sector creado" sigue visible.)
object Mensajes {
    // SharedFlow = un "canal" por el que se envían eventos de una sola vez (no guarda estado).
    // extraBufferCapacity = guarda hasta 8 mensajes por si llegan varios seguidos.
    private val _flujo = MutableSharedFlow<MensajeUi>(extraBufferCapacity = 8)
    // Expongo solo la versión de lectura: desde afuera nadie puede emitir directamente.
    val flujo: SharedFlow<MensajeUi> = _flujo.asSharedFlow()

    // tryEmit envía el mensaje sin esperar (no hace falta una corrutina).
    fun exito(texto: String) { _flujo.tryEmit(MensajeUi(texto)) }
    fun error(texto: String) { _flujo.tryEmit(MensajeUi(texto, esError = true)) }
}
