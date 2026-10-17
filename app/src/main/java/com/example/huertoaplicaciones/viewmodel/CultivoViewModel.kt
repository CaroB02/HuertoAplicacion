package com.example.huertoaplicaciones.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertoaplicaciones.data.AppDatabase
import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.data.entity.Sector
import com.example.huertoaplicaciones.data.repository.CultivoRepository
import com.example.huertoaplicaciones.data.repository.SectorRepository
import com.example.huertoaplicaciones.util.formatearFecha
import com.example.huertoaplicaciones.util.parsearFecha
import com.example.huertoaplicaciones.util.validarFecha
import com.example.huertoaplicaciones.util.validarNumeroPositivo
import com.example.huertoaplicaciones.util.validarObligatorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CultivoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = CultivoRepository(db.cultivoDao())
    private val sectorRepository = SectorRepository(db.sectorDao())

    val cultivos: StateFlow<List<Cultivo>> = repository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sectores: StateFlow<List<Sector>> = sectorRepository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _form = MutableStateFlow(CultivoFormUiState())
    val form: StateFlow<CultivoFormUiState> = _form.asStateFlow()

    fun nuevoCultivo() {
        _form.value = CultivoFormUiState()
    }

    fun cargarCultivo(id: Long) {
        viewModelScope.launch {
            repository.obtenerPorId(id)?.let { c ->
                _form.value = CultivoFormUiState(
                    id = c.id,
                    nombre = c.nombre,
                    descripcion = c.descripcion,
                    sectorId = c.sectorId,
                    fechaSiembra = formatearFecha(c.fechaSiembra),
                    fechaCosecha = formatearFecha(c.fechaCosecha),
                    frecuenciaRiego = c.frecuenciaRiegoDias.toString(),
                    estado = c.estado,
                    observaciones = c.observaciones,
                    fechaTrasplante = c.fechaTrasplante,
                    fotoUri = c.fotoUri
                )
            }
        }
    }

    fun cambiarNombre(v: String) = _form.update {
        it.copy(nombre = v, errorNombre = !validarObligatorio(v))
    }
    fun cambiarDescripcion(v: String) = _form.update {
        it.copy(descripcion = v, errorDescripcion = !validarObligatorio(v))
    }
    fun cambiarSector(id: Long) = _form.update {
        it.copy(sectorId = id, errorSector = false)
    }
    fun cambiarFechaSiembra(v: String) = _form.update {
        it.copy(fechaSiembra = v, errorFechaSiembra = !validarFecha(v))
    }
    fun cambiarFechaCosecha(v: String) = _form.update {
        it.copy(fechaCosecha = v, errorFechaCosecha = hayErrorCosecha(it.fechaSiembra, v))
    }
    fun cambiarFrecuencia(v: String) = _form.update {
        it.copy(frecuenciaRiego = v, errorFrecuencia = !validarNumeroPositivo(v))
    }
    fun cambiarEstado(v: String) = _form.update { it.copy(estado = v) }
    fun cambiarObservaciones(v: String) = _form.update { it.copy(observaciones = v) }

    // La cosecha debe ser una fecha válida y no anterior a la siembra
    private fun hayErrorCosecha(siembra: String, cosecha: String): Boolean {
        val c = parsearFecha(cosecha) ?: return true
        val s = parsearFecha(siembra) ?: return false
        return c < s
    }

    fun guardar() {
        val f = _form.value
        val eNombre = !validarObligatorio(f.nombre)
        val eDescripcion = !validarObligatorio(f.descripcion)
        val eSector = f.sectorId == null
        val eSiembra = !validarFecha(f.fechaSiembra)
        val eCosecha = hayErrorCosecha(f.fechaSiembra, f.fechaCosecha)
        val eFrecuencia = !validarNumeroPositivo(f.frecuenciaRiego)

        _form.update {
            it.copy(
                errorNombre = eNombre, errorDescripcion = eDescripcion,
                errorSector = eSector, errorFechaSiembra = eSiembra,
                errorFechaCosecha = eCosecha, errorFrecuencia = eFrecuencia
            )
        }
        if (eNombre || eDescripcion || eSector || eSiembra || eCosecha || eFrecuencia) return

        val sectorId = f.sectorId ?: return
        val siembra = parsearFecha(f.fechaSiembra) ?: return
        val cosecha = parsearFecha(f.fechaCosecha) ?: return
        val frecuencia = f.frecuenciaRiego.toIntOrNull() ?: return

        viewModelScope.launch {
            val cultivo = Cultivo(
                id = f.id,
                nombre = f.nombre.trim(),
                descripcion = f.descripcion.trim(),
                sectorId = sectorId,
                fechaSiembra = siembra,
                fechaTrasplante = f.fechaTrasplante,
                fechaCosecha = cosecha,
                frecuenciaRiegoDias = frecuencia,
                estado = f.estado,
                observaciones = f.observaciones.trim(),
                fotoUri = f.fotoUri
            )
            if (f.id == 0L) repository.guardar(cultivo) else repository.actualizar(cultivo)
            _form.update { it.copy(guardado = true) }
        }
    }
}