package com.example.huertoaplicaciones.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PanelBase(
    titulo: String,
    saludo: String,
    descripcion: String,
    onCerrarSesion: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(saludo, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(descripcion, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun HomeAdminScreen(nombre: String, onCerrarSesion: () -> Unit) {
    PanelBase(
        titulo = "Panel del docente",
        saludo = "Hola, $nombre",
        descripcion = "Aquí irán sectores, cultivos, tareas y validaciones",
        onCerrarSesion = onCerrarSesion
    )
}

@Composable
fun HomeEstudianteScreen(nombre: String, onCerrarSesion: () -> Unit) {
    PanelBase(
        titulo = "Panel del estudiante",
        saludo = "Hola, $nombre",
        descripcion = "Aquí irán mis tareas y reportes de problemas",
        onCerrarSesion = onCerrarSesion
    )
}