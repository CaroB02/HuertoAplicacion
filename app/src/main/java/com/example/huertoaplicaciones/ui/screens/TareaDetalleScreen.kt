package com.example.huertoaplicaciones.ui.screens

import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.huertoaplicaciones.ui.components.EstadoChip
import com.example.huertoaplicaciones.ui.components.FilaInfo
import com.example.huertoaplicaciones.ui.components.IconoCircular
import com.example.huertoaplicaciones.ui.components.SeccionTitulo
import com.example.huertoaplicaciones.ui.components.iconoEstadoTarea
import com.example.huertoaplicaciones.ui.components.iconoTipoTarea
import com.example.huertoaplicaciones.ui.theme.ColorError
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.ui.components.CampoTexto
import com.example.huertoaplicaciones.ui.components.ConfirmarEliminar
import com.example.huertoaplicaciones.ui.components.GaleriaFotos
import com.example.huertoaplicaciones.ui.components.SelectorFoto
import com.example.huertoaplicaciones.ui.components.TextoError
import com.example.huertoaplicaciones.ui.theme.ColorExito
import com.example.huertoaplicaciones.ui.theme.ColorTextoSobreContenedor
import com.example.huertoaplicaciones.ui.theme.colorEstadoTarea
import com.example.huertoaplicaciones.util.EstadoTarea
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.util.estadoMostrado
import com.example.huertoaplicaciones.util.formatearFecha
import com.example.huertoaplicaciones.util.formatearFechaHora
import com.example.huertoaplicaciones.viewmodel.TareaViewModel

// Le enseña a Compose a guardar la lista de fotos elegidas aunque se rote la pantalla
// (rememberSaveable necesita saber cómo guardar y restaurar el valor).
private val GuardadorFotos = listSaver<List<String>, String>(
    save = { it },
    restore = { it }
)

@OptIn(ExperimentalMaterial3Api::class)
// DETALLE DE LA TAREA: la pantalla más importante del flujo. La usan los DOS perfiles y muestra
// cosas distintas según quién la mira y en qué estado está la tarea:
//  - Estudiante dueño: Iniciar (si está Pendiente) o Entregar con comentario y foto.
//  - Docente: Validar o Rechazar cuando está Pendiente de validación; Editar o Eliminar si está Pendiente.
@Composable
fun TareaDetalleScreen(
    viewModel: TareaViewModel,
    usuario: Usuario,
    onEditar: (Long) -> Unit,
    onVolver: () -> Unit
) {
    // Todos los datos de esta pantalla vienen del TareaViewModel como StateFlow.
    val tarea by viewModel.seleccionada.collectAsState()
    val sectores by viewModel.sectores.collectAsState()
    val cultivos by viewModel.cultivos.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()
    val evidencias by viewModel.evidencias.collectAsState()
    val validaciones by viewModel.validaciones.collectAsState()
    val errorComentario by viewModel.errorComentario.collectAsState()
    val errorEntrega by viewModel.errorEntrega.collectAsState()
    val accionHecha by viewModel.accionHecha.collectAsState()
    var confirmarBorrado by remember { mutableStateOf(false) }

    // Cuando una acción termina bien (entregar, validar, rechazar, eliminar), vuelvo a la lista.
    LaunchedEffect(accionHecha) {
        if (accionHecha) onVolver()
    }

    // Solo el docente puede editar o eliminar, y únicamente mientras la tarea siga Pendiente.
    val puedeGestionar = usuario.perfil == Perfil.ADMIN && tarea?.estado == EstadoTarea.PENDIENTE

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                title = { Text("Detalle de la tarea") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (puedeGestionar) {
                        tarea?.let { t ->
                            IconButton(onClick = { onEditar(t.id) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar tarea")
                            }
                            IconButton(onClick = { confirmarBorrado = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar tarea")
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val t = tarea
        if (t == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Cargando tarea…") }
        } else {
            // Estado LOCAL de la pantalla: el comentario que se está escribiendo. Se reinicia al cambiar de tarea.
            var comentario by remember(t.id) { mutableStateOf("") }
            // Las fotos elegidas por el estudiante antes de entregar. Aún no están en la base de datos:
            // se guardan recién al presionar Entregar.
            var fotos by rememberSaveable(t.id, stateSaver = GuardadorFotos) {
                mutableStateOf<List<String>>(emptyList())
            }

            if (confirmarBorrado) {
                ConfirmarEliminar(
                    titulo = "Eliminar tarea",
                    mensaje = "¿Eliminar la tarea de ${t.tipo}? Esta acción no se puede deshacer.",
                    onConfirmar = {
                        confirmarBorrado = false
                        viewModel.eliminar(t, usuario)
                    },
                    onCancelar = { confirmarBorrado = false }
                )
            }

            val estadoActual = estadoMostrado(t)
            val sector = sectores.firstOrNull { it.id == t.sectorId }?.nombre ?: "-"
            val cultivo = cultivos.firstOrNull { it.id == t.cultivoId }?.nombre ?: "-"
            fun nombre(id: Long?) = usuarios.firstOrNull { it.id == id }?.nombre ?: "-"

            // PERMISOS: aquí se decide qué botones puede ver quien mira la pantalla. Son el control de acceso
            // de la interfaz (el ViewModel vuelve a verificarlo, por seguridad).
            val esDueno = usuario.perfil == Perfil.ESTUDIANTE && t.asignadoAId == usuario.id
            // Solo el estudiante dueño de la tarea puede iniciarla, y solo si está Pendiente.
            val puedeIniciar = esDueno && t.estado == EstadoTarea.PENDIENTE
            // Entrega desde "En desarrollo", o reenvío desde "Rechazada" después de corregir.
            val puedeEntregar = esDueno && (
                    t.estado == EstadoTarea.EN_DESARROLLO || t.estado == EstadoTarea.RECHAZADA
                    )
            // El docente revisa solo si la tarea espera validación y la tarea NO es suya (nadie aprueba lo propio).
            val puedeRevisar = usuario.perfil == Perfil.ADMIN &&
                    usuario.id != t.asignadoAId &&
                    t.estado == EstadoTarea.PENDIENTE_VALIDACION
            // Verdadero cuando ya hay comentario y al menos una foto: activa el aviso animado "Todo listo".
            val listoParaEnviar = comentario.isNotBlank() && fotos.isNotEmpty()

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tarjeta de encabezado: ícono grande del tipo de tarea, su estado como "chip" y los datos clave
                // (cada uno con su ícono) para leer la tarea de un vistazo.
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorEstadoTarea(estadoActual),
                        contentColor = ColorTextoSobreContenedor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            IconoCircular(
                                iconoTipoTarea(t.tipo),
                                fondo = Color.White.copy(alpha = 0.65f), tinte = ColorExito, tamano = 56.dp
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    t.tipo,
                                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
                                )
                                EstadoChip(
                                    estadoActual, iconoEstadoTarea(estadoActual),
                                    Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }
                        HorizontalDivider()
                        FilaInfo(Icons.Default.Place, "Sector: $sector")
                        FilaInfo(Icons.Default.Eco, "Cultivo: $cultivo")
                        FilaInfo(Icons.Default.Face, "Asignada a: ${nombre(t.asignadoAId)}")
                        FilaInfo(Icons.Default.School, "Creada por: ${nombre(t.creadoPorId)}")
                        FilaInfo(Icons.Default.CalendarMonth, "Fecha programada: ${formatearFecha(t.fechaProgramada)}")
                        t.fechaRealizacion?.let {
                            FilaInfo(Icons.Default.Upload, "Entregada el: ${formatearFechaHora(it)}")
                        }
                        t.fechaValidacion?.let { fecha ->
                            FilaInfo(
                                Icons.Default.VerifiedUser,
                                "Revisada por ${nombre(t.validadoPorId)} el ${formatearFechaHora(fecha)}"
                            )
                        }
                    }
                }

                // Cada bloque de texto tiene un título con ícono (SeccionTitulo) para ordenar la lectura.
                SeccionTitulo(Icons.Default.Description, "Descripción")
                Text(t.descripcion)
                SeccionTitulo(Icons.Default.Checklist, "Instrucciones")
                Text(t.instrucciones)

                if (t.comentarioEstudiante.isNotBlank()) {
                    SeccionTitulo(Icons.Default.ChatBubble, "Comentario del estudiante")
                    Text(t.comentarioEstudiante)
                }
                if (t.comentarioDocente.isNotBlank()) {
                    SeccionTitulo(Icons.Default.School, "Comentario del docente")
                    Text(t.comentarioDocente)
                }

                // Las fotos que entregó el estudiante (el docente las revisa aquí).
                if (evidencias.isNotEmpty()) {
                    SeccionTitulo(Icons.Default.PhotoLibrary, "Evidencias (${evidencias.size})")
                    GaleriaFotos(fotos = evidencias.map { it.uri })
                }

                if (validaciones.isNotEmpty()) {
                    SeccionTitulo(Icons.Default.History, "Historial de revisiones")
                    validaciones.forEach { v ->
                        // Cada revisión lleva un ícono verde (validada) o rojo (rechazada).
                        val valida = v.resultado == EstadoTarea.VALIDADA
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                if (valida) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (valida) ColorExito else ColorError,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${v.resultado} por ${nombre(v.validadorId)} el ${formatearFechaHora(v.fecha)}" +
                                        (if (v.comentario.isNotBlank()) ": ${v.comentario}" else ""),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (esDueno && t.estado == EstadoTarea.PENDIENTE_VALIDACION) {
                    Text(
                        "Tu trabajo está pendiente de validación por el docente.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // ---- Estudiante: iniciar ----
                // ---- Estudiante: iniciar. Pendiente -> En desarrollo ----
                if (puedeIniciar) {
                    Text(
                        "Cuando empieces a trabajar presiona Iniciar. Después podrás entregar tu evidencia.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = { viewModel.iniciar(t, usuario) },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Iniciar tarea")
                    }
                }

                // ---- Estudiante: entregar con comentario y evidencia ----
                // ---- Estudiante: entregar con comentario y foto. Pasa a "Pendiente de validación" ----
                if (puedeEntregar) {
                    if (t.estado == EstadoTarea.RECHAZADA) {
                        Text(
                            "El docente rechazó la entrega. Corrige el trabajo y vuelve a enviarlo.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    CampoTexto(
                        valor = comentario, etiqueta = "Comentario de tu trabajo",
                        icono = Icons.Default.Edit,
                        hayError = errorEntrega.comentario,
                        mensajeError = "El comentario es obligatorio (máx. 300 caracteres)",
                        onCambio = {
                            comentario = it
                            viewModel.quitarErrorEntrega()
                        }
                    )
                    SeccionTitulo(Icons.Default.PhotoCamera, "Evidencia fotográfica")
                    SelectorFoto(
                        fotos = fotos,
                        onFotoAgregada = {
                            fotos = fotos + it
                            viewModel.quitarErrorEntrega()
                        },
                        onFotoQuitada = { fotos = fotos - it }
                    )
                    if (errorEntrega.foto) TextoError("Adjunta al menos una fotografía")

                    // Animación: el aviso aparece cuando la entrega ya está completa
                    // ANIMACIÓN: el aviso verde aparece con una transición cuando la entrega ya está completa.
                    AnimatedVisibility(
                        visible = listoParaEnviar,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle, contentDescription = null,
                                tint = ColorExito
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Todo listo para enviar", color = ColorExito)
                        }
                    }
                    Button(
                        onClick = { viewModel.marcarRealizada(t, usuario, comentario, fotos) },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Entregar para validación")
                        }
                }

                // ---- Docente: aprobar o rechazar ----
                // ---- Docente: validar o rechazar. Rechazar exige escribir el motivo ----
                if (puedeRevisar) {
                    CampoTexto(
                        valor = comentario, etiqueta = "Comentario del docente",
                        icono = Icons.Default.Edit,
                        hayError = errorComentario,
                        mensajeError = "Para rechazar debes explicar el motivo",
                        onCambio = {
                            comentario = it
                            viewModel.limpiarErrorComentario()
                        }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.validar(t, usuario, comentario) },
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Validar")
                        }
                        OutlinedButton(
                            onClick = { viewModel.rechazar(t, usuario, comentario) },
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Rechazar")
                        }
                    }
                }
            }
        }
    }
}
