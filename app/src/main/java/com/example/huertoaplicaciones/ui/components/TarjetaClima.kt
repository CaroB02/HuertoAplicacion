package com.example.huertoaplicaciones.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertoaplicaciones.ui.theme.ColorInfo
import com.example.huertoaplicaciones.util.ClimaHuerto
import com.example.huertoaplicaciones.viewmodel.ClimaUiState
import java.util.Locale

// Tarjeta del CLIMA (API externa Open-Meteo) que aparece en el panel de los dos roles.
// Dibuja algo distinto según el estado de la consulta (patrón visto en clase):
//  Cargando -> ruedita de progreso | Error -> mensaje + "Reintentar" | Exito -> datos del clima
@Composable
fun TarjetaClima(estado: ClimaUiState, onReintentar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SeccionTitulo(Icons.Default.WbSunny, "Clima del huerto")

            when (estado) {
                is ClimaUiState.Cargando -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ClimaUiState.Error -> {
                    Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = onReintentar) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Reintentar")
                    }
                }

                is ClimaUiState.Exito -> ContenidoClima(estado.clima)
            }

            // Se aclara de dónde vienen los datos: es una API externa, no un microservicio propio
            Text(
                "Fuente: Open-Meteo (API externa)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContenidoClima(clima: ClimaHuerto) {
    // Temperatura grande y descripción del cielo
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "${grados(clima.temperatura)}°C",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(12.dp))
        Text(clima.descripcion, style = MaterialTheme.typography.titleMedium)
    }

    // Humedad, viento y lluvia esperada hoy
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Dato(Icons.Default.WaterDrop, "Humedad ${clima.humedad}%")
        Dato(Icons.Default.Air, "Viento ${grados(clima.viento)} km/h")
        Dato(Icons.Default.Umbrella, "Lluvia ${clima.probabilidadLluviaHoy}%")
    }

    // Pronóstico de los próximos días
    clima.dias.forEach { dia ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(dia.etiqueta, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${dia.descripcion} · ${grados(dia.minima)}° / ${grados(dia.maxima)}° · ${decimal(dia.lluviaMm)} mm",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    // Recomendación de riego calculada a partir del pronóstico (util/Clima.kt)
    Text(
        clima.recomendacion,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ColorInfo.copy(alpha = 0.10f))
            .padding(10.dp)
    )
}

@Composable
private fun Dato(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = ColorInfo)
        Spacer(Modifier.width(4.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall)
    }
}

// Redondea a entero para mostrar (21.6 -> "22")
private fun grados(valor: Double): String = String.format(Locale.US, "%.0f", valor)

// Un decimal para los milímetros de lluvia (0.25 -> "0.3")
private fun decimal(valor: Double): String = String.format(Locale.US, "%.1f", valor)
