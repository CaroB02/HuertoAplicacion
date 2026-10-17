package com.example.huertoaplicaciones.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.ui.components.CampoTexto
import com.example.huertoaplicaciones.util.EstadoCultivo
import com.example.huertoaplicaciones.viewmodel.CultivoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivoFormScreen(
    viewModel: CultivoViewModel,
    onVolver: () -> Unit
) {
    val form by viewModel.form.collectAsState()
    val sectores by viewModel.sectores.collectAsState()

    LaunchedEffect(form.guardado) {
        if (form.guardado) onVolver()
    }

    Scaffold(
        topBar = {
            TopAppBar(
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
                hayError = form.errorNombre, mensajeError = "El nombre es obligatorio",
                onCambio = { viewModel.cambiarNombre(it) }
            )
            CampoTexto(
                valor = form.descripcion, etiqueta = "Descripción", icono = Icons.Default.Info,
                hayError = form.errorDescripcion, mensajeError = "La descripción es obligatoria",
                onCambio = { viewModel.cambiarDescripcion(it) }
            )

            Text("Sector del cultivo", style = MaterialTheme.typography.labelLarge)
            if (sectores.isEmpty()) {
                Text(
                    "No hay sectores. Crea uno primero en Gestionar sectores.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
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
                Text(
                    "Seleccione un sector",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTexto(
                valor = form.fechaSiembra, etiqueta = "Fecha de siembra (dd/mm/aaaa)",
                icono = Icons.Default.DateRange,
                hayError = form.errorFechaSiembra, mensajeError = "Use el formato dd/mm/aaaa",
                onCambio = { viewModel.cambiarFechaSiembra(it) }
            )
            CampoTexto(
                valor = form.fechaCosecha, etiqueta = "Cosecha estimada (dd/mm/aaaa)",
                icono = Icons.Default.DateRange,
                hayError = form.errorFechaCosecha,
                mensajeError = "Fecha inválida o anterior a la siembra",
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

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.guardar() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Guardar cultivo")
            }
        }
    }
}
