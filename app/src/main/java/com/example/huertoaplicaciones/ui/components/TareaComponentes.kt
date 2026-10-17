package com.example.huertoaplicaciones.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.data.entity.Tarea
import com.example.huertoaplicaciones.util.EstadoTarea
import com.example.huertoaplicaciones.util.estadoMostrado
import com.example.huertoaplicaciones.util.formatearFecha

fun colorEstadoTarea(estado: String): Color = when (estado) {
    EstadoTarea.PENDIENTE -> Color(0xFFFFF9C4)
    EstadoTarea.EN_DESARROLLO -> Color(0xFFBBDEFB)
    EstadoTarea.PENDIENTE_VALIDACION -> Color(0xFFFFE0B2)
    EstadoTarea.VALIDADA -> Color(0xFFC8E6C9)
    EstadoTarea.RECHAZADA -> Color(0xFFFFCDD2)
    EstadoTarea.ATRASADA -> Color(0xFFFFAB91)
    else -> Color(0xFFE0E0E0)
}

@Composable
fun TareaCard(tarea: Tarea, detalle: String, onClick: () -> Unit) {
    val estado = estadoMostrado(tarea)
    val color by animateColorAsState(
        targetValue = colorEstadoTarea(estado),
        animationSpec = tween(durationMillis = 500),
        label = "colorEstadoTarea"
    )
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color, contentColor = Color.Black),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(tarea.tipo, style = MaterialTheme.typography.titleMedium)
            Text(tarea.descripcion)
            Text(detalle)
            Text("Para el ${formatearFecha(tarea.fechaProgramada)}")
            Text("Estado: $estado", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun TextoError(mensaje: String) {
    Text(
        mensaje,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall
    )
}
