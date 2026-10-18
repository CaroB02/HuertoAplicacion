package com.example.huertoaplicaciones.ui.screens

import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.huertoaplicaciones.ui.components.IconoCircular
import com.example.huertoaplicaciones.ui.components.EstadoChip
import com.example.huertoaplicaciones.ui.components.FilaInfo
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.huertoaplicaciones.ui.components.iconoEstadoSector
import com.example.huertoaplicaciones.ui.theme.ColorExito
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
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.data.entity.Sector
import com.example.huertoaplicaciones.ui.theme.ColorTextoSobreContenedor
import com.example.huertoaplicaciones.ui.theme.colorEstadoSector
import com.example.huertoaplicaciones.viewmodel.SectorViewModel

// Lista de sectores (solo docente). Muestra una tarjeta por sector; al tocarla abre su detalle.
// El botón "+" crea un sector nuevo. Si no hay sectores muestra un mensaje de estado vacío.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorListScreen(
    viewModel: SectorViewModel,
    onNuevo: () -> Unit,
    onAbrir: (Long) -> Unit,
    onVolver: () -> Unit
) {
    // La lista viene del ViewModel y se actualiza sola cuando se crea, edita o elimina un sector.
    val sectores by viewModel.sectores.collectAsState()

    // Scaffold = la estructura base de una pantalla Material: barra superior, botón flotante y contenido.
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
        // ESTADO VACÍO: si todavía no hay datos, explico qué hacer en vez de dejar la pantalla en blanco.
        if (sectores.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no hay sectores. Presiona + para crear uno.")
            }
        } else {
            // LazyColumn = lista vertical que solo dibuja las tarjetas visibles (eficiente aunque haya muchas).
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sectores, key = { it.id }) { sector ->
                    SectorCard(sector = sector, onClick = { onAbrir(sector.id) })
                }
            }
        }
    }
}

// La tarjeta de un sector. Su color depende del estado (Disponible, En uso...) y cambia con animación.
@Composable
private fun SectorCard(sector: Sector, onClick: () -> Unit) {
    val color by animateColorAsState(
        targetValue = colorEstadoSector(sector.estado),
        animationSpec = tween(durationMillis = 500),
        label = "colorEstadoSector"
    )

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = color, contentColor = ColorTextoSobreContenedor
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Ícono del sector a la izquierda; a la derecha el nombre, la ubicación y el estado como "chip".
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconoCircular(Icons.Default.Yard, fondo = Color.White.copy(alpha = 0.65f), tinte = ColorExito)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "${sector.codigo} · ${sector.nombre}",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                )
                FilaInfo(Icons.Default.Place, sector.ubicacion)
                EstadoChip(sector.estado, iconoEstadoSector(sector.estado), Color.White.copy(alpha = 0.65f))
            }
        }
    }
}
