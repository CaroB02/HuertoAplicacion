package com.example.huertoaplicaciones.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertoaplicaciones.data.AppDatabase
import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.data.entity.Sector
import com.example.huertoaplicaciones.data.entity.Tarea
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.data.repository.CultivoRepository
import com.example.huertoaplicaciones.data.repository.SectorRepository
import com.example.huertoaplicaciones.data.repository.TareaRepository
import com.example.huertoaplicaciones.util.EstadoTarea
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.util.parsearFecha
import com.example.huertoaplicaciones.util.validarFecha
import com.example.huertoaplicaciones.util.validarObligatorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TareaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = TareaRepository(db.tareaDao())

    val tareas: StateFlow<List<Tarea>> = repository.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sectores: StateFlow<List<Sector>> = SectorRepository(db.sectorDao()).obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cultivos: StateFlow<List<Cultivo>> = CultivoRepository(db.cultivoDao()).obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val estudiantes: StateFlow<List<Usuario>> = db.usuarioDao().obtenerEstudiantes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun tareasDeEstudiante(estudianteId: Long): Flow<List<Tarea>> =
        repository.obtenerPorEstudiante(estudianteId)

    // ---------- Formulario de creación (solo docente) ----------
    private val _form = MutableStateFlow(TareaFormUiState())
    val form: StateFlow<TareaFormUiState> = _form.asStateFlow()

    fun nuevaTarea() { _form.value = TareaFormUiState() }

    fun cambiarTipo(v: String) = _form.update { it.copy(tipo = v) }
    fun cambiarDescripcion(v: String) = _form.update {
        it.copy(descripcion = v, errorDescripcion = !validarObligatorio(v))
    }
    fun cambiarInstrucciones(v: String) = _form.update {
        it.copy(instrucciones = v, errorInstrucciones = !validarObligatorio(v))
    }
    fun cambiarSector(id: Long) = _form.update {
        it.copy(sectorId = id, cultivoId = null, errorSector = false)
    }
    fun cambiarCultivo(id: Long) = _form.update { it.copy(cultivoId = id, errorCultivo = false) }
    fun cambiarEstudiante(id: Long) = _form.update { it.copy(estudianteId = id, errorEstudiante = false) }
    fun cambiarFecha(v: String) = _form.update {
        it.copy(fechaProgramada = v, errorFecha = !validarFecha(v))
    }

    fun guardar(creador: Usuario) {
        if (creador.perfil != Perfil.ADMIN) return   // solo docentes crean tareas

        val f = _form.value
        val eDescripcion = !validarObligatorio(f.descripcion)
        val eInstrucciones = !validarObligatorio(f.instrucciones)
        val eSector = f.sectorId == null
        val eCultivo = f.cultivoId == null
        val eEstudiante = f.estudianteId == null
        val eFecha = !validarFecha(f.fechaProgramada)

        _form.update {
            it.copy(
                errorDescripcion = eDescripcion, errorInstrucciones = eInstrucciones,
                errorSector = eSector, errorCultivo = eCultivo,
                errorEstudiante = eEstudiante, errorFecha = eFecha
            )
        }
        if (eDescripcion || eInstrucciones || eSector || eCultivo || eEstudiante || eFecha) return

        val sectorId = f.sectorId ?: return
        val cultivoId = f.cultivoId ?: return
        val estudianteId = f.estudianteId ?: return
        val fecha = parsearFecha(f.fechaProgramada) ?: return

        viewModelScope.launch {
            repository.guardar(
                Tarea(
                    tipo = f.tipo,
                    descripcion = f.descripcion.trim(),
                    instrucciones = f.instrucciones.trim(),
                    sectorId = sectorId,
                    cultivoId = cultivoId,
                    fechaCreacion = System.currentTimeMillis(),
                    fechaProgramada = fecha,
                    asignadoAId = estudianteId,
                    creadoPorId = creador.id,
                    estado = EstadoTarea.PENDIENTE
                )
            )
            _form.update { it.copy(guardado = true) }
        }
    }

    // ---------- Detalle y revisión ----------
    private val _seleccionada = MutableStateFlow<Tarea?>(null)
    val seleccionada: StateFlow<Tarea?> = _seleccionada.asStateFlow()

    private val _errorComentario = MutableStateFlow(false)
    val errorComentario: StateFlow<Boolean> = _errorComentario.asStateFlow()

    private val _accionHecha = MutableStateFlow(false)
    val accionHecha: StateFlow<Boolean> = _accionHecha.asStateFlow()

    fun seleccionar(id: Long) {
        _accionHecha.value = false
        _errorComentario.value = false
        viewModelScope.launch { _seleccionada.value = repository.obtenerPorId(id) }
    }

    // El estudiante marca SU tarea como realizada; queda pendiente de validación
    fun marcarRealizada(tarea: Tarea, estudiante: Usuario, comentario: String) {
        val puedeHacerla = tarea.estado == EstadoTarea.PENDIENTE ||
                tarea.estado == EstadoTarea.EN_DESARROLLO ||
                tarea.estado == EstadoTarea.RECHAZADA
        if (tarea.asignadoAId != estudiante.id || !puedeHacerla) return

        viewModelScope.launch {
            repository.actualizar(
                tarea.copy(
                    estado = EstadoTarea.PENDIENTE_VALIDACION,
                    fechaRealizacion = System.currentTimeMillis(),
                    comentarioEstudiante = comentario.trim()
                )
            )
            _accionHecha.value = true
        }
    }

    // Solo un docente puede validar; un estudiante nunca valida (ni su propia tarea)
    fun validar(tarea: Tarea, validador: Usuario, comentario: String) {
        if (validador.perfil != Perfil.ADMIN) return
        if (tarea.estado != EstadoTarea.PENDIENTE_VALIDACION) return

        viewModelScope.launch {
            repository.actualizar(
                tarea.copy(
                    estado = EstadoTarea.VALIDADA,
                    comentarioDocente = comentario.trim(),
                    validadoPorId = validador.id,
                    fechaValidacion = System.currentTimeMillis()
                )
            )
            _accionHecha.value = true
        }
    }

    // Rechazar exige explicar el motivo
    fun rechazar(tarea: Tarea, validador: Usuario, comentario: String) {
        if (validador.perfil != Perfil.ADMIN) return
        if (tarea.estado != EstadoTarea.PENDIENTE_VALIDACION) return
        if (!validarObligatorio(comentario)) {
            _errorComentario.value = true
            return
        }
        viewModelScope.launch {
            repository.actualizar(
                tarea.copy(
                    estado = EstadoTarea.RECHAZADA,
                    comentarioDocente = comentario.trim(),
                    validadoPorId = validador.id,
                    fechaValidacion = System.currentTimeMillis()
                )
            )
            _accionHecha.value = true
        }
    }

    fun limpiarErrorComentario() { _errorComentario.value = false }
}