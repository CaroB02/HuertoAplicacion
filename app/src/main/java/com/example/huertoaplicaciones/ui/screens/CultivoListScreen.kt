package com.example.huertoaplicaciones.ui.screens

import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.huertoaplicaciones.ui.components.IconoCircular
import com.example.huertoaplicaciones.ui.components.EstadoChip
import com.example.huertoaplicaciones.ui.components.FilaInfo
import com.example.huertoaplicaciones.ui.components.iconoEstadoCultivo
import com.example.huertoaplicaciones.ui.theme.ColorExito
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.ui.theme.ColorTextoSobreContenedor
import com.example.huertoaplicaciones.ui.theme.colorEstadoCultivo
import com.example.huertoaplicaciones.util.formatearFecha
import com.example.huertoaplicaciones.viewmodel.CultivoViewModel

// Lista de cultivos (solo docente): una tarjeta por cultivo con su sector, fechas, riego y estado.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivoListScreen(
    viewModel: CultivoViewModel,
    onNuevo: () -> Unit,
    onEditar: (Long) -> Unit,
    onVolver: () -> Unit
) {
    // Necesito la lista de sectores solo para escribir el NOMBRE del sector en cada tarjeta
    // (el cultivo guarda únicamente el id del sector).
    val cultivos by viewModel.cultivos.collectAsState()
    val sectores by viewModel.sectores.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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

// La tarjeta de un cultivo: su color depende del estado y muestra la foto a la derecha si tiene.
@Composable
private fun CultivoCard(cultivo: Cultivo, nombreSector: String, onClick: () -> Unit) {
    val color by animateColorAsState(
        targetValue = colorEstadoCultivo(cultivo.estado),
        animationSpec = tween(durationMillis = 500),
        label = "colorEstadoCultivo"
    )

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = color, contentColor = ColorTextoSobreContenedor
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconoCircular(Icons.Default.Eco, fondo = Color.White.copy(alpha = 0.65f), tinte = ColorExito)
            // Cada dato lleva su propio ícono, así la tarjeta se lee de un vistazo.
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(cultivo.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FilaInfo(Icons.Default.Place, "Sector: $nombreSector")
                FilaInfo(Icons.Default.Grass, "Siembra: ${formatearFecha(cultivo.fechaSiembra)}")
                cultivo.fechaTrasplante?.let {
                    FilaInfo(Icons.Default.Yard, "Trasplante: ${formatearFecha(it)}")
                }
                FilaInfo(Icons.Default.Agriculture, "Cosecha estimada: ${formatearFecha(cultivo.fechaCosecha)}")
                FilaInfo(Icons.Default.WaterDrop, "Riego cada ${cultivo.frecuenciaRiegoDias} días")
                EstadoChip(cultivo.estado, iconoEstadoCultivo(cultivo.estado), Color.White.copy(alpha = 0.65f))
            }
            cultivo.fotoUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Foto de ${cultivo.nombre}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}
