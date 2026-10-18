package com.example.huertoaplicaciones.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertoaplicaciones.data.AppDatabase
import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.data.entity.Problema
import com.example.huertoaplicaciones.data.entity.Sector
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.data.repository.CultivoRepository
import com.example.huertoaplicaciones.data.repository.ProblemaRepository
import com.example.huertoaplicaciones.data.repository.SectorRepository
import com.example.huertoaplicaciones.data.repository.UsuarioRepository
import com.example.huertoaplicaciones.util.EstadoProblema
import com.example.huertoaplicaciones.util.Limites
import com.example.huertoaplicaciones.util.Mensajes
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.util.TipoProblema
import com.example.huertoaplicaciones.util.validarTexto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado del formulario para reportar un problema.
data class ProblemaFormUiState(
    val tipo: String = TipoProblema.PLANTA_DANADA,
    val descripcion: String = "",
    val sectorId: Long? = null,
    val cultivoId: Long? = null,
    val fotos: List<String> = emptyList(),      // rutas de las fotos adjuntas (máximo 1 en reportes)

    val errorDescripcion: Boolean = false,
    val errorSector: Boolean = false,
    val errorCultivo: Boolean = false,

    val guardado: Boolean = false
)

// ViewModel de los reportes de problemas: lista, formulario y seguimiento del docente.
class ProblemaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ProblemaRepository(db.problemaDao())
    private val sectorRepository = SectorRepository(db.sectorDao())
    private val cultivoRepository = CultivoRepository(db.cultivoDao())
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())

    // El docente ve todos los reportes
    // stateIn(...) convierte el Flow del repositorio en un StateFlow que la pantalla puede observar,
    // con una lista vacía como valor inicial mientras llegan los datos.
    val reportes: StateFlow<List<Problema>> = repository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cada estudiante ve únicamente los suyos
    fun reportesDe(usuarioId: Long): Flow<List<Problema>> = repository.obtenerPorUsuario(usuarioId)

    // Listas de apoyo para las pantallas (elegir sector y cultivo, y mostrar nombres de personas).
    val sectores: StateFlow<List<Sector>> = sectorRepository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cultivos: StateFlow<List<Cultivo>> = cultivoRepository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val usuarios: StateFlow<List<Usuario>> = usuarioRepository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Formulario de reporte ----------
    private val _form = MutableStateFlow(ProblemaFormUiState())
    val form: StateFlow<ProblemaFormUiState> = _form.asStateFlow()

    // Antes de abrir el formulario lo dejo vacío, para que no aparezcan datos de un reporte anterior.
    fun nuevoReporte() { _form.value = ProblemaFormUiState() }

    // Cada vez que el usuario escribe o elige algo, la pantalla llama a una de estas funciones
    // y yo actualizo el estado (con validación en tiempo real en la descripción).
    fun cambiarTipo(v: String) = _form.update { it.copy(tipo = v) }
    fun cambiarDescripcion(v: String) = _form.update {
        it.copy(descripcion = v, errorDescripcion = !validarTexto(v, Limites.COMENTARIO))
    }
    // Al cambiar el sector borro el cultivo elegido, porque los cultivos dependen del sector.
    fun cambiarSector(id: Long) = _form.update {
        it.copy(sectorId = id, cultivoId = null, errorSector = false)
    }
    fun cambiarCultivo(id: Long) = _form.update { it.copy(cultivoId = id, errorCultivo = false) }
    // takeLast(1): en un reporte solo se permite una foto; si agrega otra, reemplaza a la anterior.
    fun agregarFoto(uri: String) = _form.update { it.copy(fotos = (it.fotos + uri).takeLast(1)) }
    fun quitarFoto(uri: String) = _form.update { it.copy(fotos = it.fotos - uri) }

    fun guardar(reportador: Usuario) {
        val f = _form.value
        // Valido todos los campos obligatorios antes de guardar (si falta alguno, no se guarda).
        val eDescripcion = !validarTexto(f.descripcion, Limites.COMENTARIO)
        val eSector = f.sectorId == null
        val eCultivo = f.cultivoId == null

        _form.update {
            it.copy(errorDescripcion = eDescripcion, errorSector = eSector, errorCultivo = eCultivo)
        }
        if (eDescripcion || eSector || eCultivo) {
            Mensajes.error("Revisa los campos marcados en rojo")
            return
        }
        // "?: return": si por algún motivo es null, salgo. Después de esto Kotlin sabe que no es null.
        val sectorId = f.sectorId ?: return
        val cultivoId = f.cultivoId ?: return

        viewModelScope.launch {
            // Regla de negocio: el cultivo debe pertenecer al sector elegido.
            val cultivo = cultivoRepository.obtenerPorId(cultivoId)
            if (cultivo == null || cultivo.sectorId != sectorId) {
                _form.update { it.copy(errorCultivo = true) }
                Mensajes.error("El cultivo seleccionado no pertenece a ese sector")
                return@launch
            }
            // try/catch: si la base falla, aviso al usuario en vez de que la app se cierre.
            try {
                repository.guardar(
                    Problema(
                        sectorId = sectorId,
                        cultivoId = cultivoId,
                        tipo = f.tipo,
                        descripcion = f.descripcion.trim(),
                        fechaReporte = System.currentTimeMillis(),
                        reportadoPorId = reportador.id,
                        fotoUri = f.fotos.firstOrNull(),
                        // Todo reporte nuevo parte como "Informado".
                        estado = EstadoProblema.INFORMADO
                    )
                )
                Mensajes.exito("Reporte enviado. El docente lo revisará")
                _form.update { it.copy(guardado = true) }
            } catch (e: Exception) {
                Mensajes.error("No se pudo enviar el reporte")
            }
        }
    }

    // Solo el docente cambia el estado de atención. Atender o cerrar exige un comentario.
    // Devuelve false cuando falta el comentario para que la pantalla marque el error.
    fun cambiarEstadoAtencion(
        problema: Problema, docente: Usuario, nuevoEstado: String, comentario: String
    ): Boolean {
        // Control de acceso: aunque la pantalla ya oculta el botón, aquí vuelvo a verificar el perfil.
        if (docente.perfil != Perfil.ADMIN) return false
        val exigeComentario = nuevoEstado == EstadoProblema.ATENDIDO ||
                nuevoEstado == EstadoProblema.CERRADO
        if (exigeComentario && comentario.isBlank()) {
            Mensajes.error("Explica en un comentario cómo se atendió el problema")
            return false
        }
        viewModelScope.launch {
            try {
                // copy() deja igual todo el reporte y cambia solo lo indicado (estado, comentario, cierre).
                repository.actualizar(
                    problema.copy(
                        estado = nuevoEstado,
                        comentarioDocente = comentario.trim(),
                        fechaCierre = if (exigeComentario) System.currentTimeMillis() else null
                    )
                )
                Mensajes.exito("Reporte actualizado: $nuevoEstado")
            } catch (e: Exception) {
                Mensajes.error("No se pudo actualizar el reporte")
            }
        }
        return true
    }
}
