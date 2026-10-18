package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.data.entity.Usuario

// UiState = una "fotografía" de TODO lo que la pantalla necesita mostrar en un momento dado.
// La pantalla no guarda nada por su cuenta: solo dibuja lo que dice este objeto.
// Cuando el ViewModel crea una copia con valores nuevos, la pantalla se redibuja sola.
// Representa el estado de la pantalla de Login y sus validaciones de formulario
data class LoginUiState(
    val emailInput: String = "",
    val emailError: String? = null,       // null = sin error; si tiene texto, se muestra bajo el campo
    val passwordInput: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false,       // true = muestro la ruedita de carga
    val usuarioLogueado: Usuario? = null, // null = nadie ha iniciado sesión todavía
    val errorMessage: String? = null      // error general (credenciales incorrectas)
)
