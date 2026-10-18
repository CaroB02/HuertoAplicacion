package com.example.huertoaplicaciones.ui.screens

import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.huertoaplicaciones.ui.components.EstadoChip
import com.example.huertoaplicaciones.ui.components.FilaInfo
import com.example.huertoaplicaciones.ui.components.IconoCircular
import com.example.huertoaplicaciones.ui.components.SeccionTitulo
import com.example.huertoaplicaciones.ui.components.iconoEstadoSector
import com.example.huertoaplicaciones.ui.theme.ColorExito
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.ui.components.ConfirmarEliminar
import com.example.huertoaplicaciones.ui.theme.ColorInfo
import com.example.huertoaplicaciones.ui.theme.ColorInfoContenedor
import com.example.huertoaplicaciones.ui.theme.ColorTextoSobreContenedor
import com.example.huertoaplicaciones.ui.theme.colorEstadoCultivo
import com.example.huertoaplicaciones.ui.theme.colorEstadoSector
import com.example.huertoaplicaciones.util.formatearFecha
import com.example.huertoaplicaciones.util.formatearFechaHora
import com.example.huertoaplicaciones.viewmodel.SectorViewModel

// Detalle de un sector: sus datos, la lectura simulada de sensores y los cultivos que tiene.
// Desde aquí el docente puede editar o eliminar el sector (con ventana de confirmación).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorDetalleScreen(
    viewModel: SectorViewModel,
    onEditar: (Long) -> Unit,
    onEditarCultivo: (Long) -> Unit,
    onVolver: () -> Unit
) {
    // Los tres datos de esta pantalla vienen del ViewModel y se actualizan solos.
    val sector by viewModel.sectorDetalle.collectAsState()
    val cultivos by viewModel.cultivosDelSector.collectAsState()
    val lectura by viewModel.lecturaDelSector.collectAsState()
    // Estado LOCAL de esta pantalla: si se muestra o no la ventana "¿Eliminar?".
    // remember lo conserva mientras la pantalla está viva; mutableStateOf hace que se redibuje al cambiar.
    var confirmarBorrado by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                title = { Text("Detalle del sector") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    sector?.let { s ->
                        IconButton(onClick = { onEditar(s.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar sector")
                        }
                        IconButton(onClick = { confirmarBorrado = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar sector")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val s = sector
        // Mientras el sector carga (es null) muestro "Cargando"; cuando llega, dibujo el detalle.
        if (s == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Cargando sector…") }
        } else {
            if (confirmarBorrado) {
                ConfirmarEliminar(
                    titulo = "Eliminar sector",
                    mensaje = "¿Eliminar ${s.nombre}? Solo es posible si no tiene cultivos, tareas ni reportes.",
                    onConfirmar = {
                        confirmarBorrado = false
                        viewModel.eliminarSector(s) { eliminado -> if (eliminado) onVolver() }
                    },
                    onCancelar = { confirmarBorrado = false }
                )
            }

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colorEstadoSector(s.estado),
                        contentColor = ColorTextoSobreContenedor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Encabezado: ícono del sector, su nombre y el estado como "chip".
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            IconoCircular(
                                Icons.Default.Yard,
                                fondo = Color.White.copy(alpha = 0.65f), tinte = ColorExito, tamano = 56.dp
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "${s.codigo} · ${s.nombre}",
                                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
                                )
                                EstadoChip(
                                    s.estado, iconoEstadoSector(s.estado),
                                    Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }
                        FilaInfo(Icons.Default.Place, "Ubicación: ${s.ubicacion}")
                        FilaInfo(Icons.Default.Info, s.descripcion)
                        Text(
                            "Creado el ${formatearFecha(s.fechaCreacion)} · " +
                                    "Actualizado el ${formatearFecha(s.fechaActualizacion)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                SeccionTitulo(Icons.Default.Sensors, "Sensores")
                val l = lectura
                // Estas lecturas son SIMULADAS: el estilo distinto y el rótulo "DATOS DE PRUEBA" las diferencian
                // de los datos que escriben las personas.
                if (l == null) {
                    Text("Este sector aún no tiene lecturas.")
                } else {
                    // Estilo distinto al resto de la información: son datos de prueba, no mediciones reales
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = ColorInfoContenedor,
                            contentColor = ColorTextoSobreContenedor
                        ),
                        border = BorderStroke(1.dp, ColorInfo),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Science, contentDescription = null, tint = ColorInfo)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "DATOS DE PRUEBA · ${l.origen}",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            Text("Humedad del suelo: ${l.humedadSuelo}%")
                            Text("Temperatura ambiental: ${l.temperatura} °C")
                            Text("Humedad ambiental: ${l.humedadAmbiente}%")
                            Text("Estado de la lectura: ${l.estado}", style = MaterialTheme.typography.labelLarge)
                            Text(
                                "Lectura ficticia del ${formatearFechaHora(l.fecha)}. No proviene de un sensor físico.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                SeccionTitulo(Icons.Default.Eco, "Cultivos del sector (${cultivos.size})")
                if (cultivos.isEmpty()) {
                    Text("Este sector aún no tiene cultivos registrados.")
                } else {
                    cultivos.forEach { cultivo ->
                        CultivoResumen(cultivo = cultivo, onClick = { onEditarCultivo(cultivo.id) })
                    }
                }
            }
        }
    }
}

// Una tarjeta pequeña con el resumen de un cultivo, dentro del detalle del sector.
@Composable
private fun CultivoResumen(cultivo: Cultivo, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorEstadoCultivo(cultivo.estado),
            contentColor = ColorTextoSobreContenedor
        ),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            cultivo.fotoUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Foto de ${cultivo.nombre}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(cultivo.nombre, style = MaterialTheme.typography.titleMedium)
                Text("Estado: ${cultivo.estado}")
                Text("Cosecha estimada: ${formatearFecha(cultivo.fechaCosecha)}")
                Text("Riego cada ${cultivo.frecuenciaRiegoDias} días")
            }
        }
    }
}
