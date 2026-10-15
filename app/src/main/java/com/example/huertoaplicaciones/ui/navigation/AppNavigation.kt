package com.example.huertoaplicaciones.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.huertoaplicaciones.ui.screens.HomeAdminScreen
import com.example.huertoaplicaciones.ui.screens.HomeEstudianteScreen
import com.example.huertoaplicaciones.ui.screens.LoginScreen
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.viewmodel.AuthViewModel

object Rutas {
    const val LOGIN = "login"
    const val HOME_ADMIN = "home_admin"
    const val HOME_ESTUDIANTE = "home_estudiante"
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val uiState by authViewModel.uiState.collectAsState()
    val nombre = uiState.usuarioLogueado?.nombre ?: ""

    val cerrarSesion = {
        authViewModel.cerrarSesion()
        navController.navigate(Rutas.LOGIN) { popUpTo(0) { inclusive = true } }
    }

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {
        composable(Rutas.LOGIN) {
            LoginScreen(authViewModel) { usuario ->
                val destino = if (usuario.perfil == Perfil.ADMIN)
                    Rutas.HOME_ADMIN else Rutas.HOME_ESTUDIANTE
                navController.navigate(destino) {
                    popUpTo(Rutas.LOGIN) { inclusive = true }
                }
            }
        }
        composable(Rutas.HOME_ADMIN) {
            HomeAdminScreen(nombre = nombre, onCerrarSesion = cerrarSesion)
        }
        composable(Rutas.HOME_ESTUDIANTE) {
            HomeEstudianteScreen(nombre = nombre, onCerrarSesion = cerrarSesion)
        }
    }
}