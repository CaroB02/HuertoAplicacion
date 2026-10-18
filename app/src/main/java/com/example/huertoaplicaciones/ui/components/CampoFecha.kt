package com.example.huertoaplicaciones.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.huertoaplicaciones.util.fechaUtcATexto
import com.example.huertoaplicaciones.util.hoyComoFechaUtc
import com.example.huertoaplicaciones.util.textoAFechaUtc

// CAMPO DE FECHA fácil de usar: en vez de escribir "20/10/2026" a mano (y equivocarse con las barras),
// la persona toca el campo y elige el día en un CALENDARIO. Al aceptar, el campo muestra "dd/mm/aaaa".
// El resultado vuelve como texto con onCambio, así los ViewModels y sus validaciones siguen igual.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    valor: String,                          // la fecha actual en texto dd/mm/aaaa ("" si no hay)
    etiqueta: String,
    hayError: Boolean,
    mensajeError: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    opcional: Boolean = false,              // true = aparece una "X" para borrar la fecha elegida
    desdeHoy: Boolean = false               // true = los días anteriores a hoy aparecen desactivados
) {
    // Estado LOCAL: si el calendario está abierto o cerrado.
    var mostrarCalendario by remember { mutableStateOf(false) }

    // Box apila dos cosas: el campo (solo de lectura) y, encima, una capa transparente que recibe el toque.
    // Hago esto porque un campo de texto se "come" los toques y no deja que el Box los reciba.
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},             // no se escribe a mano: solo se elige en el calendario
            readOnly = true,
            label = { Text(etiqueta) },
            placeholder = { Text("Toca para elegir la fecha") },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            trailingIcon = {
                when {
                    hayError -> Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    // En los campos opcionales se puede quitar la fecha con la "X".
                    opcional && valor.isNotEmpty() -> IconButton(onClick = { onCambio("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Quitar fecha")
                    }
                }
            },
            isError = hayError,
            supportingText = { if (hayError) Text(mensajeError) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente: al tocarla se abre el calendario. Deja libre la "X" de borrar (a la derecha).
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { mostrarCalendario = true }
        )
    }

    if (mostrarCalendario) {
        // El calendario abre en la fecha ya elegida (o en hoy si todavía no hay ninguna).
        // Regla de fechas seleccionables: si desdeHoy es true, los días pasados no se pueden elegir.
        val seleccionables = remember(desdeHoy) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    !desdeHoy || utcTimeMillis >= hoyComoFechaUtc()
            }
        }
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = textoAFechaUtc(valor),
            selectableDates = seleccionables
        )

        // Los textos del calendario (meses, días, títulos) salen en español porque MainActivity fuerza
        // el idioma español en toda la app.
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Si eligió un día, lo paso a texto dd/mm/aaaa y se lo entrego al formulario.
                        estado.selectedDateMillis?.let { onCambio(fechaUtcATexto(it)) }
                        mostrarCalendario = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estado)
        }
    }
}
