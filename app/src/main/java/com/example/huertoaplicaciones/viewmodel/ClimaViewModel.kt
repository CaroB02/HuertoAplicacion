package com.example.huertoaplicaciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertoaplicaciones.data.repository.ClimaRepository
import com.example.huertoaplicaciones.data.repository.Repositorios
import com.example.huertoaplicaciones.util.ClimaHuerto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Los tres estados posibles de la consulta a la API externa (igual que ClientesUiState en clase):
//  Cargando -> se muestra un indicador de progreso
//  Exito    -> se muestra la tarjeta del clima
//  Error    -> se muestra el mensaje y un botón "Reintentar"
sealed interface ClimaUiState {
    data object Cargando : ClimaUiState
    data class Exito(val clima: ClimaHuerto) : ClimaUiState
    data class Error(val mensaje: String) : ClimaUiState
}

// ViewModel de la API EXTERNA de clima (Open-Meteo).
// Es independiente de los microservicios: si no hay internet, solo esta tarjeta muestra el error.
class ClimaViewModel(
    private val repository: ClimaRepository = Repositorios.clima
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClimaUiState>(ClimaUiState.Cargando)
    val uiState: StateFlow<ClimaUiState> = _uiState.asStateFlow()

    init {
        cargarClima()
    }

    fun cargarClima() {
        viewModelScope.launch {
            _uiState.value = ClimaUiState.Cargando
            try {
                val clima = repository.obtenerClima()
                _uiState.value = ClimaUiState.Exito(clima)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = ClimaUiState.Error(
                    "No se pudo obtener el clima. Comprueba la conexión a internet e inténtalo nuevamente."
                )
            }
        }
    }
}
