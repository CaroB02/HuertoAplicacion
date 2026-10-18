package com.example.huertoaplicaciones.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// TEMA de la app (Material Design 3). Aquí defino los colores principales para el modo oscuro y el claro.
// Las pantallas nunca escriben colores a mano: usan MaterialTheme.colorScheme.primary, .error, etc.
// Así, si quiero cambiar el estilo de toda la app, lo cambio solo aquí.

// Paleta del modo oscuro (colores más claros, para que se vean sobre fondo oscuro).
private val DarkColorScheme = darkColorScheme(
    primary = Verde80,
    secondary = VerdeGris80,
    tertiary = Tierra80,
    primaryContainer = VerdeContenedorOscuro,
    onPrimaryContainer = SobreVerdeContenedorOscuro,
    secondaryContainer = SecundarioContenedorOscuro,
    onSecondaryContainer = SobreSecundarioContenedorOscuro,
    background = FondoOscuro,
    surface = FondoOscuro,
    surfaceVariant = SuperficieVarianteOscura,
    surfaceContainerHighest = SuperficieVarianteOscura,
    surfaceContainerHigh = SuperficieAltaOscura
)

// Paleta del modo claro (colores más oscuros, para que se vean sobre fondo claro).
private val LightColorScheme = lightColorScheme(
    primary = Verde40,
    secondary = VerdeGris40,
    tertiary = Tierra40,
    primaryContainer = VerdeContenedorClaro,
    onPrimaryContainer = SobreVerdeContenedorClaro,
    secondaryContainer = SecundarioContenedorClaro,
    onSecondaryContainer = SobreSecundarioContenedorClaro,
    background = FondoClaro,
    surface = FondoClaro,
    surfaceVariant = SuperficieVarianteClara,
    surfaceContainerHighest = SuperficieVarianteClara,
    surfaceContainerHigh = SuperficieAltaClara

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

// Este Composable envuelve TODA la app (se usa en MainActivity). Todo lo que quede adentro
// hereda estos colores y estas tipografías.
@Composable
fun HuertoAplicacionesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),   // sigue el modo oscuro o claro del teléfono
    // Se desactiva el color dinámico para conservar la paleta verde del huerto
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Elijo la paleta según el modo del teléfono (y el color dinámico de Android 12+, que dejé apagado).
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
