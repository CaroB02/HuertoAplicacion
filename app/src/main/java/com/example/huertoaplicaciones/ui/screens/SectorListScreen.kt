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
import com.example.huertoaplicaciones.data.entity.Sector
import com.example.huertoaplicaciones.util.EstadoSector
import com.example.huertoaplicaciones.viewmodel.SectorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorListScreen(
    viewModel: SectorViewModel,
    onNuevo: () -> Unit,
    onEditar: (Long) -> Unit,
    onVolver: () -> Unit
) {
    val sectores by viewModel.sectores.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sectores") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo sector")
            }
        }
    ) { innerPadding ->
        if (sectores.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no hay sectores. Presiona + para crear uno.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sectores, key = { it.id }) { sector ->
                    SectorCard(sector = sector, onClick = { onEditar(sector.id) })
                }
            }
        }
    }
}

@Composable
private fun SectorCard(sector: Sector, onClick: () -> Unit) {
    val colorObjetivo = when (sector.estado) {
        EstadoSector.DISPONIBLE -> Color(0xFFC8E6C9)
        EstadoSector.EN_USO -> Color(0xFFBBDEFB)
        EstadoSector.EN_MANTENIMIENTO -> Color(0xFFFFE0B2)
        else -> Color(0xFFE0E0E0)
    }
    val color by animateColorAsState(
        targetValue = colorObjetivo,
        animationSpec = tween(durationMillis = 500),
        label = "colorEstadoSector"
    )

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color, contentColor = Color.Black),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${sector.codigo} · ${sector.nombre}", style = MaterialTheme.typography.titleMedium)
            Text(sector.ubicacion)
            Text("Estado: ${sector.estado}", style = MaterialTheme.typography.labelLarge)
        }
    }
}