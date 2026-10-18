package com.example.huertoaplicaciones.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.huertoaplicaciones.util.EstadoCultivo
import com.example.huertoaplicaciones.util.EstadoProblema
import com.example.huertoaplicaciones.util.EstadoSector
import com.example.huertoaplicaciones.util.EstadoTarea

// Aquí están TODOS los colores de la app, en un solo lugar (así no repito códigos de color por las pantallas).
// Un color se escribe Color(0xFFRRGGBB): 0xFF = totalmente opaco, y luego rojo, verde y azul en hexadecimal.
// ---------- Paleta de marca (verde huerto) ----------
val Verde80 = Color(0xFFA5D6A7)
val VerdeGris80 = Color(0xFFB9CCB9)
val Tierra80 = Color(0xFFE0C097)

val Verde40 = Color(0xFF2E7D32)
val VerdeGris40 = Color(0xFF52634F)
val Tierra40 = Color(0xFF8D6E4A)

// Fondos y contenedores en tonos verdes (Material 3 trae morados por defecto; los cambio para que
// la barra superior, las tarjetas y los fondos combinen con la paleta del huerto).
val VerdeContenedorClaro = Color(0xFFC8E6C9)
val SobreVerdeContenedorClaro = Color(0xFF0B3D12)
val FondoClaro = Color(0xFFF6FBF3)
val SuperficieVarianteClara = Color(0xFFE1E9DB)
val VerdeContenedorOscuro = Color(0xFF1B5E20)
val SobreVerdeContenedorOscuro = Color(0xFFC8E6C9)
val FondoOscuro = Color(0xFF101410)
// Contenedor secundario (barra de progreso, botones de selección, etc.): verde grisáceo.
// Superficie de ventanas emergentes y menús (por defecto sería lila).
val SuperficieAltaClara = Color(0xFFEAF2E5)
val SuperficieAltaOscura = Color(0xFF252E23)
val SecundarioContenedorClaro = Color(0xFFD3E8CF)
val SobreSecundarioContenedorClaro = Color(0xFF0F2410)
val SecundarioContenedorOscuro = Color(0xFF3A4B38)
val SobreSecundarioContenedorOscuro = Color(0xFFD3E8CF)
val SuperficieVarianteOscura = Color(0xFF2B3329)

// Colores SEMÁNTICOS: el nombre dice para qué sirven (éxito, advertencia, información, error)
// y no cuál es el color. Si mañana cambio el verde de éxito, lo cambio solo aquí.
// Los "Contenedor" son versiones suaves, para fondos de tarjetas.
// ---------- Colores semánticos de retroalimentación ----------
val ColorExito = Color(0xFF2E7D32)
val ColorExitoContenedor = Color(0xFFC8E6C9)
val ColorAdvertencia = Color(0xFFEF6C00)
val ColorAdvertenciaContenedor = Color(0xFFFFE0B2)
val ColorInfo = Color(0xFF1565C0)
val ColorInfoContenedor = Color(0xFFBBDEFB)
val ColorError = Color(0xFFC62828)
val ColorErrorContenedor = Color(0xFFFFCDD2)

// Color neutro y texto sobre los contenedores pastel (se ven bien en tema claro y oscuro)
val ColorNeutroContenedor = Color(0xFFE0E0E0)
val ColorTextoSobreContenedor = Color(0xFF1B1B1B)

// De aquí en adelante: una función por cada tipo de estado, que devuelve su color.
// Las tarjetas llaman a estas funciones en vez de escribir colores a mano.
// ---------- Estados de tareas ----------
val ColorTareaPendiente = Color(0xFFFFF9C4)
val ColorTareaAtrasada = Color(0xFFFFAB91)

fun colorEstadoTarea(estado: String): Color = when (estado) {
    EstadoTarea.PENDIENTE -> ColorTareaPendiente
    EstadoTarea.EN_DESARROLLO -> ColorInfoContenedor
    EstadoTarea.PENDIENTE_VALIDACION -> ColorAdvertenciaContenedor
    EstadoTarea.VALIDADA -> ColorExitoContenedor
    EstadoTarea.RECHAZADA -> ColorErrorContenedor
    EstadoTarea.ATRASADA -> ColorTareaAtrasada
    else -> ColorNeutroContenedor
}

// ---------- Estados de sectores ----------
fun colorEstadoSector(estado: String): Color = when (estado) {
    EstadoSector.DISPONIBLE -> ColorExitoContenedor
    EstadoSector.EN_USO -> ColorInfoContenedor
    EstadoSector.EN_MANTENIMIENTO -> ColorAdvertenciaContenedor
    else -> ColorNeutroContenedor
}

// ---------- Estados de cultivos ----------
fun colorEstadoCultivo(estado: String): Color = when (estado) {
    EstadoCultivo.SEMBRADO -> ColorTareaPendiente
    EstadoCultivo.EN_CRECIMIENTO -> ColorExitoContenedor
    EstadoCultivo.LISTO_COSECHA -> ColorAdvertenciaContenedor
    EstadoCultivo.COSECHADO -> ColorInfoContenedor
    EstadoCultivo.CON_PROBLEMAS -> ColorErrorContenedor
    else -> ColorNeutroContenedor
}

// ---------- Estados de reportes de problemas ----------
fun colorEstadoProblema(estado: String): Color = when (estado) {
    EstadoProblema.INFORMADO -> ColorErrorContenedor
    EstadoProblema.EN_REVISION -> ColorAdvertenciaContenedor
    EstadoProblema.ATENDIDO -> ColorInfoContenedor
    EstadoProblema.CERRADO -> ColorExitoContenedor
    else -> ColorNeutroContenedor
}
