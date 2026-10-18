package com.example.huertoaplicaciones.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertoaplicaciones.data.AppDatabase
import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.data.entity.Evidencia
import com.example.huertoaplicaciones.data.entity.Sector
import com.example.huertoaplicaciones.data.entity.Tarea
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.data.entity.Validacion
import com.example.huertoaplicaciones.data.repository.CultivoRepository
import com.example.huertoaplicaciones.data.repository.SectorRepository
import com.example.huertoaplicaciones.data.repository.TareaRepository
import com.example.huertoaplicaciones.data.repository.UsuarioRepository
import com.example.huertoaplicaciones.util.EstadoTarea
import com.example.huertoaplicaciones.util.Limites
import com.example.huertoaplicaciones.util.MILIS_POR_DIA
import com.example.huertoaplicaciones.util.Mensajes
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.util.estadoMostrado
import com.example.huertoaplicaciones.util.formatearFecha
import com.example.huertoaplicaciones.util.inicioDelDia
import com.example.huertoaplicaciones.util.parsearFecha
import com.example.huertoaplicaciones.util.validarFechaProgramada
import com.example.huertoaplicaciones.util.validarObligatorio
import com.example.huertoaplicaciones.util.validarTexto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel de las TAREAS: el más importante del proyecto. Aquí vive todo el flujo de estados:
//   Pendiente -> En desarrollo -> Pendiente de validación -> Validada o Rechazada
// y las reglas de negocio (quién puede hacer cada paso). Las pantallas solo muestran y envían eventos.
class TareaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = TareaRepository(db)
    private val cultivoRepository = CultivoRepository(db.cultivoDao())
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())

    // Listas que las pantallas observan. Todas son StateFlow: cuando cambia la base, se actualizan solas.
    val tareas: StateFlow<List<Tarea>> = repository.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sectores: StateFlow<List<Sector>> = SectorRepository(db.sectorDao()).obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cultivos: StateFlow<List<Cultivo>> = cultivoRepository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Solo los estudiantes: son los únicos a quienes se les puede asignar una tarea.
    val estudiantes: StateFlow<List<Usuario>> = usuarioRepository.obtenerEstudiantes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val usuarios: StateFlow<List<Usuario>> = usuarioRepository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Las tareas de UN estudiante (la pantalla "Mis tareas"): así nunca ve las de otros.
    fun tareasDeEstudiante(estudianteId: Long): Flow<List<Tarea>> =
        repository.obtenerPorEstudiante(estudianteId)

    // ---------- Filtros del listado (docente) ----------
    private val _filtros = MutableStateFlow(FiltrosTarea())
    val filtros: StateFlow<FiltrosTarea> = _filtros.asStateFlow()

    // La lista FILTRADA se recalcula sola cuando cambia la lista de tareas O los filtros elegidos.
    // combine junta ambos flujos: es la forma de que la pantalla siempre muestre lo correcto.
    val tareasFiltradas: StateFlow<List<Tarea>> = combine(tareas, _filtros) { lista, f ->
        val hoy = inicioDelDia(System.currentTimeMillis())
        lista.filter { t ->
            // Una tarea se muestra solo si cumple los TRES criterios a la vez (por eso el && del final).
            // "null" en un filtro significa "no filtrar por este criterio" (acepta todo).
            val porEstado = f.estado == null || estadoMostrado(t) == f.estado
            val porResponsable = f.responsableId == null || t.asignadoAId == f.responsableId
            val porFecha = when (f.fecha) {
                FiltroFecha.TODAS -> true
                FiltroFecha.HOY -> t.fechaProgramada in hoy until hoy + MILIS_POR_DIA
                FiltroFecha.SEMANA -> t.fechaProgramada in hoy until hoy + 7 * MILIS_POR_DIA
            }
            porEstado && porResponsable && porFecha
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cada función cambia un solo filtro y deja los otros igual (copy).
    fun filtrarPorEstado(estado: String?) = _filtros.update { it.copy(estado = estado) }
    fun filtrarPorResponsable(id: Long?) = _filtros.update { it.copy(responsableId = id) }
    fun filtrarPorFecha(fecha: FiltroFecha) = _filtros.update { it.copy(fecha = fecha) }
    fun limpiarFiltros() { _filtros.value = FiltrosTarea() }

    // ---------- Formulario de creación y edición (solo docente) ----------
    private val _form = MutableStateFlow(TareaFormUiState())
    val form: StateFlow<TareaFormUiState> = _form.asStateFlow()

    // Antes de abrir el formulario para una tarea nueva lo dejo vacío.
    fun nuevaTarea() { _form.value = TareaFormUiState() }

    // Para EDITAR: cargo la tarea y relleno el formulario (la fecha pasa de Long a texto).
    fun editarTarea(id: Long) {
        viewModelScope.launch {
            repository.obtenerPorId(id)?.let { t ->
                _form.value = TareaFormUiState(
                    id = t.id, tipo = t.tipo, descripcion = t.descripcion,
                    instrucciones = t.instrucciones, sectorId = t.sectorId,
                    cultivoId = t.cultivoId, estudianteId = t.asignadoAId,
                    fechaProgramada = formatearFecha(t.fechaProgramada),
                    fechaOriginal = t.fechaProgramada
                )
            }
        }
    }

    // Cada función de aquí abajo se llama cuando el usuario cambia un campo del formulario.
    // Las que tienen validación recalculan su error EN TIEMPO REAL, mientras escribe.
    fun cambiarTipo(v: String) = _form.update { it.copy(tipo = v) }
    fun cambiarDescripcion(v: String) = _form.update {
        it.copy(descripcion = v, errorDescripcion = !validarTexto(v, Limites.DESCRIPCION))
    }
    fun cambiarInstrucciones(v: String) = _form.update {
        it.copy(instrucciones = v, errorInstrucciones = !validarTexto(v, Limites.COMENTARIO))
    }
    // Al cambiar de sector borro el cultivo, porque cada cultivo pertenece a un sector.
    fun cambiarSector(id: Long) = _form.update {
        it.copy(sectorId = id, cultivoId = null, errorSector = false)
    }
    fun cambiarCultivo(id: Long) = _form.update { it.copy(cultivoId = id, errorCultivo = false) }
    fun cambiarEstudiante(id: Long) = _form.update { it.copy(estudianteId = id, errorEstudiante = false) }
    fun cambiarFecha(v: String) = _form.update {
        it.copy(fechaProgramada = v, errorFecha = !validarFechaProgramada(v, it.fechaOriginal))
    }

    // Guarda la tarea (nueva o editada). Es el punto donde se aplican las reglas de negocio.
    fun guardar(creador: Usuario) {
        // REGLA 1: solo el docente crea y asigna tareas. Aunque la pantalla ya lo restringe,
        // lo verifico aquí también (control de acceso en la capa de lógica, no solo en la interfaz).
        if (creador.perfil != Perfil.ADMIN) return   // solo docentes crean tareas

        val f = _form.value
        // REGLA 2: todos los campos obligatorios (tipo, descripción, responsable, fecha...).
        val eDescripcion = !validarTexto(f.descripcion, Limites.DESCRIPCION)
        val eInstrucciones = !validarTexto(f.instrucciones, Limites.COMENTARIO)
        val eSector = f.sectorId == null
        val eCultivo = f.cultivoId == null
        val eEstudiante = f.estudianteId == null
        // La fecha debe ser de hoy en adelante (o la misma que ya tenía la tarea, si se está editando).
        val eFecha = !validarFechaProgramada(f.fechaProgramada, f.fechaOriginal)

        _form.update {
            it.copy(
                errorDescripcion = eDescripcion, errorInstrucciones = eInstrucciones,
                errorSector = eSector, errorCultivo = eCultivo,
                errorEstudiante = eEstudiante, errorFecha = eFecha
            )
        }
        if (eDescripcion || eInstrucciones || eSector || eCultivo || eEstudiante || eFecha) {
            Mensajes.error("Revisa los campos marcados en rojo")
            return
        }

        // Ya validé que no son null; "?: return" le dice a Kotlin que de aquí en adelante tampoco lo son.
        val sectorId = f.sectorId ?: return
        val cultivoId = f.cultivoId ?: return
        val estudianteId = f.estudianteId ?: return
        val fecha = parsearFecha(f.fechaProgramada) ?: return

        viewModelScope.launch {
            // REGLA 3: el responsable debe EXISTIR, estar activo y ser estudiante.
            // El responsable debe existir, estar activo y ser estudiante
            val responsable = usuarioRepository.obtenerPorId(estudianteId)
            if (responsable == null || responsable.perfil != Perfil.ESTUDIANTE || !responsable.activo) {
                _form.update { it.copy(errorEstudiante = true) }
                Mensajes.error("El responsable no existe o no está autorizado para recibir tareas")
                return@launch
            }
            // REGLA 4: el cultivo debe pertenecer al sector elegido.
            // El cultivo debe pertenecer al sector elegido
            val cultivo = cultivoRepository.obtenerPorId(cultivoId)
            if (cultivo == null || cultivo.sectorId != sectorId) {
                _form.update { it.copy(errorCultivo = true) }
                Mensajes.error("El cultivo seleccionado no pertenece a ese sector")
                return@launch
            }

            try {
                // id == 0 = tarea nueva (INSERT); si no, edición (UPDATE).
                if (f.id == 0L) {
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
                            // Toda tarea nueva empieza en "Pendiente".
                            estado = EstadoTarea.PENDIENTE
                        )
                    )
                    Mensajes.exito("Tarea creada y asignada a ${responsable.nombre}")
                } else {
                    val actual = repository.obtenerPorId(f.id) ?: return@launch
                    // REGLA 5: solo se editan tareas que NO se han empezado.
                    if (actual.estado != EstadoTarea.PENDIENTE) {
                        Mensajes.error("Solo se pueden editar tareas que aún están pendientes")
                        return@launch
                    }
                    // copy() mantiene todos los datos de la tarea y cambia solo los que se editaron.
                    repository.actualizar(
                        actual.copy(
                            tipo = f.tipo,
                            descripcion = f.descripcion.trim(),
                            instrucciones = f.instrucciones.trim(),
                            sectorId = sectorId,
                            cultivoId = cultivoId,
                            fechaProgramada = fecha,
                            asignadoAId = estudianteId
                        )
                    )
                    // Refresco la tarea abierta para que el detalle muestre los datos nuevos.
                    _seleccionada.value = repository.obtenerPorId(f.id)
                    Mensajes.exito("Tarea actualizada")
                }
                _form.update { it.copy(guardado = true) }
            } catch (e: Exception) {
                Mensajes.error("No se pudo guardar la tarea")
            }
        }
    }

    // ---------- Detalle, entrega y revisión ----------
    // La tarea que tengo abierta en la pantalla de detalle (null mientras carga).
    private val _seleccionada = MutableStateFlow<Tarea?>(null)
    val seleccionada: StateFlow<Tarea?> = _seleccionada.asStateFlow()

    // Las fotos y las revisiones de la tarea abierta. flatMapLatest: cuando cambia la tarea
    // seleccionada, cancela la consulta anterior y busca las de la nueva.
    @OptIn(ExperimentalCoroutinesApi::class)
    val evidencias: StateFlow<List<Evidencia>> = _seleccionada
        .flatMapLatest { t -> if (t == null) flowOf(emptyList()) else repository.evidenciasDe(t.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val validaciones: StateFlow<List<Validacion>> = _seleccionada
        .flatMapLatest { t -> if (t == null) flowOf(emptyList()) else repository.validacionesDe(t.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Banderas de error para el detalle: el comentario del rechazo y los campos de la entrega.
    private val _errorComentario = MutableStateFlow(false)
    val errorComentario: StateFlow<Boolean> = _errorComentario.asStateFlow()

    private val _errorEntrega = MutableStateFlow(ErrorEntrega())
    val errorEntrega: StateFlow<ErrorEntrega> = _errorEntrega.asStateFlow()

    // Se pone en true cuando una acción terminó bien (entregar, validar...): la pantalla vuelve atrás.
    private val _accionHecha = MutableStateFlow(false)
    val accionHecha: StateFlow<Boolean> = _accionHecha.asStateFlow()

    // Abre una tarea en el detalle. Primero limpio todo lo de la tarea anterior.
    fun seleccionar(id: Long) {
        _seleccionada.value = null
        _accionHecha.value = false
        _errorComentario.value = false
        _errorEntrega.value = ErrorEntrega()
        viewModelScope.launch { _seleccionada.value = repository.obtenerPorId(id) }
    }

    // El estudiante comienza SU tarea: Pendiente -> En desarrollo
    fun iniciar(tarea: Tarea, estudiante: Usuario) {
        // Solo puede iniciarla SU dueño, y solo si está Pendiente.
        if (tarea.asignadoAId != estudiante.id || tarea.estado != EstadoTarea.PENDIENTE) return

        viewModelScope.launch {
            val actualizada = tarea.copy(estado = EstadoTarea.EN_DESARROLLO)
            repository.actualizar(actualizada)
            _seleccionada.value = actualizada
            Mensajes.exito("Tarea iniciada. Cuando termines, adjunta una foto y entrégala")
        }
    }

    fun quitarErrorEntrega() { _errorEntrega.value = ErrorEntrega() }

    // El estudiante entrega SU tarea con comentario y fotos; queda pendiente de validación.
    // También reenvía una tarea rechazada después de corregirla.
    fun marcarRealizada(tarea: Tarea, estudiante: Usuario, comentario: String, fotos: List<String>) {
        // Se puede entregar desde "En desarrollo" o reenviar desde "Rechazada".
        val puedeEntregarla = tarea.estado == EstadoTarea.EN_DESARROLLO ||
                tarea.estado == EstadoTarea.RECHAZADA
        // Solo SU dueño la entrega: así un estudiante no puede tocar la tarea de otro.
        if (tarea.asignadoAId != estudiante.id || !puedeEntregarla) return

        // La entrega exige comentario Y al menos una foto (evidencia).
        val errores = ErrorEntrega(
            comentario = !validarTexto(comentario, Limites.COMENTARIO),
            foto = fotos.isEmpty()
        )
        _errorEntrega.value = errores
        if (errores.comentario || errores.foto) {
            Mensajes.error("Para entregar escribe un comentario y adjunta al menos una foto")
            return
        }

        viewModelScope.launch {
            try {
                val ahora = System.currentTimeMillis()
                // repository.entregar guarda el nuevo estado y las fotos en UNA transacción.
                repository.entregar(
                    tarea.copy(
                        estado = EstadoTarea.PENDIENTE_VALIDACION,
                        fechaRealizacion = ahora,
                        comentarioEstudiante = comentario.trim()
                    ),
                    fotos, estudiante.id
                )
                Mensajes.exito("Tarea enviada. Quedó pendiente de validación")
                _accionHecha.value = true
            } catch (e: Exception) {
                Mensajes.error("No se pudo entregar la tarea")
            }
        }
    }

    // Solo un docente puede validar; un estudiante nunca valida (ni su propia tarea)
    fun validar(tarea: Tarea, validador: Usuario, comentario: String) {
        if (!puedeRevisar(tarea, validador)) return

        viewModelScope.launch {
            try {
                val ahora = System.currentTimeMillis()
                // Dos cosas juntas en una transacción: cambio el estado de la tarea
                // Y dejo el registro en el historial de validaciones (quién, cuándo, resultado).
                repository.registrarValidacion(
                    tarea.copy(
                        estado = EstadoTarea.VALIDADA,
                        comentarioDocente = comentario.trim(),
                        validadoPorId = validador.id,
                        fechaValidacion = ahora
                    ),
                    Validacion(
                        tareaId = tarea.id, validadorId = validador.id,
                        resultado = EstadoTarea.VALIDADA,
                        comentario = comentario.trim(), fecha = ahora
                    )
                )
                Mensajes.exito("Tarea validada")
                _accionHecha.value = true
            } catch (e: Exception) {
                Mensajes.error("No se pudo validar la tarea")
            }
        }
    }

    // Rechazar exige explicar el motivo
    fun rechazar(tarea: Tarea, validador: Usuario, comentario: String) {
        if (!puedeRevisar(tarea, validador)) return
        // Regla de negocio: no se puede rechazar sin escribir el motivo.
        if (!validarObligatorio(comentario)) {
            _errorComentario.value = true
            Mensajes.error("Para rechazar debes explicar el motivo")
            return
        }
        viewModelScope.launch {
            try {
                val ahora = System.currentTimeMillis()
                repository.registrarValidacion(
                    tarea.copy(
                        estado = EstadoTarea.RECHAZADA,
                        comentarioDocente = comentario.trim(),
                        validadoPorId = validador.id,
                        fechaValidacion = ahora
                    ),
                    Validacion(
                        tareaId = tarea.id, validadorId = validador.id,
                        resultado = EstadoTarea.RECHAZADA,
                        comentario = comentario.trim(), fecha = ahora
                    )
                )
                Mensajes.exito("Tarea rechazada. El estudiante podrá corregirla y reenviarla")
                _accionHecha.value = true
            } catch (e: Exception) {
                Mensajes.error("No se pudo rechazar la tarea")
            }
        }
    }

    // Las 3 condiciones para poder validar o rechazar. Las junto en una función para usarlas
    // en validar() y en rechazar() sin repetir código.
    private fun puedeRevisar(tarea: Tarea, validador: Usuario): Boolean =
        validador.perfil == Perfil.ADMIN &&
                validador.id != tarea.asignadoAId &&      // nadie aprueba su propio trabajo
                tarea.estado == EstadoTarea.PENDIENTE_VALIDACION

    // El docente solo elimina tareas que todavía no se han iniciado
    fun eliminar(tarea: Tarea, usuario: Usuario) {
        if (usuario.perfil != Perfil.ADMIN || tarea.estado != EstadoTarea.PENDIENTE) return
        viewModelScope.launch {
            if (repository.eliminar(tarea)) {
                Mensajes.exito("Tarea eliminada")
                _accionHecha.value = true
            } else {
                Mensajes.error("No se pudo eliminar la tarea")
            }
        }
    }

    fun limpiarErrorComentario() { _errorComentario.value = false }
}
