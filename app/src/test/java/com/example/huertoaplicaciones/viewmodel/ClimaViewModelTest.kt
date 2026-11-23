package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.ReglaMainDispatcher
import com.example.huertoaplicaciones.data.repository.ClimaRepository
import com.example.huertoaplicaciones.util.ClimaHuerto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Pruebas del ViewModel de la API externa: los tres estados Cargando / Exito / Error.
class ClimaViewModelTest {

    @get:Rule
    val reglaMain = ReglaMainDispatcher()

    private val repository = mockk<ClimaRepository>()

    private val clima = ClimaHuerto(
        temperatura = 21.0, humedad = 40, viento = 10.0, descripcion = "Despejado",
        lluviaHoyMm = 0.0, probabilidadLluviaHoy = 0, dias = emptyList(),
        recomendacion = "Sin lluvia importante"
    )

    @Test
    fun alCrearseConsultaElClimaYQuedaEnExito() = runTest {
        coEvery { repository.obtenerClima(any(), any()) } returns clima

        val vm = ClimaViewModel(repository)

        assertEquals(ClimaUiState.Exito(clima), vm.uiState.value)
    }

    @Test
    fun sinInternetQuedaEnErrorYReintentarFunciona() = runTest {
        coEvery { repository.obtenerClima(any(), any()) } throws IOException("sin red")
        val vm = ClimaViewModel(repository)
        assertTrue(vm.uiState.value is ClimaUiState.Error)

        coEvery { repository.obtenerClima(any(), any()) } returns clima
        vm.cargarClima()

        assertEquals(ClimaUiState.Exito(clima), vm.uiState.value)
    }
}
