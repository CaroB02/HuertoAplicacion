package com.example.huertoaplicaciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertoaplicaciones.data.dao.UsuarioDao
import com.example.huertoaplicaciones.util.validarCorreo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Valida el correo en tiempo real usando las reglas de util/Validaciones.kt
    fun onEmailChanged(nuevoEmail: String) {
        val error = when {
            nuevoEmail.isBlank() -> "El correo electrónico no puede estar vacío"
            !validarCorreo(nuevoEmail) -> "Ingrese un correo válido (ejemplo: nombre@huerta.cl)"
            else -> null
        }
        _uiState.update {
            it.copy(emailInput = nuevoEmail, emailError = error, errorMessage = null)
        }
    }

    // Valida la contraseña en tiempo real
    fun onPasswordChanged(nuevaPass: String) {
        val error = if (nuevaPass.isBlank()) "La contraseña no puede estar vacía" else null
        _uiState.update {
            it.copy(passwordInput = nuevaPass, passwordError = error, errorMessage = null)
        }
    }

    // Consulta en Room si existe un usuario activo con ese correo y contraseña
    fun iniciarSesion() {
        val currentState = _uiState.value

        if (currentState.emailInput.isBlank() || currentState.emailError != null) {
            _uiState.update { it.copy(emailError = "Verifique el correo ingresado") }
            return
        }
        if (currentState.passwordInput.isBlank()) {
            _uiState.update { it.copy(passwordError = "Ingrese la contraseña") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val usuario = usuarioDao.iniciarSesion(
                currentState.emailInput.trim(),
                currentState.passwordInput
            )

            if (usuario != null) {
                _uiState.update { it.copy(isLoading = false, usuarioLogueado = usuario) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Correo o contraseña incorrectos")
                }
            }
        }
    }

    // Carga rápida de cuentas ficticias de prueba (docente y estudiante)
    fun cargarCuentaPrueba(email: String) {
        onEmailChanged(email)
        onPasswordChanged("123456")
    }

    fun cerrarSesion() {
        _uiState.update { LoginUiState() }
    }
}