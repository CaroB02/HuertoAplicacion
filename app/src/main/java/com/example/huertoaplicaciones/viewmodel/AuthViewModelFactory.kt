package com.example.huertoaplicaciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.huertoaplicaciones.data.dao.UsuarioDao

class AuthViewModelFactory(
    private val usuarioDao: UsuarioDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(usuarioDao) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}