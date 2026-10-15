package com.example.huertoaplicaciones.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertoaplicaciones.data.AppDatabase
import com.example.huertoaplicaciones.data.entity.Sector
import com.example.huertoaplicaciones.data.repository.SectorRepository
import com.example.huertoaplicaciones.util.validarObligatorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SectorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository =
        SectorRepository(AppDatabase.getDatabase(application).sectorDao())

    val sectores: StateFlow<List<Sector>> = repository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _form = MutableStateFlow(SectorFormUiState())
    val form: StateFlow<SectorFormUiState> = _form.asStateFlow()

    fun nuevoSector() {
        _form.value = SectorFormUiState()
    }

    fun cargarSector(id: Long) {
        viewModelScope.launch {
            repository.obtenerPorId(id)?.let { s ->
                _form.value = SectorFormUiState(
                    id = s.id, codigo = s.codigo, nombre = s.nombre,
                    descripcion = s.descripcion, ubicacion = s.ubicacion,
                    estado = s.estado, responsableId = s.responsableId,
                    fechaCreacion = s.fechaCreacion
                )
            }
        }
    }

    fun cambiarCodigo(v: String) = _form.update {
        it.copy(codigo = v, errorCodigo = !validarObligatorio(v))
    }
    fun cambiarNombre(v: String) = _form.update {
        it.copy(nombre = v, errorNombre = !validarObligatorio(v))
    }
    fun cambiarDescripcion(v: String) = _form.update {
        it.copy(descripcion = v, errorDescripcion = !validarObligatorio(v))
    }
    fun cambiarUbicacion(v: String) = _form.update {
        it.copy(ubicacion = v, errorUbicacion = !validarObligatorio(v))
    }
    fun cambiarEstado(v: String) = _form.update { it.copy(estado = v) }

    fun guardar() {
        val f = _form.value
        val eCodigo = !validarObligatorio(f.codigo)
        val eNombre = !validarObligatorio(f.nombre)
        val eDescripcion = !validarObligatorio(f.descripcion)
        val eUbicacion = !validarObligatorio(f.ubicacion)

        _form.update {
            it.copy(
                errorCodigo = eCodigo, errorNombre = eNombre,
                errorDescripcion = eDescripcion, errorUbicacion = eUbicacion
            )
        }
        if (eCodigo || eNombre || eDescripcion || eUbicacion) return

        val ahora = System.currentTimeMillis()
        viewModelScope.launch {
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

            _form.update { it.copy(guardado = true) }
        }
    }
}