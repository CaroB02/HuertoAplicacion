package com.example.huertoaplicaciones.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.ui.components.CampoTexto
import com.example.huertoaplicaciones.util.EstadoSector
import com.example.huertoaplicaciones.viewmodel.SectorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorFormScreen(
    viewModel: SectorViewModel,
    onVolver: () -> Unit
) {
    val form by viewModel.form.collectAsState()

    LaunchedEffect(form.guardado) {
        if (form.guardado) onVolver()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (form.id == 0L) "Nuevo sector" else "Editar sector") },
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
                valor = form.codigo, etiqueta = "Código", icono = Icons.Default.Tag,
                hayError = form.errorCodigo, mensajeError = "El código es obligatorio",
                onCambio = { viewModel.cambiarCodigo(it) }
            )
            CampoTexto(
                valor = form.nombre, etiqueta = "Nombre", icono = Icons.Default.Edit,
                hayError = form.errorNombre, mensajeError = "El nombre es obligatorio",
                onCambio = { viewModel.cambiarNombre(it) }
            )
            CampoTexto(
                valor = form.descripcion, etiqueta = "Descripción", icono = Icons.Default.Info,
                hayError = form.errorDescripcion, mensajeError = "La descripción es obligatoria",
                onCambio = { viewModel.cambiarDescripcion(it) }
            )
            CampoTexto(
                valor = form.ubicacion, etiqueta = "Ubicación en el huerto", icono = Icons.Default.Place,
                hayError = form.errorUbicacion, mensajeError = "La ubicación es obligatoria",
                onCambio = { viewModel.cambiarUbicacion(it) }
            )

            Text("Estado del sector", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    EstadoSector.DISPONIBLE, EstadoSector.EN_USO,
                    EstadoSector.EN_MANTENIMIENTO, EstadoSector.FUERA_DE_USO
                ).forEach { estado ->
                    FilterChip(
                        selected = form.estado == estado,
                        onClick = { viewModel.cambiarEstado(estado) },
                        label = { Text(estado) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.guardar() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Guardar sector")
            }
        }
    }
}