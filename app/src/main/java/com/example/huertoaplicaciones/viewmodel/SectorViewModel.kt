package com.example.huertoaplicaciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.data.entity.LecturaSensor
import com.example.huertoaplicaciones.data.entity.Sector
import com.example.huertoaplicaciones.data.repository.CultivoRepository
import com.example.huertoaplicaciones.data.repository.LecturaSensorRepository
import com.example.huertoaplicaciones.data.repository.Repositorios
import com.example.huertoaplicaciones.data.repository.SectorRepository
import com.example.huertoaplicaciones.data.remote.describirError
import com.example.huertoaplicaciones.util.Limites
import com.example.huertoaplicaciones.util.Mensajes
import com.example.huertoaplicaciones.util.validarTexto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

// ViewModel de los sectores. Maneja tres cosas: la lista, el detalle de un sector y el formulario.
// Los repositorios llegan por el constructor con un valor por defecto (igual que en clase):
// la app usa los repositorios reales (Retrofit) y las pruebas unitarias le pasan unos simulados (MockK).
class SectorViewModel(
    private val repository: SectorRepository = Repositorios.sectores,
    private val cultivoRepository: CultivoRepository = Repositorios.cultivos,
    private val lecturaRepository: LecturaSensorRepository = Repositorios.lecturas
) : ViewModel() {

    // La lista de sectores. stateIn convierte el Flow del repositorio en un StateFlow:
    // cada vez que llega una lista nueva del servidor, la pantalla se actualiza sola.
    val sectores: StateFlow<List<Sector>> = repository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Detalle de un sector ----------
    // Guardo SOLO el id del sector abierto, y de él derivo todo lo demás.
    private val _sectorSeleccionadoId = MutableStateFlow<Long?>(null)

    // El sector abierto: lo busco en la lista cada vez que cambia el id o la lista.
    val sectorDetalle: StateFlow<Sector?> = combine(_sectorSeleccionadoId, sectores) { id, lista ->
        lista.firstOrNull { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Los cultivos del sector abierto. flatMapLatest: cada vez que cambia el id, cancela la
    // consulta anterior y empieza una nueva con el sector nuevo.
    @OptIn(ExperimentalCoroutinesApi::class)
    val cultivosDelSector: StateFlow<List<Cultivo>> = _sectorSeleccionadoId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else cultivoRepository.obtenerPorSector(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Última lectura simulada de sensores del sector seleccionado
    // (las lecturas vienen de la más nueva a la más antigua, por eso firstOrNull es la última).
    val lecturaDelSector: StateFlow<LecturaSensor?> =
        combine(_sectorSeleccionadoId, lecturaRepository.obtenerTodas()) { id, lecturas ->
            lecturas.firstOrNull { it.sectorId == id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun seleccionarSector(id: Long) { _sectorSeleccionadoId.value = id }

    // Pide al servidor los datos actualizados (GET /sectores, /cultivos y /lecturas).
    // AppNavigation la llama con LifecycleEventEffect(ON_RESUME) cada vez que se vuelve a la pantalla.
    fun recargar() {
        viewModelScope.launch {
            try {
                repository.recargar()
                cultivoRepository.recargar()
                lecturaRepository.recargar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Mensajes.error(describirError(e))
            }
        }
    }

    // Elimina un sector. "alTerminar" es una función que me pasa la pantalla para avisarle
    // si salió bien (true) o no (false), y así decidir si vuelve atrás.
    fun eliminarSector(sector: Sector, alTerminar: (Boolean) -> Unit) {
        viewModelScope.launch {
            val eliminado = try {
                repository.eliminar(sector)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Mensajes.error("No se pudo eliminar el sector: ${describirError(e)}")
                alTerminar(false)
                return@launch
            }
            if (eliminado) Mensajes.exito("Sector eliminado")
            else Mensajes.error(
                "No se puede eliminar: el sector tiene cultivos, tareas o reportes asociados"
            )
            alTerminar(eliminado)
        }
    }

    // ---------- Formulario ----------
    // _form (privado, mutable) y form (público, solo lectura): la pantalla solo observa, nunca escribe.
    private val _form = MutableStateFlow(SectorFormUiState())
    val form: StateFlow<SectorFormUiState> = _form.asStateFlow()

    // Antes de abrir el formulario para un sector nuevo, lo dejo vacío.
    fun nuevoSector() {
        _form.value = SectorFormUiState()
    }

    // Para EDITAR: pido el sector al servidor (GET /sectores/{id}) y relleno el formulario con sus datos.
    // Mientras llega, cargando = true y la pantalla muestra un indicador de progreso.
    fun cargarSector(id: Long) {
        viewModelScope.launch {
            _form.update { it.copy(cargando = true) }
            try {
                val s = repository.obtenerPorId(id)
                if (s == null) {
                    Mensajes.error("El sector ya no existe")
                    _form.update { it.copy(cargando = false) }
                } else {
                    _form.value = SectorFormUiState(
                        id = s.id, codigo = s.codigo, nombre = s.nombre,
                        descripcion = s.descripcion, ubicacion = s.ubicacion,
                        estado = s.estado, responsableId = s.responsableId,
                        fechaCreacion = s.fechaCreacion
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _form.update { it.copy(cargando = false) }
                Mensajes.error("No se pudo cargar el sector: ${describirError(e)}")
            }
        }
    }

    // Validación EN TIEMPO REAL: cada vez que el usuario escribe una letra se actualiza el texto
    // y se recalcula si el campo tiene error (validarTexto: obligatorio y con largo máximo).
    fun cambiarCodigo(v: String) = _form.update {
        it.copy(codigo = v, errorCodigo = !validarTexto(v, Limites.CODIGO))
    }
    fun cambiarNombre(v: String) = _form.update {
        it.copy(nombre = v, errorNombre = !validarTexto(v, Limites.NOMBRE))
    }
    fun cambiarDescripcion(v: String) = _form.update {
        it.copy(descripcion = v, errorDescripcion = !validarTexto(v, Limites.DESCRIPCION))
    }
    fun cambiarUbicacion(v: String) = _form.update {
        it.copy(ubicacion = v, errorUbicacion = !validarTexto(v, Limites.NOMBRE))
    }
    fun cambiarEstado(v: String) = _form.update { it.copy(estado = v) }

    // Al presionar "Guardar": reviso TODOS los campos de una vez y recién ahí decido si guardo.
    fun guardar() {
        val f = _form.value
        // Si ya está guardando (doble toque en el botón) no envío la petición dos veces
        if (f.guardando || f.cargando || f.guardado) return
        val eCodigo = !validarTexto(f.codigo, Limites.CODIGO)
        val eNombre = !validarTexto(f.nombre, Limites.NOMBRE)
        val eDescripcion = !validarTexto(f.descripcion, Limites.DESCRIPCION)
        val eUbicacion = !validarTexto(f.ubicacion, Limites.NOMBRE)

        // Marco en rojo todos los campos con error (incluso los que el usuario nunca tocó).
        _form.update {
            it.copy(
                errorCodigo = eCodigo, errorNombre = eNombre,
                errorDescripcion = eDescripcion, errorUbicacion = eUbicacion
            )
        }
        // Si hay algún error, aviso y NO guardo: así es imposible guardar datos inválidos.
        if (eCodigo || eNombre || eDescripcion || eUbicacion) {
            Mensajes.error("Revisa los campos marcados en rojo")
            return
        }

        val ahora = System.currentTimeMillis()
        viewModelScope.launch {
            _form.update { it.copy(guardando = true) }
            try {
                // id == 0 significa sector nuevo (POST); si no, es una edición (PUT).
                if (f.id == 0L) {
                    repository.guardar(
                        Sector(
                            codigo = f.codigo.trim(), nombre = f.nombre.trim(),
                            descripcion = f.descripcion.trim(), ubicacion = f.ubicacion.trim(),
                            estado = f.estado, responsableId = f.responsableId,
                            fechaCreacion = ahora, fechaActualizacion = ahora
                        )
                    )
                } else {
                    repository.actualizar(
                        Sector(
                            id = f.id, codigo = f.codigo.trim(), nombre = f.nombre.trim(),
                            descripcion = f.descripcion.trim(), ubicacion = f.ubicacion.trim(),
                            estado = f.estado, responsableId = f.responsableId,
                            fechaCreacion = f.fechaCreacion, fechaActualizacion = ahora
                        )
                    )
                }
                Mensajes.exito(if (f.id == 0L) "Sector creado" else "Sector actualizado")
                // guardado = true: la pantalla lo detecta y vuelve a la lista.
                _form.update { it.copy(guardado = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Mensajes.error("No se pudo guardar el sector: ${describirError(e)}")
            } finally {
                _form.update { it.copy(guardando = false) }
            }
        }
    }
}
