package com.example.huertoaplicaciones.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

// COMPONENTE REUTILIZABLE: un campo de texto con todo lo que piden los formularios validados:
// ícono a la izquierda, ícono de advertencia a la derecha cuando hay error, borde rojo y el
// mensaje de error debajo. Lo escribí UNA vez y lo uso en todos los formularios, así se ven iguales.
// @Composable = función que dibuja interfaz. Se vuelve a ejecutar sola cuando cambian sus parámetros.
@Composable
fun CampoTexto(
    valor: String,                          // el texto actual (lo guarda el ViewModel, no este componente)
    etiqueta: String,                       // el título que flota arriba del campo
    icono: ImageVector,                     // ícono decorativo de la izquierda
    hayError: Boolean,                      // true = pintar en rojo y mostrar el mensaje
    mensajeError: String,
    onCambio: (String) -> Unit,             // función que se llama cada vez que el usuario escribe
    modifier: Modifier = Modifier,
    tipoTeclado: KeyboardType = KeyboardType.Text,   // por ejemplo KeyboardType.Number para solo números
    esClave: Boolean = false                         // true = el texto se muestra como puntitos (contraseñas)
) {
    // OutlinedTextField es el campo de Material 3 con borde.
    OutlinedTextField(
        value = valor,
        // Patrón "state hoisting": el campo no guarda el texto; avisa con onCambio y el ViewModel
        // actualiza el estado, que vuelve a entrar por "valor". Así la fuente de verdad es una sola.
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        // El ícono de error solo existe cuando hayError es true.
        trailingIcon = {
            if (hayError) Icon(
                Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error
            )
        },
        isError = hayError,
        // El mensaje aparece debajo del campo solo si hay error.
        supportingText = { if (hayError) Text(mensajeError) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        visualTransformation = if (esClave) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier.fillMaxWidth()
    )
}
