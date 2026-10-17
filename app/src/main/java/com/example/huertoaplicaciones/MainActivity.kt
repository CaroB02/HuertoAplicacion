package com.example.huertoaplicaciones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.huertoaplicaciones.data.AppDatabase
import com.example.huertoaplicaciones.data.cargarDatosIniciales
import com.example.huertoaplicaciones.ui.navigation.AppNavigation
import com.example.huertoaplicaciones.ui.theme.HuertoAplicacionesTheme
import com.example.huertoaplicaciones.viewmodel.AuthViewModel
import com.example.huertoaplicaciones.viewmodel.AuthViewModelFactory
import com.example.huertoaplicaciones.viewmodel.CultivoViewModel
import com.example.huertoaplicaciones.viewmodel.SectorViewModel
import com.example.huertoaplicaciones.viewmodel.TareaViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        lifecycleScope.launch { cargarDatosIniciales(db) }

        val authViewModel = ViewModelProvider(
            this, AuthViewModelFactory(db.usuarioDao())
        )[AuthViewModel::class.java]
        val sectorViewModel = ViewModelProvider(this)[SectorViewModel::class.java]
        val cultivoViewModel = ViewModelProvider(this)[CultivoViewModel::class.java]
        val tareaViewModel = ViewModelProvider(this)[TareaViewModel::class.java]

        setContent {
            HuertoAplicacionesTheme {
                AppNavigation(authViewModel, sectorViewModel, cultivoViewModel, tareaViewModel)
            }
        }
    }
}