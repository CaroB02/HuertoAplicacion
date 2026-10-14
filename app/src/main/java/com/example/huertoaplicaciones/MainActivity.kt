package com.example.huertoaplicaciones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.huertoaplicaciones.data.AppDatabase
import com.example.huertoaplicaciones.data.cargarDatosIniciales
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.ui.screens.LoginScreen
import com.example.huertoaplicaciones.ui.theme.HuertoAplicacionesTheme
import com.example.huertoaplicaciones.viewmodel.AuthViewModel
import com.example.huertoaplicaciones.viewmodel.AuthViewModelFactory
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

        setContent {
            HuertoAplicacionesTheme {
                var usuario by remember { mutableStateOf<Usuario?>(null) }
                val u = usuario
                if (u == null) {
                    LoginScreen(authViewModel) { usuario = it }
                } else {
                    Text("Bienvenido ${u.nombre} (${u.perfil})")
                }
            }
        }
    }
}