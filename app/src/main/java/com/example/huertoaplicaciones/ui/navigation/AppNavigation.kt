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
import com.example.huertoaplicaciones.ui.screens.SectorFormScreen
import com.example.huertoaplicaciones.ui.screens.SectorListScreen
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.viewmodel.AuthViewModel
import com.example.huertoaplicaciones.viewmodel.SectorViewModel

object Rutas {
    const val LOGIN = "login"
    const val HOME_ADMIN = "home_admin"
    const val HOME_ESTUDIANTE = "home_estudiante"
    const val SECTORES = "sectores"
    const val SECTOR_FORM = "sector_form"
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    sectorViewModel: SectorViewModel
) {
    val navController = rememberNavController()
    val uiState by authViewModel.uiState.collectAsState()
    val nombre = uiState.usuarioLogueado?.nombre ?: ""
    val esAdmin = uiState.usuarioLogueado?.perfil == Perfil.ADMIN

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
            HomeAdminScreen(
                nombre = nombre,
                onIrASectores = { navController.navigate(Rutas.SECTORES) },
                onCerrarSesion = cerrarSesion
            )
        }
        composable(Rutas.HOME_ESTUDIANTE) {
            HomeEstudianteScreen(nombre = nombre, onCerrarSesion = cerrarSesion)
        }
        composable(Rutas.SECTORES) {
            if (esAdmin) {
                SectorListScreen(
                    viewModel = sectorViewModel,
                    onNuevo = {
                        sectorViewModel.nuevoSector()
                        navController.navigate(Rutas.SECTOR_FORM)
                    },
                    onEditar = { id ->
                        sectorViewModel.nuevoSector()
                        sectorViewModel.cargarSector(id)
                        navController.navigate(Rutas.SECTOR_FORM)
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }
        composable(Rutas.SECTOR_FORM) {
            if (esAdmin) {
                SectorFormScreen(
                    viewModel = sectorViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}