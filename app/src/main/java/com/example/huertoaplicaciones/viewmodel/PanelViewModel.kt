package com.example.huertoaplicaciones.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertoaplicaciones.data.AppDatabase
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.data.repository.CultivoRepository
import com.example.huertoaplicaciones.data.repository.LecturaSensorRepository
import com.example.huertoaplicaciones.data.repository.ProblemaRepository
import com.example.huertoaplicaciones.data.repository.SectorRepository
import com.example.huertoaplicaciones.data.repository.TareaRepository
import com.example.huertoaplicaciones.data.repository.UsuarioRepository
import com.example.huertoaplicaciones.util.Aviso
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.util.ResumenTareas
import com.example.huertoaplicaciones.util.avisosDocente
import com.example.huertoaplicaciones.util.avisosEstudiante
import com.example.huertoaplicaciones.util.resumirTareas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// Lo que muestra el panel: el resumen de tareas (los números y la barra) y la lista de avisos.
data class PanelUiState(
    val resumen: ResumenTareas = ResumenTareas(),
    val avisos: List<Aviso> = emptyList()
)

// Alimenta el Dashboard de cada rol: resumen de tareas y avisos/recordatorios
// AndroidViewModel = un ViewModel que además recibe el Application (lo necesito para abrir la base).
class PanelViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    // Un repositorio por cada tabla que necesita el panel (el ViewModel nunca usa los DAO directamente).
    private val tareaRepository = TareaRepository(db)
    private val cultivoRepository = CultivoRepository(db.cultivoDao())
    private val problemaRepository = ProblemaRepository(db.problemaDao())
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())
    private val sectorRepository = SectorRepository(db.sectorDao())
    private val lecturaRepository = LecturaSensorRepository(db.lecturaSensorDao())
    // Quién inició sesión. Empieza en null y AppNavigation lo define al hacer login.
    private val usuario = MutableStateFlow<Usuario?>(null)

    fun definirUsuario(u: Usuario?) { usuario.value = u }

    // Lecturas simuladas y sectores, usados solo en los avisos del docente
    // combine junta dos Flows en uno: cada vez que cualquiera cambia, se recalcula el par.
    private val sensores = combine(
        lecturaRepository.obtenerTodas(),
        sectorRepository.obtenerTodos()
    ) { lecturas, sectores -> lecturas to sectores }

    // COMBINE: junto el usuario y todas las tablas en un solo flujo. Si cambia CUALQUIERA de ellos
    // (otro usuario inicia sesión, una tarea cambia de estado, llega un problema...),
    // el bloque se ejecuta de nuevo y el panel se actualiza solo, sin que yo haga nada.
    val estado: StateFlow<PanelUiState> = combine(
        usuario,
        tareaRepository.obtenerTodas(),
        cultivoRepository.obtenerTodos(),
        problemaRepository.obtenerTodos(),
        combine(usuarioRepository.obtenerTodos(), sensores) { usuarios, s -> usuarios to s }
    ) { u, tareas, cultivos, problemas, (usuarios, sens) ->
        // Según el perfil armo un panel distinto (cada rol ve cosas diferentes).
        when (u?.perfil) {
            // El docente ve el resumen de TODAS las tareas y todos los avisos.
            Perfil.ADMIN -> PanelUiState(
                resumen = resumirTareas(tareas),
                avisos = avisosDocente(
                    tareas, cultivos, problemas, usuarios,
                    lecturas = sens.first, sectores = sens.second
                )
            )
            // El estudiante solo ve lo suyo: filtro las tareas asignadas a él.
            Perfil.ESTUDIANTE -> {
                val propias = tareas.filter { it.asignadoAId == u.id }
                PanelUiState(
                    resumen = resumirTareas(propias),
                    avisos = avisosEstudiante(propias, cultivos, tareas)
                )
            }
            else -> PanelUiState()
        }
        // stateIn convierte el Flow en StateFlow (siempre tiene un valor actual).
        // WhileSubscribed(5000): sigue activo mientras haya pantallas mirándolo
        // (y 5 segundos más, así una rotación de pantalla no lo reinicia).
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PanelUiState())
}
