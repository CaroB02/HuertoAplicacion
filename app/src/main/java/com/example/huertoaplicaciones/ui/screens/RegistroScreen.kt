package com.example.huertoaplicaciones.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.ui.components.CampoTexto
import com.example.huertoaplicaciones.ui.components.IconoCircular
import com.example.huertoaplicaciones.ui.components.SeccionTitulo
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.viewmodel.RegistroViewModel

// Pantalla para CREAR UNA CUENTA (estudiante o docente), a la que se llega desde el login.
// Igual que las demás: solo dibuja el formulario; las reglas de validación están en el RegistroViewModel.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel,
    onRegistrado: (String) -> Unit,
    onVolver: () -> Unit
) {
    val e by viewModel.estado.collectAsState()
    val esDocente = e.perfil == Perfil.ADMIN

    // Cuando la cuenta se creó bien, aviso con el correo para que el login ya lo traiga escrito.
    LaunchedEffect(e.registrado) {
        if (e.registrado) onRegistrado(e.correo.trim().lowercase())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                title = { Text("Crear cuenta") },
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
            // Mensaje de bienvenida con ícono.
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    IconoCircular(Icons.Default.PersonAdd, tamano = 48.dp)
                    Text(
                        "Crea tu cuenta para entrar al huerto. Usa solo datos ficticios.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // ---- ¿Estudiante o docente? ----
            SeccionTitulo(Icons.Default.Badge, "¿Quién eres?")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !esDocente,
                    onClick = { viewModel.cambiarPerfil(Perfil.ESTUDIANTE) },
                    label = { Text("Estudiante") },
                    leadingIcon = { Icon(Icons.Default.Face, contentDescription = null) }
                )
                FilterChip(
                    selected = esDocente,
                    onClick = { viewModel.cambiarPerfil(Perfil.ADMIN) },
                    label = { Text("Docente") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) }
                )
            }

            // ---- Datos de la cuenta ----
            CampoTexto(
                valor = e.nombre, etiqueta = "Nombre y apellido", icono = Icons.Default.Person,
                hayError = e.errorNombre, mensajeError = "Escribe tu nombre y al menos un apellido",
                onCambio = { viewModel.cambiarNombre(it) }
            )
            CampoTexto(
                valor = e.correo, etiqueta = "Correo electrónico", icono = Icons.Default.Email,
                hayError = e.errorCorreo || e.correoRepetido,
                // El mensaje cambia si el problema es que el correo ya existe.
                mensajeError = if (e.correoRepetido) "Ya existe una cuenta con ese correo"
                else "Usa un correo válido que termine en .cl",
                onCambio = { viewModel.cambiarCorreo(it) },
                tipoTeclado = KeyboardType.Email
            )
            CampoTexto(
                valor = e.clave, etiqueta = "Contraseña", icono = Icons.Default.Lock,
                hayError = e.errorClave, mensajeError = "Mínimo 8 caracteres",
                onCambio = { viewModel.cambiarClave(it) },
                tipoTeclado = KeyboardType.Password, esClave = true
            )
            CampoTexto(
                valor = e.repetirClave, etiqueta = "Repite la contraseña", icono = Icons.Default.Lock,
                hayError = e.errorRepetir, mensajeError = "Las contraseñas no coinciden",
                onCambio = { viewModel.cambiarRepetirClave(it) },
                tipoTeclado = KeyboardType.Password, esClave = true
            )
            CampoTexto(
                valor = e.grupo,
                etiqueta = if (esDocente) "Taller o grupo (opcional)" else "Tu grupo (por ejemplo Grupo A)",
                icono = Icons.Default.Groups,
                hayError = e.errorGrupo, mensajeError = "Escribe tu grupo",
                onCambio = { viewModel.cambiarGrupo(it) }
            )

            // ANIMACIÓN: el campo del código solo aparece cuando se elige "Docente".
            // Es la regla de seguridad: sin el código no se puede crear una cuenta de administrador.
            AnimatedVisibility(
                visible = esDocente,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CampoTexto(
                        valor = e.codigoDocente, etiqueta = "Código de autorización del docente",
                        icono = Icons.Default.VpnKey,
                        hayError = e.errorCodigo, mensajeError = "El código no es correcto",
                        onCambio = { viewModel.cambiarCodigo(it) },
                        esClave = true
                    )
                    Text(
                        "Solo docentes autorizados. Pide el código al encargado del taller.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.registrar() },
                enabled = !e.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (e.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Crear cuenta")
                }
            }
        }
    }
}
