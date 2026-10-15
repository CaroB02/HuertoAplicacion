package com.example.huertoaplicaciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertoaplicaciones.data.dao.UsuarioDao
import com.example.huertoaplicaciones.data.entity.Usuario
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

    // Maneja los cambios de texto en el campo de Email con validación en tiempo real
    fun onEmailChanged(nuevoEmail: String) {
        val error = when {
            nuevoEmail.isBlank() -> "El correo electrónico no puede estar vacío"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(nuevoEmail).matches() -> "Ingrese un correo válido (ejemplo: nombre@huerta.cl)"
            else -> null
        }
        _uiState.update {
            it.copy(emailInput = nuevoEmail, emailError = error, errorMessage = null)
        }
    }

    // Maneja los cambios de texto en la contraseña
    fun onPasswordChanged(nuevaPass: String) {
        val error = if (nuevaPass.isBlank()) "La contraseña no puede estar vacía" else null
        _uiState.update {
            it.copy(passwordInput = nuevaPass, passwordError = error, errorMessage = null)
        }
    }

    // Intenta iniciar sesión consultando los datos en la base de datos de Room
    fun iniciarSesion() {
        val currentState = _uiState.value

        // Validaciones finales antes de procesar
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
/*
            // Búsqueda en Room
            val usuario = usuarioDao.obtenerPorCorreo(currentState.emailInput.trim())
*/
            val usuario = usuarioDao.iniciarSesion(
                currentState.emailInput.trim(),
                currentState.passwordInput
            )

            if (usuario != null) {
                // Validación simulación exitosa de credenciales
                _uiState.update {
                    it.copy(isLoading = false, usuarioLogueado = usuario)
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        //errorMessage = "Usuario no registrado. Pruebe con las cuentas de prueba."
                        errorMessage = "Correo o contraseña incorrectos"
                    )
                }
            }
        }
    }

    // Método para cargar rápidamente cuentas predeterminadas de prueba (Docente / Estudiante)
    fun cargarCuentaPrueba(email: String) {
        onEmailChanged(email)
        onPasswordChanged("123456")
    }

    // Cerrar sesión activa
    fun cerrarSesion() {
        _uiState.update { LoginUiState() }
    }
}