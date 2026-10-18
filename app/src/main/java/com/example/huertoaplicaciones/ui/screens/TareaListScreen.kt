package com.example.huertoaplicaciones.ui.screens

import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Assignment
import com.example.huertoaplicaciones.ui.components.SeccionTitulo
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.ui.components.TareaCard
import com.example.huertoaplicaciones.util.EstadoTarea
import com.example.huertoaplicaciones.util.estadoMostrado
import com.example.huertoaplicaciones.viewmodel.FiltroFecha
import com.example.huertoaplicaciones.viewmodel.TareaViewModel

private val ESTADOS_FILTRO = listOf(
    EstadoTarea.PENDIENTE, EstadoTarea.EN_DESARROLLO, EstadoTarea.PENDIENTE_VALIDACION,
    EstadoTarea.VALIDADA, EstadoTarea.RECHAZADA, EstadoTarea.ATRASADA
)

@OptIn(ExperimentalMaterial3Api::class)
// Lista de tareas del DOCENTE, con filtros por estado, responsable y fecha.
// Los filtros viven en el TareaViewModel: aquí solo muestro los botones y la lista ya filtrada.
@Composable
fun TareaListScreen(
    viewModel: TareaViewModel,
    onNueva: () -> Unit,
    onAbrir: (Long) -> Unit,
    onVolver: () -> Unit
) {
    // "todas" = sin filtrar (para los contadores). "tareas" = ya filtradas (lo que se dibuja).
    val todas by viewModel.tareas.collectAsState()
    val tareas by viewModel.tareasFiltradas.collectAsState()
    val filtros by viewModel.filtros.collectAsState()
    val cultivos by viewModel.cultivos.collectAsState()
    val estudiantes by viewModel.estudiantes.collectAsState()

    val porValidar = todas.count { it.estado == EstadoTarea.PENDIENTE_VALIDACION }
    val atrasadas = todas.count { estadoMostrado(it) == EstadoTarea.ATRASADA }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                title = { Text("Tareas") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNueva) {
                Icon(Icons.Default.Add, contentDescription = "Nueva tarea")
            }
        }
    ) { innerPadding ->
        if (todas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no hay tareas. Presiona + para crear y asignar una.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Encabezado con ícono: cuántas tareas esperan validación y cuántas están atrasadas.
                    SeccionTitulo(
                        Icons.Default.Assignment,
                        "Por validar: $porValidar · Atrasadas: $atrasadas"
                    )
                }

                // Tres filas de botones de selección. Al tocar uno, el ViewModel cambia el filtro y la lista se recalcula.
                // ---- Filtros por estado, responsable y fecha ----
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SeccionTitulo(Icons.Default.FilterList, "Estado")
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = filtros.estado == null,
                                onClick = { viewModel.filtrarPorEstado(null) },
                                label = { Text("Todos") }
                            )
                            ESTADOS_FILTRO.forEach { estado ->
                                FilterChip(
                                    selected = filtros.estado == estado,
                                    onClick = { viewModel.filtrarPorEstado(estado) },
                                    label = { Text(estado) }
                                )
                            }
                        }
                        SeccionTitulo(Icons.Default.Face, "Responsable")
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = filtros.responsableId == null,
                                onClick = { viewModel.filtrarPorResponsable(null) },
                                label = { Text("Todos") }
                            )
                            estudiantes.forEach { est ->
                                FilterChip(
                                    selected = filtros.responsableId == est.id,
                                    onClick = { viewModel.filtrarPorResponsable(est.id) },
                                    label = { Text(est.nombre) }
                                )
                            }
                        }
                        SeccionTitulo(Icons.Default.CalendarMonth, "Fecha programada")
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FiltroFecha.entries.forEach { fecha ->
                                FilterChip(
                                    selected = filtros.fecha == fecha,
                                    onClick = { viewModel.filtrarPorFecha(fecha) },
                                    label = { Text(fecha.etiqueta) }
                                )
                            }
                        }
                        if (filtros.hayFiltros) {
                            TextButton(onClick = { viewModel.limpiarFiltros() }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Limpiar filtros (${tareas.size} resultado(s))")
                            }
                        }
                    }
                }

                // Si hay tareas pero ningún resultado con los filtros elegidos, aviso en vez de mostrar una lista vacía.
                if (tareas.isEmpty()) {
                    item {
                        Text("Ninguna tarea coincide con los filtros seleccionados.")
                    }
                }
                items(tareas, key = { it.id }) { tarea ->
                    val cultivo = cultivos.firstOrNull { it.id == tarea.cultivoId }?.nombre ?: "Cultivo"
                    val alumno = estudiantes.firstOrNull { it.id == tarea.asignadoAId }?.nombre ?: "Sin asignar"
                    TareaCard(tarea, "$cultivo · Asignada a $alumno") { onAbrir(tarea.id) }
                }
            }
        }
    }
}
