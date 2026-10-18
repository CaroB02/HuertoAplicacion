package com.example.huertoaplicaciones.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

// Ventana de confirmación antes de borrar algo (sector, cultivo o tarea).
// Es un componente reutilizable: cada pantalla le pasa el título, el mensaje y qué hacer
// al confirmar o cancelar. Así evito borrar por accidente con un solo toque.
@Composable
fun ConfirmarEliminar(
    titulo: String,
    mensaje: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        // Se llama si el usuario toca fuera del cuadro o presiona Atrás: lo trato como "cancelar".
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                // El texto va en color de error (rojo) porque es una acción destructiva.
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
