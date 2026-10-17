package com.example.huertoaplicaciones.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.ui.components.CampoTexto
import com.example.huertoaplicaciones.ui.components.colorEstadoTarea
import com.example.huertoaplicaciones.util.EstadoTarea
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.util.estadoMostrado
import com.example.huertoaplicaciones.util.formatearFecha
import com.example.huertoaplicaciones.viewmodel.TareaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareaDetalleScreen(
    viewModel: TareaViewModel,
    usuario: Usuario,
    onVolver: () -> Unit
) {
    val tarea by viewModel.seleccionada.collectAsState()
    val sectores by viewModel.sectores.collectAsState()
    val cultivos by viewModel.cultivos.collectAsState()
    val estudiantes by viewModel.estudiantes.collectAsState()
    val errorComentario by viewModel.errorComentario.collectAsState()
    val accionHecha by viewModel.accionHecha.collectAsState()

    LaunchedEffect(accionHecha) {
        if (accionHecha) onVolver()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de la tarea") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        val t = tarea
        if (t == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Cargando tarea…") }
        } else {
            var comentario by remember(t.id) { mutableStateOf("") }

            val estadoActual = estadoMostrado(t)
            val sector = sectores.firstOrNull { it.id == t.sectorId }?.nombre ?: "-"
            val cultivo = cultivos.firstOrNull { it.id == t.cultivoId }?.nombre ?: "-"
            val alumno = estudiantes.firstOrNull { it.id == t.asignadoAId }?.nombre ?: "-"

            val esDueno = usuario.perfil == Perfil.ESTUDIANTE && t.asignadoAId == usuario.id
            val puedeHacerla = esDueno && (
                    t.estado == EstadoTarea.PENDIENTE ||
                            t.estado == EstadoTarea.EN_DESARROLLO ||
                            t.estado == EstadoTarea.RECHAZADA
                    )
            val puedeRevisar = usuario.perfil == Perfil.ADMIN &&
                    t.estado == EstadoTarea.PENDIENTE_VALIDACION

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colorEstadoTarea(estadoActual),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(t.tipo, style = MaterialTheme.typography.titleLarge)
                        Text("Estado: $estadoActual", style = MaterialTheme.typography.labelLarge)
                        Text("Sector: $sector")
                        Text("Cultivo: $cultivo")
                        Text("Asignada a: $alumno")
                        Text("Fecha programada: ${formatearFecha(t.fechaProgramada)}")
                        t.fechaRealizacion?.let { Text("Realizada el: ${formatearFecha(it)}") }
                    }
                }

                Text("Descripción", style = MaterialTheme.typography.labelLarge)
                Text(t.descripcion)
                Text("Instrucciones", style = MaterialTheme.typography.labelLarge)
                Text(t.instrucciones)

                if (t.comentarioEstudiante.isNotBlank()) {
                    Text("Comentario del estudiante", style = MaterialTheme.typography.labelLarge)
                    Text(t.comentarioEstudiante)
                }
                if (t.comentarioDocente.isNotBlank()) {
                    Text("Comentario del docente", style = MaterialTheme.typography.labelLarge)
                    Text(t.comentarioDocente)
                }

                if (esDueno && t.estado == EstadoTarea.PENDIENTE_VALIDACION) {
                    Text(
                        "Tu trabajo está pendiente de validación por el docente.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (puedeHacerla) {
                    CampoTexto(
                        valor = comentario, etiqueta = "Comentario (opcional)",
                        icono = Icons.Default.Edit, hayError = false, mensajeError = "",
                        onCambio = { comentario = it }
                    )
                    Button(
                        onClick = { viewModel.marcarRealizada(t, usuario, comentario) },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { Text("Marcar como realizada") }
                }

                if (puedeRevisar) {
                    CampoTexto(
                        valor = comentario, etiqueta = "Comentario del docente",
                        icono = Icons.Default.Edit,
                        hayError = errorComentario,
                        mensajeError = "Para rechazar debes explicar el motivo",
                        onCambio = {
                            comentario = it
                            viewModel.limpiarErrorComentario()
                        }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.validar(t, usuario, comentario) },
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) { Text("Validar") }
                        OutlinedButton(
                            onClick = { viewModel.rechazar(t, usuario, comentario) },
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) { Text("Rechazar") }
                    }
                }
            }
        }
    }
}
