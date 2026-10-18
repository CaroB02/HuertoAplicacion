package com.example.huertoaplicaciones.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huertoaplicaciones.ui.theme.ColorTextoSobreContenedor
import com.example.huertoaplicaciones.util.EstadoCultivo
import com.example.huertoaplicaciones.util.EstadoProblema
import com.example.huertoaplicaciones.util.EstadoSector
import com.example.huertoaplicaciones.util.EstadoTarea
import com.example.huertoaplicaciones.util.TipoProblema
import com.example.huertoaplicaciones.util.TipoTarea

// Este archivo junta las piezas VISUALES que reutilizo en muchas pantallas:
// íconos según el tipo o el estado, chips de estado, títulos de sección y filas de información.
// Así todas las pantallas se ven iguales (consistencia) y no repito código.

// ---------- Íconos según el tipo de tarea ----------
// "when" revisa el texto y devuelve el ícono que corresponde; "else" cubre cualquier otro caso.
fun iconoTipoTarea(tipo: String): ImageVector = when (tipo) {
    TipoTarea.RIEGO -> Icons.Default.WaterDrop
    TipoTarea.SIEMBRA -> Icons.Default.Grass
    TipoTarea.TRASPLANTE -> Icons.Default.Yard
    TipoTarea.PREPARACION -> Icons.Default.Construction
    TipoTarea.LIMPIEZA -> Icons.Default.CleaningServices
    TipoTarea.MALEZAS -> Icons.Default.ContentCut
    TipoTarea.OBSERVACION -> Icons.Default.Visibility
    TipoTarea.MANTENIMIENTO -> Icons.Default.Build
    TipoTarea.REVISION_SUELO -> Icons.Default.Terrain
    TipoTarea.REGISTRO_CRECIMIENTO -> Icons.Default.TrendingUp
    TipoTarea.COSECHA -> Icons.Default.Agriculture
    else -> Icons.Default.Assignment
}

// ---------- Íconos según el estado ----------
fun iconoEstadoTarea(estado: String): ImageVector = when (estado) {
    EstadoTarea.PENDIENTE -> Icons.Default.Schedule
    EstadoTarea.EN_DESARROLLO -> Icons.Default.PlayCircle
    EstadoTarea.PENDIENTE_VALIDACION -> Icons.Default.HourglassTop
    EstadoTarea.VALIDADA -> Icons.Default.CheckCircle
    EstadoTarea.RECHAZADA -> Icons.Default.Cancel
    EstadoTarea.ATRASADA -> Icons.Default.Warning
    else -> Icons.Default.Info
}

fun iconoEstadoSector(estado: String): ImageVector = when (estado) {
    EstadoSector.DISPONIBLE -> Icons.Default.CheckCircle
    EstadoSector.EN_USO -> Icons.Default.Yard
    EstadoSector.EN_MANTENIMIENTO -> Icons.Default.Build
    else -> Icons.Default.Block
}

fun iconoEstadoCultivo(estado: String): ImageVector = when (estado) {
    EstadoCultivo.PLANIFICADO -> Icons.Default.Schedule
    EstadoCultivo.SEMBRADO -> Icons.Default.Grass
    EstadoCultivo.EN_CRECIMIENTO -> Icons.Default.Eco
    EstadoCultivo.LISTO_COSECHA -> Icons.Default.Agriculture
    EstadoCultivo.COSECHADO -> Icons.Default.CheckCircle
    EstadoCultivo.CON_PROBLEMAS -> Icons.Default.Warning
    else -> Icons.Default.Flag
}

fun iconoEstadoProblema(estado: String): ImageVector = when (estado) {
    EstadoProblema.INFORMADO -> Icons.Default.Report
    EstadoProblema.EN_REVISION -> Icons.Default.Visibility
    EstadoProblema.ATENDIDO -> Icons.Default.TaskAlt
    else -> Icons.Default.CheckCircle
}

fun iconoTipoProblema(tipo: String): ImageVector = when (tipo) {
    TipoProblema.FALTA_AGUA -> Icons.Default.WaterDrop
    TipoProblema.PLANTA_DANADA -> Icons.Default.LocalFlorist
    TipoProblema.PLAGA -> Icons.Default.BugReport
    TipoProblema.DETERIORO -> Icons.Default.Construction
    TipoProblema.MANTENIMIENTO -> Icons.Default.Build
    else -> Icons.Default.Help
}

// Un círculo de color con un ícono adentro. Lo uso como "logo" de tarjetas y accesos.
@Composable
fun IconoCircular(
    icono: ImageVector,
    modifier: Modifier = Modifier,
    fondo: Color = MaterialTheme.colorScheme.primaryContainer,
    tinte: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    tamano: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        // El ícono ocupa un poco más de la mitad del círculo.
        Icon(icono, contentDescription = null, tint = tinte, modifier = Modifier.size(tamano * 0.55f))
    }
}

// Una "pastilla" pequeña con ícono y texto: la uso para mostrar el ESTADO de tareas, sectores,
// cultivos y reportes de forma uniforme. El color de fondo viene del tema (colores semánticos).
@Composable
fun EstadoChip(texto: String, icono: ImageVector, fondo: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(50),
        color = fondo,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icono, contentDescription = null,
                tint = ColorTextoSobreContenedor, modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(texto, style = MaterialTheme.typography.labelMedium, color = ColorTextoSobreContenedor)
        }
    }
}

// Título de una sección con su ícono (por ejemplo: "Descripción", "Evidencias").
@Composable
fun SeccionTitulo(icono: ImageVector, texto: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icono, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            texto,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// Avatar redondo con las INICIALES de la persona (por ejemplo, "Carolina Muñoz Soto" -> "CM").
// Es el "perfil pequeño": reemplaza a una foto, que no queremos guardar (el caso exige no usar fotos personales).
@Composable
fun AvatarIniciales(
    nombre: String,
    modifier: Modifier = Modifier,
    tamano: Dp = 44.dp,
    fondo: Color = MaterialTheme.colorScheme.primaryContainer,
    texto: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    // Parto el nombre por espacios, tomo las dos primeras palabras y de cada una su primera letra.
    val iniciales = nombre.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Text(
            iniciales,
            color = texto,
            fontWeight = FontWeight.Bold,
            // El tamaño de la letra es proporcional al del círculo.
            fontSize = (tamano.value * 0.4f).sp
        )
    }
}

// Una línea de información: un ícono pequeño y un texto (por ejemplo, 📅 "Para el 18/10/2026").
@Composable
fun FilaInfo(icono: ImageVector, texto: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icono, contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}
