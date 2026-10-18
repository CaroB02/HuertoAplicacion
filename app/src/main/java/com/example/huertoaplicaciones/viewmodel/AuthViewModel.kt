package com.example.huertoaplicaciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertoaplicaciones.data.repository.UsuarioRepository
import com.example.huertoaplicaciones.util.validarCorreo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// VIEWMODEL del login. Un ViewModel guarda el ESTADO de la pantalla y la lógica, y sobrevive
// a rotaciones de pantalla (la pantalla se destruye y se vuelve a crear, pero el ViewModel no).
// Flujo: la pantalla llama a una función de aquí -> aquí valido y consulto -> actualizo _uiState
// -> la pantalla, que está "escuchando" el estado, se redibuja sola.
class AuthViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    // Patrón de los dos StateFlow:
    //  - _uiState es PRIVADO y mutable: solo este ViewModel puede cambiarlo.
    //  - uiState es PÚBLICO y de solo lectura: la pantalla solo puede observarlo.
    // Así la pantalla nunca modifica el estado por su cuenta.
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Valida el correo en tiempo real usando las reglas de util/Validaciones.kt
    fun onEmailChanged(nuevoEmail: String) {
        // "when" sin argumento = una cadena de if/else if: devuelve el mensaje de error, o null si está bien.
        val error = when {
            nuevoEmail.isBlank() -> "El correo electrónico no puede estar vacío"
            !validarCorreo(nuevoEmail) -> "Ingrese un correo válido (ejemplo: nombre@huerta.cl)"
            else -> null
        }
        // update { it.copy(...) }: creo una COPIA del estado cambiando solo lo que necesito.
        // El estado es inmutable (data class), por eso siempre copio en vez de modificar.
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

        // Antes de consultar la base, reviso que los campos estén bien. Si no, corto con "return".
        if (currentState.emailInput.isBlank() || currentState.emailError != null) {
            _uiState.update { it.copy(emailError = "Verifique el correo ingresado") }
            return
        }
        if (currentState.passwordInput.isBlank()) {
            _uiState.update { it.copy(passwordError = "Ingrese la contraseña") }
            return
        }

        // launch = abro una corrutina (trabajo en segundo plano) porque consultar la base puede tardar
        // y no quiero congelar la pantalla. viewModelScope la cancela sola si el ViewModel se destruye.
        viewModelScope.launch {
            // Enciendo el indicador de carga (la ruedita del botón).
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val usuario = usuarioRepository.iniciarSesion(
                // Los correos se guardan en minúscula, así "Matias@..." y "matias@..." son la misma cuenta.
                currentState.emailInput.trim().lowercase(),
                currentState.passwordInput
            )

            // Si encontró al usuario guardo quién es (la navegación reacciona a esto); si no, muestro el error.
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

    // Cerrar sesión = volver al estado inicial vacío (se borra el usuario logueado).
    fun cerrarSesion() {
        _uiState.update { LoginUiState() }
    }
}
