package com.example.huertoaplicaciones.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.ui.components.CampoTexto
import com.example.huertoaplicaciones.ui.components.SelectorFoto
import com.example.huertoaplicaciones.ui.components.TextoError
import com.example.huertoaplicaciones.util.TipoProblema
import com.example.huertoaplicaciones.viewmodel.ProblemaViewModel

@OptIn(ExperimentalMaterial3Api::class)
// Formulario para reportar un problema del huerto (estudiante o docente): tipo, descripción,
// sector, cultivo afectado y una foto opcional con galería o cámara.
@Composable
fun ReporteFormScreen(
    viewModel: ProblemaViewModel,
    usuario: Usuario,
    onVolver: () -> Unit
) {
    val form by viewModel.form.collectAsState()
    val sectores by viewModel.sectores.collectAsState()
    val cultivos by viewModel.cultivos.collectAsState()

    LaunchedEffect(form.guardado) {
        if (form.guardado) onVolver()
    }

    // Solo se ofrecen los cultivos del sector elegido.
    val cultivosDelSector = cultivos.filter { it.sectorId == form.sectorId }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                title = { Text("Reportar problema") },
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
            Text("Tipo de problema", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TipoProblema.todos.forEach { tipo ->
                    FilterChip(
                        selected = form.tipo == tipo,
                        onClick = { viewModel.cambiarTipo(tipo) },
                        label = { Text(tipo) }
                    )
                }
            }

            CampoTexto(
                valor = form.descripcion, etiqueta = "Describe brevemente el problema",
                icono = Icons.Default.Info,
                hayError = form.errorDescripcion,
                mensajeError = "Obligatoria (máx. 300 caracteres)",
                onCambio = { viewModel.cambiarDescripcion(it) }
            )

            Text("Sector", style = MaterialTheme.typography.labelLarge)
            if (sectores.isEmpty()) {
                TextoError("No hay sectores registrados todavía.")
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
            if (form.errorSector) TextoError("Seleccione un sector")

            Text("Cultivo afectado", style = MaterialTheme.typography.labelLarge)
            when {
                form.sectorId == null -> Text("Elige primero un sector")
                cultivosDelSector.isEmpty() -> TextoError("Este sector no tiene cultivos registrados.")
                else -> Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cultivosDelSector.forEach { cultivo ->
                        FilterChip(
                            selected = form.cultivoId == cultivo.id,
                            onClick = { viewModel.cambiarCultivo(cultivo.id) },
                            label = { Text(cultivo.nombre) }
                        )
                    }
                }
            }
            if (form.errorCultivo) TextoError("Seleccione un cultivo del sector")

            Text("Fotografía (opcional)", style = MaterialTheme.typography.labelLarge)
            SelectorFoto(
                fotos = form.fotos,
                onFotoAgregada = { viewModel.agregarFoto(it) },
                onFotoQuitada = { viewModel.quitarFoto(it) },
                maxFotos = 1
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                // El ViewModel valida los campos obligatorios y que el cultivo pertenezca al sector antes de guardar.
                onClick = { viewModel.guardar(usuario) },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Enviar reporte")
            }
        }
    }
}
