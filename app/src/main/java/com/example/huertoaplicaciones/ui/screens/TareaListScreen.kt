package com.example.huertoaplicaciones.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import com.example.huertoaplicaciones.viewmodel.TareaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareaListScreen(
    viewModel: TareaViewModel,
    onNueva: () -> Unit,
    onAbrir: (Long) -> Unit,
    onVolver: () -> Unit
) {
    val tareas by viewModel.tareas.collectAsState()
    val cultivos by viewModel.cultivos.collectAsState()
    val estudiantes by viewModel.estudiantes.collectAsState()

    val porValidar = tareas.count { it.estado == EstadoTarea.PENDIENTE_VALIDACION }
    val atrasadas = tareas.count { estadoMostrado(it) == EstadoTarea.ATRASADA }

    Scaffold(
        topBar = {
            TopAppBar(
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
        if (tareas.isEmpty()) {
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
                    Text(
                        "Por validar: $porValidar · Atrasadas: $atrasadas",
                        style = MaterialTheme.typography.labelLarge
                    )
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
