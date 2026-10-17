package com.example.huertoaplicaciones.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.data.entity.Tarea
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.ui.components.TareaCard
import com.example.huertoaplicaciones.util.EstadoTarea
import com.example.huertoaplicaciones.util.estadoMostrado
import com.example.huertoaplicaciones.viewmodel.TareaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisTareasScreen(
    viewModel: TareaViewModel,
    usuario: Usuario,
    onAbrir: (Long) -> Unit,
    onVolver: () -> Unit
) {
    val flujo = remember(usuario.id) { viewModel.tareasDeEstudiante(usuario.id) }
    val tareas by flujo.collectAsState(initial = emptyList<Tarea>())
    val cultivos by viewModel.cultivos.collectAsState()

    val porHacer = tareas.count {
        val e = estadoMostrado(it)
        e == EstadoTarea.PENDIENTE || e == EstadoTarea.EN_DESARROLLO ||
                e == EstadoTarea.ATRASADA || e == EstadoTarea.RECHAZADA
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis tareas") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (tareas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No tienes tareas asignadas por ahora.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Tareas por hacer: $porHacer", style = MaterialTheme.typography.labelLarge)
                }
                items(tareas, key = { it.id }) { tarea ->
                    val cultivo = cultivos.firstOrNull { it.id == tarea.cultivoId }?.nombre ?: "Cultivo"
                    TareaCard(tarea, "Cultivo: $cultivo") { onAbrir(tarea.id) }
                }
            }
        }
    }
}
