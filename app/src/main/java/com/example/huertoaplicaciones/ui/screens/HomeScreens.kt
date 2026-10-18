package com.example.huertoaplicaciones.ui.screens

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.ui.components.AvatarIniciales
import com.example.huertoaplicaciones.ui.components.EstadoChip
import com.example.huertoaplicaciones.ui.components.FilaInfo
import com.example.huertoaplicaciones.ui.theme.ColorExitoContenedor
import com.example.huertoaplicaciones.util.Perfil
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.ui.components.IconoCircular
import com.example.huertoaplicaciones.ui.components.SeccionTitulo
import com.example.huertoaplicaciones.ui.theme.ColorAdvertencia
import com.example.huertoaplicaciones.ui.theme.ColorError
import com.example.huertoaplicaciones.ui.theme.ColorExito
import com.example.huertoaplicaciones.ui.theme.ColorInfo
import com.example.huertoaplicaciones.ui.theme.Tierra40
import com.example.huertoaplicaciones.util.Aviso
import com.example.huertoaplicaciones.util.ResumenTareas
import com.example.huertoaplicaciones.util.TipoAviso
import com.example.huertoaplicaciones.viewmodel.PanelUiState

// PanelBase es la estructura COMPARTIDA de los dos paneles (docente y estudiante). Así los dos se ven
// parecidos y no repito código. Siempre tiene este orden:
//   1) Banner de bienvenida  2) Resumen de tareas  3) Accesos (botones grandes con ícono)  4) Avisos
// Lo único que cambia entre roles son los accesos, que llegan por el parámetro "accesos".
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PanelBase(
    titulo: String,
    usuario: Usuario,
    subtitulo: String,
    panel: PanelUiState,
    onCerrarSesion: () -> Unit,
    accesos: @Composable ColumnScope.() -> Unit
) {
    // Cada panel es una pantalla distinta según el rol: el dashboard del docente y el del estudiante
    // muestran información diferente (la calcula el PanelViewModel).
    // Estado LOCAL: si la ventanita del perfil está abierta o cerrada.
    var verPerfil by remember { mutableStateOf(false) }
    if (verPerfil) {
        PerfilDialogo(
            usuario = usuario,
            onCerrar = { verPerfil = false },
            onCerrarSesion = {
                verPerfil = false
                onCerrarSesion()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Eco, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(titulo)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Botón del perfil pequeño: abre una ventanita con los datos de la persona.
                    IconButton(onClick = { verPerfil = true }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Mi perfil")
                    }
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Banner(usuario, subtitulo)
            ResumenCard(panel.resumen)
            SeccionTitulo(Icons.Default.Apps, "¿Qué quieres hacer?")
            accesos()
            AvisosCard(panel.avisos)
            Spacer(Modifier.height(8.dp))
        }
    }
}

// "Perfil pequeño": una ventanita con el avatar, el nombre, el rol, el correo y el grupo de la persona.
// Los datos salen del Usuario que inició sesión; no se guarda ninguna foto personal.
@Composable
private fun PerfilDialogo(usuario: Usuario, onCerrar: () -> Unit, onCerrarSesion: () -> Unit) {
    val esDocente = usuario.perfil == Perfil.ADMIN
    AlertDialog(
        onDismissRequest = onCerrar,
        icon = { AvatarIniciales(usuario.nombre, tamano = 72.dp) },
        title = { Text(usuario.nombre, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EstadoChip(
                    texto = if (esDocente) "Docente" else "Estudiante",
                    icono = if (esDocente) Icons.Default.School else Icons.Default.Face,
                    fondo = ColorExitoContenedor
                )
                FilaInfo(Icons.Default.Email, usuario.correo)
                // El grupo solo se muestra si la persona tiene uno asignado.
                if (usuario.grupo.isNotBlank()) FilaInfo(Icons.Default.Groups, usuario.grupo)
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } },
        dismissButton = { TextButton(onClick = onCerrarSesion) { Text("Cerrar sesión") } }
    )
}

// Banner de bienvenida: un fondo con degradado verde, el avatar del perfil y el saludo con el nombre.
@Composable
private fun Banner(usuario: Usuario, subtitulo: String) {
    // Brush.linearGradient pinta el fondo pasando de un color a otro.
    val degradado = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(degradado)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar con las iniciales de la persona (en vez de un ícono genérico).
        AvatarIniciales(
            nombre = usuario.nombre,
            tamano = 60.dp,
            fondo = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
            texto = MaterialTheme.colorScheme.onPrimary
        )
        Column {
            Text(
                "Hola,",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                usuario.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// Tarjeta con el avance: una barra de progreso y los contadores de cada estado.
@Composable
private fun ResumenCard(resumen: ResumenTareas) {
    // ANIMACIÓN: cuando cambia el avance, la barra se llena poco a poco (800 ms) en vez de saltar.
    val avance by animateFloatAsState(
        targetValue = resumen.avance,
        animationSpec = tween(durationMillis = 800),
        label = "avanceTareas"
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SeccionTitulo(Icons.Default.Assessment, "Resumen de tareas")
            LinearProgressIndicator(
                progress = { avance },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
            )
            Text(
                "${resumen.validadas} de ${resumen.total} tareas validadas (${(resumen.avance * 100).toInt()}%)",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Contador("Pendientes", resumen.pendientes + resumen.enDesarrollo, ColorInfo, Icons.Default.Schedule)
                Contador("Por validar", resumen.porValidar, ColorAdvertencia, Icons.Default.HourglassTop)
                Contador("Atrasadas", resumen.atrasadas, ColorError, Icons.Default.Warning)
                Contador("Rechazadas", resumen.rechazadas, ColorError, Icons.Default.Cancel)
            }
        }
    }
}

// Un número grande con su ícono y su etiqueta debajo (por ejemplo: ⚠ 1 Atrasadas).
@Composable
private fun Contador(etiqueta: String, valor: Int, color: Color, icono: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconoCircular(icono = icono, fondo = color.copy(alpha = 0.15f), tinte = color, tamano = 40.dp)
        Text(
            valor.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(etiqueta, style = MaterialTheme.typography.labelSmall)
    }
}

// Cada tipo de aviso tiene su ícono y su color, para que se reconozca de un vistazo.
private fun iconoAviso(tipo: TipoAviso): ImageVector = when (tipo) {
    TipoAviso.ATRASADA -> Icons.Default.Error
    TipoAviso.POR_VALIDAR -> Icons.Default.Info
    TipoAviso.RECHAZADA -> Icons.Default.Warning
    TipoAviso.PROBLEMA -> Icons.Default.Warning
    TipoAviso.COSECHA -> Icons.Default.Grass
    TipoAviso.RIEGO -> Icons.Default.WaterDrop
    TipoAviso.PROXIMA -> Icons.Default.CalendarMonth
    TipoAviso.MANTENIMIENTO -> Icons.Default.Build
    TipoAviso.SENSOR -> Icons.Default.Sensors
}

private fun colorAviso(tipo: TipoAviso): Color = when (tipo) {
    TipoAviso.ATRASADA -> ColorError
    TipoAviso.POR_VALIDAR -> ColorInfo
    TipoAviso.RECHAZADA -> ColorAdvertencia
    TipoAviso.PROBLEMA -> ColorAdvertencia
    TipoAviso.COSECHA -> ColorExito
    TipoAviso.RIEGO -> ColorInfo
    TipoAviso.PROXIMA -> ColorInfo
    TipoAviso.MANTENIMIENTO -> ColorInfo
    TipoAviso.SENSOR -> ColorAdvertencia
}

// Lista de avisos y recordatorios. Si no hay ninguno, lo digo con un mensaje positivo (estado vacío).
// Cada aviso va en una "fila" con el color de su tipo, para que no se vea todo mezclado.
@Composable
private fun AvisosCard(avisos: List<Aviso>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SeccionTitulo(Icons.Default.Notifications, "Avisos y recordatorios")
            if (avisos.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ColorExito)
                    Spacer(Modifier.width(8.dp))
                    Text("Todo al día. No hay avisos pendientes.")
                }
            } else {
                avisos.forEach { aviso ->
                    val color = colorAviso(aviso.tipo)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(color.copy(alpha = 0.10f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconoCircular(
                            icono = iconoAviso(aviso.tipo),
                            fondo = color.copy(alpha = 0.2f),
                            tinte = color,
                            tamano = 36.dp
                        )
                        Text(aviso.texto, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

// Un ACCESO es un botón grande con ícono que lleva a otra pantalla.
//  - horizontal = true: ícono a la izquierda y flecha a la derecha (lista, como en el panel del estudiante).
//  - horizontal = false: ícono arriba y texto abajo (cuadrícula, como en el panel del docente).
// "insignia" es un número rojo en la esquina (por ejemplo, tareas por validar); si es 0 no se muestra.
@Composable
private fun Acceso(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontal: Boolean = false,
    insignia: Int = 0
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        if (horizontal) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconoCircular(icono = icono, fondo = color, tinte = Color.White, tamano = 52.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(descripcion, style = MaterialTheme.typography.bodySmall)
                }
                Insignia(insignia)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    IconoCircular(icono = icono, fondo = color, tinte = Color.White, tamano = 48.dp)
                    Insignia(insignia)
                }
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(descripcion, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// El circulito rojo con un número. Si el número es 0 no dibuja nada.
@Composable
private fun Insignia(cantidad: Int) {
    if (cantidad > 0) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(ColorError),
            contentAlignment = Alignment.Center
        ) {
            Text(
                cantidad.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}

// Panel del DOCENTE: ordenado en una cuadrícula de 2 columnas con acceso a sectores, cultivos, tareas,
// problemas e historial. La insignia roja de "Tareas" avisa cuántas esperan su validación.
@Composable
fun HomeAdminScreen(
    usuario: Usuario,
    panel: PanelUiState,
    onIrASectores: () -> Unit,
    onIrACultivos: () -> Unit,
    onIrATareas: () -> Unit,
    onIrAReportes: () -> Unit,
    onIrAHistorial: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    PanelBase(
        titulo = "Panel del docente",
        usuario = usuario,
        subtitulo = "Gestiona el huerto y revisa el trabajo de tus estudiantes",
        panel = panel,
        onCerrarSesion = onCerrarSesion
    ) {
        // IntrinsicSize.Min hace que las dos tarjetas de una fila midan lo mismo de alto.
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Acceso(
                "Sectores", "Zonas del huerto", Icons.Default.Place, ColorInfo,
                onIrASectores, Modifier.weight(1f).fillMaxHeight()
            )
            Acceso(
                "Cultivos", "Plantas y fechas", Icons.Default.Eco, ColorExito,
                onIrACultivos, Modifier.weight(1f).fillMaxHeight()
            )
        }
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Acceso(
                "Tareas", "Crear, asignar y revisar", Icons.Default.Assignment, ColorAdvertencia,
                onIrATareas, Modifier.weight(1f).fillMaxHeight(),
                insignia = panel.resumen.porValidar
            )
            Acceso(
                "Problemas", "Reportes del huerto", Icons.Default.Report, ColorError,
                onIrAReportes, Modifier.weight(1f).fillMaxHeight()
            )
        }
        Acceso(
            "Historial de actividades", "Todo lo que ha pasado en el huerto", Icons.Default.History, Tierra40,
            onIrAHistorial, Modifier.fillMaxWidth(), horizontal = true
        )
    }
}

// Panel del ESTUDIANTE: más simple y amigable. Tres accesos grandes en lista: sus tareas,
// reportar un problema y su historial. No tiene botones de administración: así se restringe
// lo que cada perfil puede hacer. La insignia de "Mis tareas" cuenta las que debe trabajar.
@Composable
fun HomeEstudianteScreen(
    usuario: Usuario,
    panel: PanelUiState,
    onIrAMisTareas: () -> Unit,
    onIrAReportes: () -> Unit,
    onIrAHistorial: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val r = panel.resumen
    PanelBase(
        titulo = "Panel del estudiante",
        usuario = usuario,
        subtitulo = "¡Vamos a cuidar el huerto juntos!",
        panel = panel,
        onCerrarSesion = onCerrarSesion
    ) {
        Acceso(
            "Mis tareas", "Revisa lo que te toca y entrega tu trabajo con fotos",
            Icons.Default.Assignment, ColorExito,
            onIrAMisTareas, Modifier.fillMaxWidth(), horizontal = true,
            insignia = r.pendientes + r.enDesarrollo + r.rechazadas + r.atrasadas
        )
        Acceso(
            "Reportar un problema", "Avisa si ves una plaga o una planta dañada",
            Icons.Default.Report, ColorError,
            onIrAReportes, Modifier.fillMaxWidth(), horizontal = true
        )
        Acceso(
            "Mi historial", "Mira todo lo que has hecho",
            Icons.Default.History, Tierra40,
            onIrAHistorial, Modifier.fillMaxWidth(), horizontal = true
        )
    }
}
