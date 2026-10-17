package com.example.huertoaplicaciones.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.huertoaplicaciones.ui.screens.*
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.viewmodel.AuthViewModel
import com.example.huertoaplicaciones.viewmodel.CultivoViewModel
import com.example.huertoaplicaciones.viewmodel.SectorViewModel
import com.example.huertoaplicaciones.viewmodel.TareaViewModel

object Rutas {
    const val LOGIN = "login"
    const val HOME_ADMIN = "home_admin"
    const val HOME_ESTUDIANTE = "home_estudiante"
    const val SECTORES = "sectores"
    const val SECTOR_FORM = "sector_form"
    const val CULTIVOS = "cultivos"
    const val CULTIVO_FORM = "cultivo_form"
    const val TAREAS = "tareas"
    const val TAREA_FORM = "tarea_form"
    const val TAREA_DETALLE = "tarea_detalle"
    const val MIS_TAREAS = "mis_tareas"
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    sectorViewModel: SectorViewModel,
    cultivoViewModel: CultivoViewModel,
    tareaViewModel: TareaViewModel
) {
    val navController = rememberNavController()
    val uiState by authViewModel.uiState.collectAsState()
    val usuario = uiState.usuarioLogueado
    val nombre = usuario?.nombre ?: ""
    val esAdmin = usuario?.perfil == Perfil.ADMIN
    val esEstudiante = usuario?.perfil == Perfil.ESTUDIANTE

    val cerrarSesion = {
        authViewModel.cerrarSesion()
        navController.navigate(Rutas.LOGIN) { popUpTo(0) { inclusive = true } }
    }

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {
        composable(Rutas.LOGIN) {
            LoginScreen(authViewModel) { u ->
                val destino = if (u.perfil == Perfil.ADMIN)
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
                onIrACultivos = { navController.navigate(Rutas.CULTIVOS) },
                onIrATareas = { navController.navigate(Rutas.TAREAS) },
                onCerrarSesion = cerrarSesion
            )
        }
        composable(Rutas.HOME_ESTUDIANTE) {
            HomeEstudianteScreen(
                nombre = nombre,
                onIrAMisTareas = { navController.navigate(Rutas.MIS_TAREAS) },
                onCerrarSesion = cerrarSesion
            )
        }

        // ---- Sectores (solo docente) ----
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

        // ---- Cultivos (solo docente) ----
        composable(Rutas.CULTIVOS) {
            if (esAdmin) {
                CultivoListScreen(
                    viewModel = cultivoViewModel,
                    onNuevo = {
                        cultivoViewModel.nuevoCultivo()
                        navController.navigate(Rutas.CULTIVO_FORM)
                    },
                    onEditar = { id ->
                        cultivoViewModel.nuevoCultivo()
                        cultivoViewModel.cargarCultivo(id)
                        navController.navigate(Rutas.CULTIVO_FORM)
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }
        composable(Rutas.CULTIVO_FORM) {
            if (esAdmin) {
                CultivoFormScreen(
                    viewModel = cultivoViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        // ---- Tareas: docente crea y revisa ----
        composable(Rutas.TAREAS) {
            if (esAdmin) {
                TareaListScreen(
                    viewModel = tareaViewModel,
                    onNueva = {
                        tareaViewModel.nuevaTarea()
                        navController.navigate(Rutas.TAREA_FORM)
                    },
                    onAbrir = { id ->
                        tareaViewModel.seleccionar(id)
                        navController.navigate(Rutas.TAREA_DETALLE)
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }
        composable(Rutas.TAREA_FORM) {
            if (esAdmin && usuario != null) {
                TareaFormScreen(
                    viewModel = tareaViewModel,
                    usuario = usuario,
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        // ---- Tareas: estudiante ve y realiza las suyas ----
        composable(Rutas.MIS_TAREAS) {
            if (esEstudiante && usuario != null) {
                MisTareasScreen(
                    viewModel = tareaViewModel,
                    usuario = usuario,
                    onAbrir = { id ->
                        tareaViewModel.seleccionar(id)
                        navController.navigate(Rutas.TAREA_DETALLE)
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        // ---- Detalle (lo usan ambos perfiles) ----
        composable(Rutas.TAREA_DETALLE) {
            if (usuario != null) {
                TareaDetalleScreen(
                    viewModel = tareaViewModel,
                    usuario = usuario,
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}