package com.example.huertoaplicaciones.ui.navigation

import com.example.huertoaplicaciones.viewmodel.RegistroViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.huertoaplicaciones.ui.screens.*
import com.example.huertoaplicaciones.util.Mensajes
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.viewmodel.AuthViewModel
import com.example.huertoaplicaciones.viewmodel.CultivoViewModel
import com.example.huertoaplicaciones.viewmodel.HistorialViewModel
import com.example.huertoaplicaciones.viewmodel.PanelViewModel
import com.example.huertoaplicaciones.viewmodel.ProblemaViewModel
import com.example.huertoaplicaciones.viewmodel.SectorViewModel
import com.example.huertoaplicaciones.viewmodel.TareaViewModel

// Aquí está el MAPA de pantallas de la app. Cada ruta es un nombre único que uso para navegar:
// navController.navigate(Rutas.SECTORES) abre la pantalla de sectores.
// Guardarlas como constantes evita escribir mal el nombre de una ruta.
object Rutas {
    const val LOGIN = "login"
    const val HOME_ADMIN = "home_admin"
    const val HOME_ESTUDIANTE = "home_estudiante"
    const val SECTORES = "sectores"
    const val SECTOR_DETALLE = "sector_detalle"
    const val SECTOR_FORM = "sector_form"
    const val CULTIVOS = "cultivos"
    const val CULTIVO_FORM = "cultivo_form"
    const val TAREAS = "tareas"
    const val TAREA_FORM = "tarea_form"
    const val TAREA_DETALLE = "tarea_detalle"
    const val MIS_TAREAS = "mis_tareas"
    const val REPORTES = "reportes"
    const val REPORTE_FORM = "reporte_form"
    const val HISTORIAL = "historial"
    const val REGISTRO = "registro"
}

// AppNavigation es el "director" de la app: decide qué pantalla se muestra y controla el ACCESO por perfil.
// Recibe todos los ViewModels ya creados (se crean en MainActivity) y se los entrega a cada pantalla.
// Estructura de Navigation Compose: NavController (controla la navegación) + NavHost (el contenedor
// donde se dibuja la pantalla actual) + composable(ruta) { } (qué dibujar en cada ruta).
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    sectorViewModel: SectorViewModel,
    cultivoViewModel: CultivoViewModel,
    tareaViewModel: TareaViewModel,
    problemaViewModel: ProblemaViewModel,
    panelViewModel: PanelViewModel,
    historialViewModel: HistorialViewModel,
    registroViewModel: RegistroViewModel
) {
    // rememberNavController guarda el estado de la navegación (la pila de pantallas visitadas).
    val navController = rememberNavController()
    // collectAsState convierte el StateFlow del ViewModel en un estado de Compose:
    // cuando cambia (por ejemplo al iniciar sesión), esta función se vuelve a dibujar sola.
    val uiState by authViewModel.uiState.collectAsState()
    val usuario = uiState.usuarioLogueado
    // Estas dos banderas me sirven para el CONTROL DE ACCESO: cada ruta de abajo solo muestra
    // su contenido si el perfil corresponde (por ejemplo, sectores solo para el docente).
    val esAdmin = usuario?.perfil == Perfil.ADMIN
    val esEstudiante = usuario?.perfil == Perfil.ESTUDIANTE
    val panel by panelViewModel.estado.collectAsState()

    // LaunchedEffect ejecuta código cuando cambia la clave (aquí "usuario"): al iniciar o cerrar sesión
    // le aviso al panel y al historial quién es el usuario actual para que calculen sus datos.
    // Los paneles y el historial se recalculan según quién inició sesión
    LaunchedEffect(usuario) {
        panelViewModel.definirUsuario(usuario)
        historialViewModel.definirUsuario(usuario)
    }

    // Los ViewModels publican mensajes en el objeto Mensajes; aquí los recibo y los muestro en un Snackbar
    // (la barrita de abajo). Está aquí, en el nivel más alto, para que se vea en cualquier pantalla.
    // Confirmaciones y errores de operación, visibles en cualquier pantalla
    val snackbarHostState = remember { SnackbarHostState() }
    var mensajeEsError by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        Mensajes.flujo.collect { mensaje ->
            mensajeEsError = mensaje.esError
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(mensaje.texto)
        }
    }

    // Cerrar sesión: borro el usuario y vuelvo al login. popUpTo(0) { inclusive = true } vacía toda la
    // pila de pantallas, para que el botón Atrás no deje volver a una pantalla de la sesión anterior.
    val cerrarSesion = {
        authViewModel.cerrarSesion()
        navController.navigate(Rutas.LOGIN) { popUpTo(0) { inclusive = true } }
    }

    // Adaptación a pantallas grandes: el contenido nunca pasa de 720dp de ancho y se centra.
    // En pantallas anchas (tablets) el contenido se centra con un ancho máximo legible
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(modifier = Modifier.widthIn(max = 720.dp).fillMaxSize()) {
            // startDestination: la primera pantalla que se ve al abrir la app es siempre el login.
            NavHost(navController = navController, startDestination = Rutas.LOGIN) {
                composable(Rutas.LOGIN) {
                    LoginScreen(
                        authViewModel,
                        // "Crear cuenta": dejo el formulario en blanco y paso a la pantalla de registro.
                        onCrearCuenta = {
                            registroViewModel.nuevo()
                            navController.navigate(Rutas.REGISTRO)
                        }
                    ) { u ->
                        val destino = if (u.perfil == Perfil.ADMIN)
                            Rutas.HOME_ADMIN else Rutas.HOME_ESTUDIANTE
                        navController.navigate(destino) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                    }
                }
                // Registro de cuentas nuevas (estudiante o docente). No requiere sesión iniciada.
                composable(Rutas.REGISTRO) {
                    RegistroScreen(
                        viewModel = registroViewModel,
                        onRegistrado = { correo ->
                            // Dejo el correo ya escrito en el login para que solo falte la contraseña.
                            authViewModel.onEmailChanged(correo)
                            navController.popBackStack()
                        },
                        onVolver = { navController.popBackStack() }
                    )
                }
                composable(Rutas.HOME_ADMIN) {
                    HomeAdminScreen(
                        usuario = usuario ?: return@composable,
                        panel = panel,
                        onIrASectores = { navController.navigate(Rutas.SECTORES) },
                        onIrACultivos = { navController.navigate(Rutas.CULTIVOS) },
                        onIrATareas = { navController.navigate(Rutas.TAREAS) },
                        onIrAReportes = { navController.navigate(Rutas.REPORTES) },
                        onIrAHistorial = { navController.navigate(Rutas.HISTORIAL) },
                        onCerrarSesion = cerrarSesion
                    )
                }
                composable(Rutas.HOME_ESTUDIANTE) {
                    HomeEstudianteScreen(
                        usuario = usuario ?: return@composable,
                        panel = panel,
                        onIrAMisTareas = { navController.navigate(Rutas.MIS_TAREAS) },
                        onIrAReportes = { navController.navigate(Rutas.REPORTES) },
                        onIrAHistorial = { navController.navigate(Rutas.HISTORIAL) },
                        onCerrarSesion = cerrarSesion
                    )
                }

                // Patrón de todas las rutas protegidas: "if (esAdmin) { Pantalla(...) }".
                // Si un estudiante llegara a esta ruta, no se dibuja nada: el acceso queda restringido por perfil.
                // ---- Sectores (solo docente) ----
                composable(Rutas.SECTORES) {
                    if (esAdmin) {
                        SectorListScreen(
                            viewModel = sectorViewModel,
                            onNuevo = {
                                sectorViewModel.nuevoSector()
                                navController.navigate(Rutas.SECTOR_FORM)
                            },
                            onAbrir = { id ->
                                sectorViewModel.seleccionarSector(id)
                                navController.navigate(Rutas.SECTOR_DETALLE)
                            },
                            onVolver = { navController.popBackStack() }
                        )
                    }
                }
                composable(Rutas.SECTOR_DETALLE) {
                    if (esAdmin) {
                        SectorDetalleScreen(
                            viewModel = sectorViewModel,
                            onEditar = { id ->
                                sectorViewModel.nuevoSector()
                                sectorViewModel.cargarSector(id)
                                navController.navigate(Rutas.SECTOR_FORM)
                            },
                            onEditarCultivo = { id ->
                                cultivoViewModel.nuevoCultivo()
                                cultivoViewModel.cargarCultivo(id)
                                navController.navigate(Rutas.CULTIVO_FORM)
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

                // "Mis tareas" es solo para estudiantes. Al abrir una tarea, primero le aviso al ViewModel cuál es
                // (seleccionar) y recién después navego al detalle.
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

                // El detalle de tarea lo usan los dos perfiles: la pantalla decide qué botones mostrar según el perfil
                // (el estudiante ve entregar; el docente ve validar o rechazar).
                // ---- Detalle (lo usan ambos perfiles) ----
                composable(Rutas.TAREA_DETALLE) {
                    if (usuario != null) {
                        TareaDetalleScreen(
                            viewModel = tareaViewModel,
                            usuario = usuario,
                            onEditar = { id ->
                                tareaViewModel.editarTarea(id)
                                navController.navigate(Rutas.TAREA_FORM)
                            },
                            onVolver = { navController.popBackStack() }
                        )
                    }
                }

                // ---- Reportes de problemas (ambos perfiles) ----
                composable(Rutas.REPORTES) {
                    if (usuario != null) {
                        ReportesScreen(
                            viewModel = problemaViewModel,
                            usuario = usuario,
                            onNuevo = {
                                problemaViewModel.nuevoReporte()
                                navController.navigate(Rutas.REPORTE_FORM)
                            },
                            onVolver = { navController.popBackStack() }
                        )
                    }
                }
                composable(Rutas.REPORTE_FORM) {
                    if (usuario != null) {
                        ReporteFormScreen(
                            viewModel = problemaViewModel,
                            usuario = usuario,
                            onVolver = { navController.popBackStack() }
                        )
                    }
                }

                // ---- Historial de actividades ----
                composable(Rutas.HISTORIAL) {
                    if (usuario != null) {
                        HistorialScreen(
                            viewModel = historialViewModel,
                            onVolver = { navController.popBackStack() }
                        )
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) { datos ->
                Snackbar(
                    snackbarData = datos,
                    containerColor = if (mensajeEsError) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.inverseSurface,
                    contentColor = if (mensajeEsError) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    }
}
