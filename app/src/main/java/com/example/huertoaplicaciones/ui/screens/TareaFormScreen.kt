package com.example.huertoaplicaciones.ui.screens

import com.example.huertoaplicaciones.ui.components.CampoFecha
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.ui.components.CampoTexto
import com.example.huertoaplicaciones.ui.components.TextoError
import com.example.huertoaplicaciones.util.TipoTarea
import com.example.huertoaplicaciones.viewmodel.TareaViewModel

@OptIn(ExperimentalMaterial3Api::class)
// Formulario para CREAR, EDITAR y ASIGNAR una tarea (solo docente). Se elige tipo, sector, cultivo y
// estudiante responsable. Las reglas (campos obligatorios, responsable válido, cultivo del sector)
// las valida el TareaViewModel, no esta pantalla.
@Composable
fun TareaFormScreen(
    viewModel: TareaViewModel,
    usuario: Usuario,
    onVolver: () -> Unit
) {
    val form by viewModel.form.collectAsState()
    val sectores by viewModel.sectores.collectAsState()
    val cultivos by viewModel.cultivos.collectAsState()
    val estudiantes by viewModel.estudiantes.collectAsState()

    LaunchedEffect(form.guardado) {
        if (form.guardado) onVolver()
    }

    // Solo ofrezco los cultivos del sector elegido (un cultivo pertenece a un único sector).
    val cultivosDelSector = cultivos.filter { it.sectorId == form.sectorId }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                title = { Text(if (form.id == 0L) "Nueva tarea" else "Editar tarea") },
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
            Text("Tipo de tarea", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TipoTarea.todos.forEach { tipo ->
                    FilterChip(
                        selected = form.tipo == tipo,
                        onClick = { viewModel.cambiarTipo(tipo) },
                        label = { Text(tipo) }
                    )
                }
            }

            CampoTexto(
                valor = form.descripcion, etiqueta = "Descripción", icono = Icons.Default.Edit,
                hayError = form.errorDescripcion, mensajeError = "La descripción es obligatoria",
                onCambio = { viewModel.cambiarDescripcion(it) }
            )
            CampoTexto(
                valor = form.instrucciones, etiqueta = "Instrucciones para el estudiante",
                icono = Icons.Default.Info,
                hayError = form.errorInstrucciones, mensajeError = "Obligatorias (máx. 300 caracteres)",
                onCambio = { viewModel.cambiarInstrucciones(it) }
            )

            Text("Sector", style = MaterialTheme.typography.labelLarge)
            if (sectores.isEmpty()) {
                TextoError("No hay sectores. Crea uno primero.")
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

            Text("Cultivo", style = MaterialTheme.typography.labelLarge)
            // Tres casos para el selector de cultivo: sin sector elegido, sector sin cultivos, o lista normal.
            when {
                form.sectorId == null -> Text("Elige primero un sector")
                cultivosDelSector.isEmpty() ->
                    TextoError("Este sector no tiene cultivos. Registra uno en Gestionar cultivos.")
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
            if (form.errorCultivo) TextoError("Seleccione un cultivo")

            Text("Asignar a", style = MaterialTheme.typography.labelLarge)
            if (estudiantes.isEmpty()) {
                TextoError("No hay estudiantes registrados.")
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    estudiantes.forEach { est ->
                        FilterChip(
                            selected = form.estudianteId == est.id,
                            onClick = { viewModel.cambiarEstudiante(est.id) },
                            label = { Text("${est.nombre} (${est.grupo})") }
                        )
                    }
                }
            }
            if (form.errorEstudiante) TextoError("Seleccione un estudiante")

            // La fecha se elige en un calendario (no se escribe a mano), así no hay errores de formato.
            CampoFecha(
                valor = form.fechaProgramada, etiqueta = "Fecha programada",
                hayError = form.errorFecha,
                mensajeError = "Elige una fecha de hoy en adelante",
                onCambio = { viewModel.cambiarFecha(it) },
                desdeHoy = true     // los días anteriores a hoy quedan desactivados en el calendario
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                // El botón sirve para crear y para guardar cambios; el ViewModel distingue por el id del formulario.
                onClick = { viewModel.guardar(usuario) },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (form.id == 0L) "Crear y asignar tarea" else "Guardar cambios")
            }
        }
    }
}