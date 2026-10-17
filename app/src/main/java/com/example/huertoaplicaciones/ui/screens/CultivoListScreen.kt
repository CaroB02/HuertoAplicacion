package com.example.huertoaplicaciones.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.util.EstadoCultivo
import com.example.huertoaplicaciones.util.formatearFecha
import com.example.huertoaplicaciones.viewmodel.CultivoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivoListScreen(
    viewModel: CultivoViewModel,
    onNuevo: () -> Unit,
    onEditar: (Long) -> Unit,
    onVolver: () -> Unit
) {
    val cultivos by viewModel.cultivos.collectAsState()
    val sectores by viewModel.sectores.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cultivos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo cultivo")
            }
        }
    ) { innerPadding ->
        if (cultivos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no hay cultivos. Presiona + para registrar uno.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cultivos, key = { it.id }) { cultivo ->
                    val nombreSector = sectores.firstOrNull { it.id == cultivo.sectorId }?.nombre
                        ?: "Sin sector"
                    CultivoCard(
                        cultivo = cultivo,
                        nombreSector = nombreSector,
                        onClick = { onEditar(cultivo.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CultivoCard(cultivo: Cultivo, nombreSector: String, onClick: () -> Unit) {
    val colorObjetivo = when (cultivo.estado) {
        EstadoCultivo.SEMBRADO -> Color(0xFFFFF9C4)
        EstadoCultivo.EN_CRECIMIENTO -> Color(0xFFC8E6C9)
        EstadoCultivo.LISTO_COSECHA -> Color(0xFFFFE0B2)
        EstadoCultivo.COSECHADO -> Color(0xFFBBDEFB)
        EstadoCultivo.CON_PROBLEMAS -> Color(0xFFFFCDD2)
        else -> Color(0xFFE0E0E0)
    }
    val color by animateColorAsState(
        targetValue = colorObjetivo,
        animationSpec = tween(durationMillis = 500),
        label = "colorEstadoCultivo"
    )

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color, contentColor = Color.Black),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(cultivo.nombre, style = MaterialTheme.typography.titleMedium)
            Text("Sector: $nombreSector")
            Text("Siembra: ${formatearFecha(cultivo.fechaSiembra)}")
            Text("Cosecha estimada: ${formatearFecha(cultivo.fechaCosecha)}")
            Text("Riego cada ${cultivo.frecuenciaRiegoDias} días")
            Text("Estado: ${cultivo.estado}", style = MaterialTheme.typography.labelLarge)
        }
    }
}