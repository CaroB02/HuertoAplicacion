package com.example.huertoaplicaciones.ui.screens

import com.example.huertoaplicaciones.ui.components.CampoFecha
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.ui.components.CampoTexto
import com.example.huertoaplicaciones.ui.components.ConfirmarEliminar
import com.example.huertoaplicaciones.ui.components.SelectorFoto
import com.example.huertoaplicaciones.ui.components.TextoError
import com.example.huertoaplicaciones.util.EstadoCultivo
import com.example.huertoaplicaciones.viewmodel.CultivoViewModel

// Formulario de cultivos (crear y editar). Es el que tiene más validaciones: fechas coherentes
// (la cosecha no puede ser antes de la siembra), frecuencia de riego positiva y sector obligatorio.
// También permite adjuntar una foto con la galería o la cámara, y eliminar el cultivo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivoFormScreen(
    viewModel: CultivoViewModel,
    onVolver: () -> Unit
) {
    // Todo el estado del formulario vive en el CultivoViewModel; la pantalla solo lo dibuja.
    val form by viewModel.form.collectAsState()
    val sectores by viewModel.sectores.collectAsState()
    // Estado local: controla si se muestra la ventana de confirmación para eliminar.
    var confirmarBorrado by remember { mutableStateOf(false) }

    LaunchedEffect(form.guardado) {
        if (form.guardado) onVolver()
    }

    // Ventana de confirmación antes de eliminar. Si el cultivo tiene tareas o reportes, la base
    // no lo permite y el ViewModel muestra el mensaje de error.
    if (confirmarBorrado) {
        ConfirmarEliminar(
            titulo = "Eliminar cultivo",
            mensaje = "¿Eliminar ${form.nombre}? Solo es posible si no tiene tareas ni reportes.",
            onConfirmar = {
                confirmarBorrado = false
                viewModel.eliminarCultivo(form.id) { eliminado -> if (eliminado) onVolver() }
            },
            onCancelar = { confirmarBorrado = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                title = { Text(if (form.id == 0L) "Nuevo cultivo" else "Editar cultivo") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CampoTexto(
                valor = form.nombre, etiqueta = "Nombre del cultivo", icono = Icons.Default.Edit,
                hayError = form.errorNombre, mensajeError = "Obligatorio (máx. 60 caracteres)",
                onCambio = { viewModel.cambiarNombre(it) }
            )
            CampoTexto(
                valor = form.descripcion, etiqueta = "Descripción", icono = Icons.Default.Info,
                hayError = form.errorDescripcion, mensajeError = "Obligatoria (máx. 200 caracteres)",
                onCambio = { viewModel.cambiarDescripcion(it) }
            )

            Text("Sector del cultivo", style = MaterialTheme.typography.labelLarge)
            if (sectores.isEmpty()) {
                TextoError("No hay sectores. Crea uno primero en Gestionar sectores.")
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sectores.forEach { sector ->
                        FilterChip(
                            selected = form.sectorId == sector.id,
                            onClick = { viewModel.cambiarSector(sector.id) },
                            label = { Text("${sector.codigo} · ${sector.nombre}") }
                        )
                    }
                }
            }
            if (form.errorSector) {
                TextoError("Seleccione un sector")
            }

            // Las tres fechas se eligen en un calendario; las validaciones siguen en el ViewModel.
            CampoFecha(
                valor = form.fechaSiembra, etiqueta = "Fecha de siembra",
                hayError = form.errorFechaSiembra, mensajeError = "Elige la fecha de siembra",
                onCambio = { viewModel.cambiarFechaSiembra(it) }
            )
            CampoFecha(
                valor = form.fechaTrasplante, etiqueta = "Fecha de trasplante (opcional)",
                hayError = form.errorFechaTrasplante,
                mensajeError = "Debe estar entre la siembra y la cosecha",
                onCambio = { viewModel.cambiarFechaTrasplante(it) },
                opcional = true
            )
            CampoFecha(
                valor = form.fechaCosecha, etiqueta = "Cosecha estimada",
                hayError = form.errorFechaCosecha,
                mensajeError = "Elige una fecha que no sea anterior a la siembra",
                onCambio = { viewModel.cambiarFechaCosecha(it) }
            )
            CampoTexto(
                valor = form.frecuenciaRiego, etiqueta = "Riego cada (días)",
                icono = Icons.Default.Refresh,
                hayError = form.errorFrecuencia, mensajeError = "Ingrese un número mayor a 0",
                onCambio = { viewModel.cambiarFrecuencia(it) },
                tipoTeclado = KeyboardType.Number
            )

            Text("Estado del cultivo", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    EstadoCultivo.PLANIFICADO, EstadoCultivo.SEMBRADO,
                    EstadoCultivo.EN_CRECIMIENTO, EstadoCultivo.LISTO_COSECHA,
                    EstadoCultivo.COSECHADO, EstadoCultivo.CON_PROBLEMAS,
                    EstadoCultivo.FINALIZADO
                ).forEach { estado ->
                    FilterChip(
                        selected = form.estado == estado,
                        onClick = { viewModel.cambiarEstado(estado) },
                        label = { Text(estado) }
                    )
                }
            }

            CampoTexto(
                valor = form.observaciones, etiqueta = "Observaciones (opcional)",
                icono = Icons.Default.Edit,
                hayError = false, mensajeError = "",
                onCambio = { viewModel.cambiarObservaciones(it) }
            )

            Text("Fotografía del cultivo (opcional)", style = MaterialTheme.typography.labelLarge)
            // Componente reutilizable de recursos nativos (galería y cámara). Aquí solo permite 1 foto.
            SelectorFoto(
                fotos = listOfNotNull(form.fotoUri),
                onFotoAgregada = { viewModel.cambiarFoto(it) },
                onFotoQuitada = { viewModel.cambiarFoto(null) },
                maxFotos = 1
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.guardar() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Guardar cultivo")
            }
            if (form.id != 0L) {
                OutlinedButton(
                    onClick = { confirmarBorrado = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Eliminar cultivo")
                }
            }
        }
    }
}
