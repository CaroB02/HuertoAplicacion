package com.example.huertoaplicaciones

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

// Los ViewModels lanzan sus corrutinas en viewModelScope, que usa el hilo principal de Android (Main).
// En una prueba unitaria NO existe ese hilo, así que esta regla lo reemplaza por un dispatcher de prueba
// antes de cada test y lo restaura al terminar. Se usa con:  @get:Rule val reglaMain = ReglaMainDispatcher()
// UnconfinedTestDispatcher ejecuta las corrutinas de inmediato, así al volver de la llamada ya terminaron.
@OptIn(ExperimentalCoroutinesApi::class)
class ReglaMainDispatcher(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
