package com.example.huertoaplicaciones.ui.screens

import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.huertoaplicaciones.ui.components.IconoCircular
import com.example.huertoaplicaciones.ui.components.EstadoChip
import com.example.huertoaplicaciones.ui.components.FilaInfo
import com.example.huertoaplicaciones.ui.components.iconoEstadoProblema
import com.example.huertoaplicaciones.ui.components.iconoTipoProblema
import com.example.huertoaplicaciones.ui.theme.ColorError
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.huertoaplicaciones.data.entity.Problema
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.ui.components.CampoTexto
import com.example.huertoaplicaciones.ui.theme.ColorTextoSobreContenedor
import com.example.huertoaplicaciones.ui.theme.colorEstadoProblema
import com.example.huertoaplicaciones.util.EstadoProblema
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.util.formatearFechaHora
import com.example.huertoaplicaciones.viewmodel.ProblemaViewModel

@OptIn(ExperimentalMaterial3Api::class)
// Lista de reportes de problemas. La usan los dos perfiles: el docente ve TODOS y puede gestionarlos;
// el estudiante ve solo los que él mismo envió.
@Composable
fun ReportesScreen(
    viewModel: ProblemaViewModel,
    usuario: Usuario,
    onNuevo: () -> Unit,
    onVolver: () -> Unit
) {
    // Quién mira la pantalla decide qué reportes se cargan y si aparece el botón de gestionar.
    val esDocente = usuario.perfil == Perfil.ADMIN
    // El docente ve todos los reportes; el estudiante solo los que él mismo envió
    val todos by viewModel.reportes.collectAsState()
    val propiosFlujo = remember(usuario.id) { viewModel.reportesDe(usuario.id) }
    val propios by propiosFlujo.collectAsState(initial = emptyList<Problema>())
    // PRIVILEGIO MÍNIMO: un estudiante nunca recibe los reportes de otras personas.
    val reportes = if (esDocente) todos else propios

    val sectores by viewModel.sectores.collectAsState()
    val cultivos by viewModel.cultivos.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                title = { Text(if (esDocente) "Problemas reportados" else "Mis reportes") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) {
                Icon(Icons.Default.Add, contentDescription = "Reportar problema")
            }
        }
    ) { innerPadding ->
        if (reportes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay reportes todavía. Presiona + para informar un problema del huerto.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reportes, key = { it.id }) { problema ->
                    ReporteCard(
                        problema = problema,
                        sector = sectores.firstOrNull { it.id == problema.sectorId }?.nombre ?: "-",
                        cultivo = cultivos.firstOrNull { it.id == problema.cultivoId }?.nombre ?: "-",
                        reportante = usuarios.firstOrNull { it.id == problema.reportadoPorId }?.nombre ?: "-",
                        docente = if (esDocente) usuario else null,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// La tarjeta de un reporte. Para el docente se puede expandir y cambiar su estado de atención.
@Composable
private fun ReporteCard(
    problema: Problema,
    sector: String,
    cultivo: String,
    reportante: String,
    docente: Usuario?,
    viewModel: ProblemaViewModel
) {
    // ANIMACIÓN: el color de la tarjeta cambia suavemente cuando cambia el estado del reporte.
    val color by animateColorAsState(
        targetValue = colorEstadoProblema(problema.estado),
        animationSpec = tween(durationMillis = 500),
        label = "colorEstadoProblema"
    )
    // Estado LOCAL de la tarjeta: si el panel de atención está abierto, el estado elegido y el comentario.
    var atendiendo by remember(problema.id) { mutableStateOf(false) }
    var estadoElegido by remember(problema.id, problema.estado) { mutableStateOf(problema.estado) }
    var comentario by remember(problema.id) { mutableStateOf(problema.comentarioDocente) }
    var errorComentario by remember(problema.id) { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color, contentColor = ColorTextoSobreContenedor
        ),
        // Animación: la tarjeta crece suavemente al abrir el panel de atención
        // ANIMACIÓN: animateContentSize hace que la tarjeta crezca o se encoja suavemente al abrir el panel.
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Encabezado: ícono según el tipo de problema, el tipo y el estado como "chip".
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconoCircular(iconoTipoProblema(problema.tipo), fondo = Color.White.copy(alpha = 0.65f), tinte = ColorError)
                Text(
                    problema.tipo,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                EstadoChip(problema.estado, iconoEstadoProblema(problema.estado), Color.White.copy(alpha = 0.65f))
            }
            Text(problema.descripcion)
            FilaInfo(Icons.Default.Place, "Sector: $sector · Cultivo: $cultivo")
            FilaInfo(
                Icons.Default.Person,
                "Reportado por $reportante el ${formatearFechaHora(problema.fechaReporte)}"
            )
            problema.fotoUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Foto del problema",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp))
                )
            }
            if (problema.comentarioDocente.isNotBlank() && !atendiendo) {
                Text("Comentario del docente: ${problema.comentarioDocente}")
            }
            problema.fechaCierre?.let {
                Text("Atendido el ${formatearFechaHora(it)}", style = MaterialTheme.typography.bodySmall)
            }

            // Solo el docente cambia el estado de atención
            // Solo el DOCENTE ve este bloque (docente es null para los estudiantes): cambiar el estado de atención.
            if (docente != null) {
                if (!atendiendo) {
                    TextButton(onClick = { atendiendo = true }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Gestionar atención")
                    }
                } else {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EstadoProblema.todos.forEach { estado ->
                            FilterChip(
                                selected = estadoElegido == estado,
                                onClick = {
                                    estadoElegido = estado
                                    errorComentario = false
                                },
                                label = { Text(estado) }
                            )
                        }
                    }
                    CampoTexto(
                        valor = comentario, etiqueta = "Comentario del docente",
                        icono = Icons.Default.Edit,
                        hayError = errorComentario,
                        mensajeError = "Explica cómo se atendió el problema",
                        onCambio = {
                            comentario = it
                            errorComentario = false
                        }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            val valido = viewModel.cambiarEstadoAtencion(
                                problema, docente, estadoElegido, comentario
                            )
                            if (valido) atendiendo = false else errorComentario = true
                        }) { Text("Guardar") }
                        OutlinedButton(onClick = { atendiendo = false }) { Text("Cancelar") }
                    }
                }
            }
        }
    }
}
