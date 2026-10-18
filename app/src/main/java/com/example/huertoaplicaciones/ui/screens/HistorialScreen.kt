package com.example.huertoaplicaciones.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.ui.theme.ColorAdvertencia
import com.example.huertoaplicaciones.ui.theme.ColorError
import com.example.huertoaplicaciones.ui.theme.ColorExito
import com.example.huertoaplicaciones.ui.theme.ColorInfo
import com.example.huertoaplicaciones.util.formatearFechaHora
import com.example.huertoaplicaciones.viewmodel.EventoHistorial
import com.example.huertoaplicaciones.viewmodel.HistorialViewModel
import com.example.huertoaplicaciones.viewmodel.TipoEvento

// Historial de actividades: una línea de tiempo con lo que ha pasado en el huerto.
// El docente ve todo y el estudiante solo lo suyo (eso lo decide el HistorialViewModel).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel,
    onVolver: () -> Unit
) {
    val eventos by viewModel.eventos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                title = { Text("Historial de actividades") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (eventos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Todavía no hay actividades registradas.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(eventos) { evento -> EventoItem(evento) }
            }
        }
    }
}

// Cada tipo de evento tiene su ícono y su color, para reconocerlo de un vistazo.
private fun iconoDe(tipo: TipoEvento): ImageVector = when (tipo) {
    TipoEvento.TAREA_CREADA -> Icons.Default.AddTask
    TipoEvento.TAREA_ENTREGADA -> Icons.Default.Upload
    TipoEvento.VALIDACION -> Icons.Default.CheckCircle
    TipoEvento.RECHAZO -> Icons.Default.Cancel
    TipoEvento.PROBLEMA -> Icons.Default.Report
    TipoEvento.PROBLEMA_ATENDIDO -> Icons.Default.TaskAlt
}

private fun colorDe(tipo: TipoEvento): Color = when (tipo) {
    TipoEvento.TAREA_CREADA -> ColorInfo
    TipoEvento.TAREA_ENTREGADA -> ColorAdvertencia
    TipoEvento.VALIDACION -> ColorExito
    TipoEvento.RECHAZO -> ColorError
    TipoEvento.PROBLEMA -> ColorError
    TipoEvento.PROBLEMA_ATENDIDO -> ColorExito
}

// Una fila del historial: ícono, título, detalle y fecha con hora.
@Composable
private fun EventoItem(evento: EventoHistorial) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = iconoDe(evento.tipo),
                contentDescription = null,
                tint = colorDe(evento.tipo)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(evento.titulo, style = MaterialTheme.typography.titleSmall)
                if (evento.detalle.isNotBlank()) Text(evento.detalle, style = MaterialTheme.typography.bodyMedium)
                Text(formatearFechaHora(evento.fecha), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
