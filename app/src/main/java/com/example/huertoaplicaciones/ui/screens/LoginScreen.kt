package com.example.huertoaplicaciones.ui.screens

import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Eco
import androidx.compose.ui.text.font.FontWeight
import com.example.huertoaplicaciones.ui.components.IconoCircular
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.ui.components.TextoError
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.viewmodel.AuthViewModel

// PANTALLA = un Composable: una función que DIBUJA la interfaz a partir del estado.
// No guarda datos ni decide reglas: lee el estado del AuthViewModel y le avisa cuando el usuario hace algo.
// Esta es la primera pantalla de la app (inicio de sesión).
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onCrearCuenta: () -> Unit,
    onLoginExitoso: (Usuario) -> Unit
) {
    // collectAsState: "escucha" el StateFlow del ViewModel. Cuando el estado cambia (por ejemplo,
    // aparece un error), Compose vuelve a dibujar solo lo que depende de ese dato.
    val uiState by authViewModel.uiState.collectAsState()

    // Si el usuario ya se autenticó correctamente, navega a la pantalla correspondiente
    // LaunchedEffect: ejecuta código cuando cambia su clave. Cuando el login resulta exitoso,
    // usuarioLogueado deja de ser null y aquí avanzo al panel que corresponde al perfil.
    LaunchedEffect(uiState.usuarioLogueado) {
        uiState.usuarioLogueado?.let { usuario ->
            onLoginExitoso(usuario)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo: un círculo verde con una hoja (en vez del emoji) y el nombre de la app debajo.
            IconoCircular(
                icono = Icons.Default.Eco,
                fondo = MaterialTheme.colorScheme.primaryContainer,
                tinte = MaterialTheme.colorScheme.primary,
                tamano = 96.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Huerta Escolar",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Sistema de Gestión y Seguimiento",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Campo Correo Electrónico
            OutlinedTextField(
                value = uiState.emailInput,
                onValueChange = { authViewModel.onEmailChanged(it) },
                label = { Text("Correo Electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                trailingIcon = {
                    if (uiState.emailError != null) Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                isError = uiState.emailError != null,
                supportingText = {
                    uiState.emailError?.let { Text(it) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = uiState.passwordInput,
                onValueChange = { authViewModel.onPasswordChanged(it) },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                trailingIcon = {
                    if (uiState.passwordError != null) Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                isError = uiState.passwordError != null,
                supportingText = {
                    uiState.passwordError?.let { Text(it) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            // Alerta de error en caso de fallo
            // ANIMACIÓN: el mensaje de error aparece y desaparece con una transición, no de golpe.
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                uiState.errorMessage?.let { error ->
                    Box(modifier = Modifier.padding(vertical = 8.dp)) { TextoError(error) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Iniciar Sesión
            Button(
                onClick = { authViewModel.iniciarSesion() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                // Mientras se consulta la base de datos muestro una ruedita de carga dentro del botón.
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Iniciar Sesión")
                }
            }

            // Para quien todavía no tiene cuenta (estudiante o docente): lleva a la pantalla de registro.
            TextButton(onClick = onCrearCuenta) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("¿No tienes cuenta? Crear cuenta")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Acceso Rápido para Pruebas (Docente / Estudiante)
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cuentas de prueba rápida:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { authViewModel.cargarCuentaPrueba("carolina.munoz@huerta.cl") }
                ) {
                    Text("Carolina (docente)")
                }

                OutlinedButton(
                    onClick = { authViewModel.cargarCuentaPrueba("matias.fuentes@huerta.cl") }
                ) {
                    Text("Matías")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { authViewModel.cargarCuentaPrueba("valentina.perez@huerta.cl") }
                ) {
                    Text("Valentina")
                }

                OutlinedButton(
                    onClick = { authViewModel.cargarCuentaPrueba("benjamin.castro@huerta.cl") }
                ) {
                    Text("Benjamín")
                }
            }
        }
    }
}