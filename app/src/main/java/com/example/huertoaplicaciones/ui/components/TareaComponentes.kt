package com.example.huertoaplicaciones.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.data.entity.Tarea
import com.example.huertoaplicaciones.ui.theme.ColorExito
import com.example.huertoaplicaciones.ui.theme.ColorTextoSobreContenedor
import com.example.huertoaplicaciones.ui.theme.colorEstadoTarea
import com.example.huertoaplicaciones.util.estadoMostrado
import com.example.huertoaplicaciones.util.formatearFecha

// La tarjeta de una tarea en las listas. La usan tanto el docente como el estudiante.
// "detalle" es el texto de la línea del cultivo y cambia según quién la mira
// (el docente ve "Asignada a ...", el estudiante ve el cultivo).
// Diseño: un círculo con el ícono del TIPO de tarea a la izquierda (gota para riego, hoja para siembra...),
// los datos al centro con su ícono, y el estado como un "chip" abajo.
@Composable
fun TareaCard(tarea: Tarea, detalle: String, onClick: () -> Unit) {
    // Uso estadoMostrado y no tarea.estado, para que una tarea vencida se vea como "Atrasada".
    val estado = estadoMostrado(tarea)
    // ANIMACIÓN: cuando el estado cambia, el color de la tarjeta pasa suavemente de uno a otro
    // en 500 milisegundos (en vez de cambiar de golpe). "by" me da el color actual de la animación.
    val color by animateColorAsState(
        targetValue = colorEstadoTarea(estado),
        animationSpec = tween(durationMillis = 500),
        label = "colorEstadoTarea"
    )
    // Blanco semitransparente: se ve bien sobre cualquier color pastel de estado.
    val blanco = Color.White.copy(alpha = 0.65f)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = color, contentColor = ColorTextoSobreContenedor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconoCircular(iconoTipoTarea(tarea.tipo), fondo = blanco, tinte = ColorExito)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(tarea.tipo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(tarea.descripcion, style = MaterialTheme.typography.bodyMedium)
                FilaInfo(Icons.Default.Eco, detalle)
                FilaInfo(Icons.Default.CalendarMonth, "Para el ${formatearFecha(tarea.fechaProgramada)}")
                Spacer(Modifier.height(2.dp))
                EstadoChip(estado, iconoEstadoTarea(estado), blanco)
            }
        }
    }
}

// Mensaje de error de un campo que no es de texto (selectores, fotos): ícono + texto en rojo
// Los campos de texto ya traen su propio ícono de error (CampoTexto); este es para los demás.
@Composable
fun TextoError(mensaje: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            mensaje,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
