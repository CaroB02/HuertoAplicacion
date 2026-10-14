package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.data.entity.Usuario

// Representa el estado de la pantalla de Login y sus validaciones de formulario
data class LoginUiState(
    val emailInput: String = "",
    val emailError: String? = null,
    val passwordInput: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val usuarioLogueado: Usuario? = null,
    val errorMessage: String? = null
)