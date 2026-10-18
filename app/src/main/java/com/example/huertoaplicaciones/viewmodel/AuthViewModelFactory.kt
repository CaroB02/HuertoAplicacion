package com.example.huertoaplicaciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.huertoaplicaciones.data.repository.UsuarioRepository

// FACTORY = la "fábrica" que sabe cómo crear el AuthViewModel.
// Android crea los ViewModels solo cuando no tienen parámetros. Como AuthViewModel necesita un
// UsuarioRepository, tengo que explicarle a Android cómo construirlo: eso hace esta clase.
// (Los otros ViewModels reciben solo "Application", que Android sí sabe entregar, y no necesitan fábrica.)
class AuthViewModelFactory(
    private val usuarioRepository: UsuarioRepository
) : ViewModelProvider.Factory {

    // Android llama a create() cuando necesita el ViewModel.
    // @Suppress silencia la advertencia del cast "as T": yo sé que el tipo es el correcto.
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(usuarioRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
