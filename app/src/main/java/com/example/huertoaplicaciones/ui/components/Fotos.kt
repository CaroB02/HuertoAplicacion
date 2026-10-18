package com.example.huertoaplicaciones.ui.components

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.huertoaplicaciones.util.FotoUtils
import com.example.huertoaplicaciones.util.Mensajes
import kotlinx.coroutines.launch
import java.io.File

// Este archivo tiene los componentes de los RECURSOS NATIVOS (galería y cámara).

// Vista previa de una lista de fotos (solo lectura)
// Se usa en el detalle de la tarea para que el docente vea las evidencias del estudiante.
@Composable
fun GaleriaFotos(fotos: List<String>, modifier: Modifier = Modifier) {
    // LazyRow = una fila deslizable que solo dibuja las fotos visibles (eficiente con muchas fotos).
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "key" le da a cada foto una identidad estable, para que Compose no las confunda al recomponer.
        items(fotos, key = { it }) { uri ->
            Miniatura(uri)
        }
    }
}

// Una foto pequeña con esquinas redondeadas. Si recibe onQuitar, muestra una "X" para quitarla.
@Composable
private fun Miniatura(uri: String, onQuitar: (() -> Unit)? = null) {
    // Box apila elementos uno encima de otro: la foto abajo y el botón X arriba a la derecha.
    Box {
        // AsyncImage (librería Coil) carga la imagen desde su ruta SIN congelar la pantalla.
        AsyncImage(
            model = uri,
            contentDescription = "Fotografía adjunta",
            // Crop = la imagen llena el cuadrado recortando lo que sobra (sin deformarse).
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        if (onQuitar != null) {
            FilledTonalIconButton(
                onClick = onQuitar,
                modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Quitar foto")
            }
        }
    }
}

// Permite elegir una foto de la galería (Photo Picker) o tomarla con la cámara.
// El Photo Picker y la cámara del sistema no requieren permisos en tiempo de ejecución.
@Composable
fun SelectorFoto(
    fotos: List<String>,
    onFotoAgregada: (String) -> Unit,
    onFotoQuitada: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxFotos: Int = 3
) {
    val context = LocalContext.current
    // Una corrutina propia de este componente, para copiar la foto en segundo plano.
    val scope = rememberCoroutineScope()
    // La ruta se guarda para sobrevivir a una rotación mientras la cámara está abierta
    // (rememberSaveable guarda el valor aunque la pantalla se destruya y se vuelva a crear).
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    val puedeAgregar = fotos.size < maxFotos

    // GALERÍA: "launcher" = pide algo a otra pantalla del sistema y recibe el resultado en el bloque.
    // PickVisualMedia es el Photo Picker moderno de Android: no necesita permisos.
    val selector = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        // uri == null significa que el usuario cerró el selector sin elegir nada (cancelación).
        if (uri == null) {
            Mensajes.error("No se seleccionó ninguna foto")
        } else {
            scope.launch {
                // Copio la foto a mi carpeta para que no se pierda al reiniciar la app.
                val guardada = FotoUtils.copiarAAlmacenamiento(context, uri)
                if (guardada != null) onFotoAgregada(guardada)
                else Mensajes.error("No se pudo guardar la foto seleccionada")
            }
        }
    }

    // CÁMARA: TakePicture abre la cámara del sistema y le dice dónde guardar la foto.
    // Devuelve true si el usuario sacó la foto y false si canceló.
    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val archivo = rutaPendiente?.let { File(it) }
        rutaPendiente = null
        // Solo acepto la foto si salió bien Y el archivo tiene contenido.
        if (exito && archivo != null && archivo.length() > 0) {
            onFotoAgregada(FotoUtils.uriDeArchivo(archivo))
        } else {
            // Cancelado: borro el archivo vacío que había preparado para no dejar basura.
            archivo?.delete()
            Mensajes.error("Captura cancelada: no se guardó ninguna foto")
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                // Se deshabilita cuando ya llegué al máximo de fotos.
                enabled = puedeAgregar,
                onClick = {
                    selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Galería")
            }
            OutlinedButton(
                enabled = puedeAgregar,
                onClick = {
                    // Preparo el archivo donde la cámara va a guardar la foto, y recuerdo su ruta.
                    val (archivo, uri) = FotoUtils.prepararCaptura(context)
                    rutaPendiente = archivo.absolutePath
                    try {
                        camara.launch(uri)
                    } catch (e: ActivityNotFoundException) {
                        // El dispositivo no tiene app de cámara (pasa en algunos emuladores).
                        rutaPendiente = null
                        archivo.delete()
                        Mensajes.error("Este dispositivo no tiene una aplicación de cámara")
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cámara")
            }
        }
        if (!puedeAgregar) {
            Text(
                "Máximo $maxFotos foto(s). Quita una para agregar otra.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Animación: la vista previa aparece y se oculta suavemente
        // AnimatedVisibility muestra u oculta su contenido con una transición (fundido + deslizamiento).
        AnimatedVisibility(
            visible = fotos.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(fotos, key = { it }) { uri ->
                    Miniatura(uri = uri, onQuitar = { onFotoQuitada(uri) })
                }
            }
        }
    }
}
