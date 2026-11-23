package com.example.huertoaplicaciones

import com.example.huertoaplicaciones.util.MensajeUi
import com.example.huertoaplicaciones.util.Mensajes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

// Guarda en una lista todos los mensajes que los ViewModels envían al Snackbar (objeto Mensajes),
// para poder comprobar en la prueba qué se le mostró al usuario.
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.escucharMensajes(): List<MensajeUi> {
    val recibidos = mutableListOf<MensajeUi>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        Mensajes.flujo.collect { recibidos.add(it) }
    }
    return recibidos
}

// Los StateFlow con WhileSubscribed solo calculan su valor mientras alguien los observa
// (en la app, la pantalla). En la prueba "observo" el flujo en segundo plano para activarlo.
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.observar(flujo: Flow<*>) {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flujo.collect { } }
}
