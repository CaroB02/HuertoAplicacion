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

// Formulario para crear o editar un sector. Usa el componente CampoTexto, que ya trae el ícono,
// el borde rojo y el mensaje de error. Las reglas de validación están en el SectorViewModel.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorFormScreen(
    viewModel: SectorViewModel,
    onVolver: () -> Unit
) {
    // El formulario completo (lo escrito y los errores) vive en el ViewModel; aquí solo lo leo.
    val form by viewModel.form.collectAsState()

    // Cuando el ViewModel guarda correctamente, "guardado" pasa a true y vuelvo a la pantalla anterior.
    LaunchedEffect(form.guardado) {
        if (form.guardado) onVolver()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                hayError = form.errorCodigo, mensajeError = "Obligatorio (máx. 10 caracteres)",
                onCambio = { viewModel.cambiarCodigo(it) }
            )
            CampoTexto(
                valor = form.nombre, etiqueta = "Nombre", icono = Icons.Default.Edit,
                hayError = form.errorNombre, mensajeError = "Obligatorio (máx. 60 caracteres)",
                onCambio = { viewModel.cambiarNombre(it) }
            )
            CampoTexto(
                valor = form.descripcion, etiqueta = "Descripción", icono = Icons.Default.Info,
                hayError = form.errorDescripcion, mensajeError = "Obligatoria (máx. 200 caracteres)",
                onCambio = { viewModel.cambiarDescripcion(it) }
            )
            CampoTexto(
                valor = form.ubicacion, etiqueta = "Ubicación en el huerto", icono = Icons.Default.Place,
                hayError = form.errorUbicacion, mensajeError = "Obligatoria (máx. 60 caracteres)",
                onCambio = { viewModel.cambiarUbicacion(it) }
            )

            Text("Estado del sector", style = MaterialTheme.typography.labelLarge)
            Row(
                // Los botones de estado se pueden deslizar horizontalmente si no caben en el ancho de la pantalla.
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    EstadoSector.DISPONIBLE, EstadoSector.EN_USO,
                    EstadoSector.EN_MANTENIMIENTO, EstadoSector.FUERA_DE_USO
                ).forEach { estado ->
                    // FilterChip = botón de selección: marca el estado actual del sector (solo uno a la vez).
                    FilterChip(
                        selected = form.estado == estado,
                        onClick = { viewModel.cambiarEstado(estado) },
                        label = { Text(estado) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                // Al presionar Guardar, el ViewModel valida TODO; si hay errores no guarda y marca los campos en rojo.
                onClick = { viewModel.guardar() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Guardar sector")
            }
        }
    }
}