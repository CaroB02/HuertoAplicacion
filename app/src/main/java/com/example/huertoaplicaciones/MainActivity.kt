package com.example.huertoaplicaciones

import com.example.huertoaplicaciones.viewmodel.RegistroViewModel
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.huertoaplicaciones.data.AppDatabase
import com.example.huertoaplicaciones.data.cargarDatosIniciales
import com.example.huertoaplicaciones.data.repository.UsuarioRepository
import com.example.huertoaplicaciones.ui.navigation.AppNavigation
import com.example.huertoaplicaciones.ui.theme.HuertoAplicacionesTheme
import com.example.huertoaplicaciones.viewmodel.AuthViewModel
import com.example.huertoaplicaciones.viewmodel.AuthViewModelFactory
import com.example.huertoaplicaciones.viewmodel.CultivoViewModel
import com.example.huertoaplicaciones.viewmodel.HistorialViewModel
import com.example.huertoaplicaciones.viewmodel.PanelViewModel
import com.example.huertoaplicaciones.viewmodel.ProblemaViewModel
import com.example.huertoaplicaciones.viewmodel.SectorViewModel
import com.example.huertoaplicaciones.viewmodel.TareaViewModel
import kotlinx.coroutines.launch

// MainActivity es la PUERTA DE ENTRADA de la app: Android la abre al tocar el ícono.
// Hace tres cosas: abre la base de datos, crea los ViewModels y dibuja la interfaz con setContent.
// La interfaz entera está hecha con Jetpack Compose, por eso aquí solo hay UNA Activity.
class MainActivity : ComponentActivity() {

    // Toda la app está escrita en español, así que le pido a Android que use español aquí sin importar el
    // idioma del teléfono. Así los textos que pone el propio sistema (por ejemplo el calendario de
    // fechas: "octubre", "lunes"...) salen en español y no mezclados con inglés.
    override fun attachBaseContext(newBase: Context) {
        val idioma = Locale("es", "CL")
        Locale.setDefault(idioma)
        val configuracion = Configuration(newBase.resources.configuration)
        configuracion.setLocale(idioma)
        super.attachBaseContext(newBase.createConfigurationContext(configuracion))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // La app se dibuja detrás de la barra de estado y la de navegación (pantalla completa moderna).
        enableEdgeToEdge()

        // Abro (o creo) la base de datos Room.
        val db = AppDatabase.getDatabase(applicationContext)
        // Cargo los datos de prueba en segundo plano; solo lo hace si la base está vacía.
        lifecycleScope.launch { cargarDatosIniciales(db, applicationContext) }

        // Creo los ViewModels. ViewModelProvider(this) los asocia a esta Activity: sobreviven a las rotaciones
        // de pantalla. El de login necesita una fábrica porque recibe el repositorio como parámetro.
        val authViewModel = ViewModelProvider(
            this, AuthViewModelFactory(UsuarioRepository(db.usuarioDao()))
        )[AuthViewModel::class.java]
        val sectorViewModel = ViewModelProvider(this)[SectorViewModel::class.java]
        val cultivoViewModel = ViewModelProvider(this)[CultivoViewModel::class.java]
        val tareaViewModel = ViewModelProvider(this)[TareaViewModel::class.java]
        val problemaViewModel = ViewModelProvider(this)[ProblemaViewModel::class.java]
        val panelViewModel = ViewModelProvider(this)[PanelViewModel::class.java]
        val historialViewModel = ViewModelProvider(this)[HistorialViewModel::class.java]
        val registroViewModel = ViewModelProvider(this)[RegistroViewModel::class.java]

        // setContent: aquí empieza la interfaz Compose. Todo va dentro del tema de la app (colores y tipografía)
        // y AppNavigation decide qué pantalla mostrar.
        setContent {
            HuertoAplicacionesTheme {
                AppNavigation(
                    authViewModel, sectorViewModel, cultivoViewModel, tareaViewModel,
                    problemaViewModel, panelViewModel, historialViewModel, registroViewModel
                )
            }
        }
    }
}
