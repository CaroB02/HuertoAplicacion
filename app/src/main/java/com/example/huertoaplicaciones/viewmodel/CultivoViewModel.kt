package com.example.huertoaplicaciones.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertoaplicaciones.data.AppDatabase
import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.data.entity.Sector
import com.example.huertoaplicaciones.data.repository.CultivoRepository
import com.example.huertoaplicaciones.data.repository.SectorRepository
import com.example.huertoaplicaciones.util.Limites
import com.example.huertoaplicaciones.util.Mensajes
import com.example.huertoaplicaciones.util.formatearFecha
import com.example.huertoaplicaciones.util.parsearFecha
import com.example.huertoaplicaciones.util.validarFecha
import com.example.huertoaplicaciones.util.validarNumeroPositivo
import com.example.huertoaplicaciones.util.validarTexto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel de los cultivos: la lista y el formulario de crear/editar.
// Tiene las validaciones más delicadas del proyecto: las fechas (cosecha no puede ser antes de la siembra).
class CultivoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = CultivoRepository(db.cultivoDao())
    private val sectorRepository = SectorRepository(db.sectorDao())

    val cultivos: StateFlow<List<Cultivo>> = repository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // La lista de sectores la necesito para elegir a cuál pertenece el cultivo.
    val sectores: StateFlow<List<Sector>> = sectorRepository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _form = MutableStateFlow(CultivoFormUiState())
    val form: StateFlow<CultivoFormUiState> = _form.asStateFlow()

    fun nuevoCultivo() {
        _form.value = CultivoFormUiState()
    }

    // Para EDITAR: cargo el cultivo y convierto cada valor al formato que se escribe en el formulario
    // (las fechas Long pasan a texto dd/mm/aaaa y el número a String).
    fun cargarCultivo(id: Long) {
        viewModelScope.launch {
            repository.obtenerPorId(id)?.let { c ->
                _form.value = CultivoFormUiState(
                    id = c.id,
                    nombre = c.nombre,
                    descripcion = c.descripcion,
                    sectorId = c.sectorId,
                    fechaSiembra = formatearFecha(c.fechaSiembra),
                    // El trasplante puede ser null: si no existe lo dejo como texto vacío.
                    fechaTrasplante = c.fechaTrasplante?.let { formatearFecha(it) } ?: "",
                    fechaCosecha = formatearFecha(c.fechaCosecha),
                    frecuenciaRiego = c.frecuenciaRiegoDias.toString(),
                    estado = c.estado,
                    observaciones = c.observaciones,
                    fotoUri = c.fotoUri
                )
            }
        }
    }

    // Validación en tiempo real de cada campo mientras el usuario escribe.
    fun cambiarNombre(v: String) = _form.update {
        it.copy(nombre = v, errorNombre = !validarTexto(v, Limites.NOMBRE))
    }
    fun cambiarDescripcion(v: String) = _form.update {
        it.copy(descripcion = v, errorDescripcion = !validarTexto(v, Limites.DESCRIPCION))
    }
    fun cambiarSector(id: Long) = _form.update {
        it.copy(sectorId = id, errorSector = false)
    }
    fun cambiarFechaSiembra(v: String) = _form.update {
        it.copy(
            fechaSiembra = v,
            errorFechaSiembra = !validarFecha(v),
            // Al cambiar la siembra se vuelven a revisar las fechas que dependen de ella
            errorFechaCosecha = if (it.fechaCosecha.isBlank()) it.errorFechaCosecha
            else hayErrorCosecha(v, it.fechaCosecha),
            errorFechaTrasplante = hayErrorTrasplante(v, it.fechaTrasplante, it.fechaCosecha)
        )
    }
    fun cambiarFechaTrasplante(v: String) = _form.update {
        it.copy(
            fechaTrasplante = v,
            errorFechaTrasplante = hayErrorTrasplante(it.fechaSiembra, v, it.fechaCosecha)
        )
    }
    fun cambiarFechaCosecha(v: String) = _form.update {
        it.copy(fechaCosecha = v, errorFechaCosecha = hayErrorCosecha(it.fechaSiembra, v))
    }
    fun cambiarFrecuencia(v: String) = _form.update {
        it.copy(frecuenciaRiego = v, errorFrecuencia = !validarNumeroPositivo(v))
    }
    fun cambiarEstado(v: String) = _form.update { it.copy(estado = v) }
    // take(...) corta el texto al largo máximo permitido.
    fun cambiarObservaciones(v: String) = _form.update {
        it.copy(observaciones = v.take(Limites.COMENTARIO))
    }
    fun cambiarFoto(uri: String?) = _form.update { it.copy(fotoUri = uri) }

    // La cosecha debe ser una fecha válida y no anterior a la siembra
    // Devuelve true si HAY error. Cada return corta la función en el primer problema que encuentra.
    private fun hayErrorCosecha(siembra: String, cosecha: String): Boolean {
        val c = parsearFecha(cosecha) ?: return true      // cosecha ilegible = error
        val s = parsearFecha(siembra) ?: return false     // si la siembra aún no es válida, no comparo
        return c < s                                       // error si la cosecha es ANTES que la siembra
    }

    // El trasplante es opcional; si se informa debe caer entre la siembra y la cosecha
    private fun hayErrorTrasplante(siembra: String, trasplante: String, cosecha: String): Boolean {
        if (trasplante.isBlank()) return false            // vacío está permitido (es opcional)
        val t = parsearFecha(trasplante) ?: return true
        val s = parsearFecha(siembra)
        val c = parsearFecha(cosecha)
        if (s != null && t < s) return true
        if (c != null && t > c) return true
        return false
    }

    // Al presionar "Guardar": reviso todo junto, y solo si no hay ningún error escribo en la base.
    fun guardar() {
        val f = _form.value
        val eNombre = !validarTexto(f.nombre, Limites.NOMBRE)
        val eDescripcion = !validarTexto(f.descripcion, Limites.DESCRIPCION)
        val eSector = f.sectorId == null
        val eSiembra = !validarFecha(f.fechaSiembra)
        val eCosecha = hayErrorCosecha(f.fechaSiembra, f.fechaCosecha)
        val eTrasplante = hayErrorTrasplante(f.fechaSiembra, f.fechaTrasplante, f.fechaCosecha)
        val eFrecuencia = !validarNumeroPositivo(f.frecuenciaRiego)

        _form.update {
            it.copy(
                errorNombre = eNombre, errorDescripcion = eDescripcion,
                errorSector = eSector, errorFechaSiembra = eSiembra,
                errorFechaTrasplante = eTrasplante,
                errorFechaCosecha = eCosecha, errorFrecuencia = eFrecuencia
            )
        }
        if (eNombre || eDescripcion || eSector || eSiembra || eCosecha || eTrasplante || eFrecuencia) {
            Mensajes.error("Revisa los campos marcados en rojo")
            return
        }

        // Convierto los textos del formulario a sus tipos reales (Long, Int).
        // Si alguno fallara, "?: return" corta; pero ya validé arriba, así que no debería pasar.
        val sectorId = f.sectorId ?: return
        val siembra = parsearFecha(f.fechaSiembra) ?: return
        val cosecha = parsearFecha(f.fechaCosecha) ?: return
        val frecuencia = f.frecuenciaRiego.toIntOrNull() ?: return
        val trasplante = if (f.fechaTrasplante.isBlank()) null else parsearFecha(f.fechaTrasplante)

        viewModelScope.launch {
            try {
                val cultivo = Cultivo(
                    id = f.id,
                    nombre = f.nombre.trim(),
                    descripcion = f.descripcion.trim(),
                    sectorId = sectorId,
                    fechaSiembra = siembra,
                    fechaTrasplante = trasplante,
                    fechaCosecha = cosecha,
                    frecuenciaRiegoDias = frecuencia,
                    estado = f.estado,
                    observaciones = f.observaciones.trim(),
                    fotoUri = f.fotoUri
                )
                // id == 0 = cultivo nuevo (insertar); si no, edición (actualizar).
                if (f.id == 0L) repository.guardar(cultivo) else repository.actualizar(cultivo)
                Mensajes.exito(if (f.id == 0L) "Cultivo registrado" else "Cultivo actualizado")
                _form.update { it.copy(guardado = true) }
            } catch (e: Exception) {
                Mensajes.error("No se pudo guardar el cultivo")
            }
        }
    }

    // Elimina un cultivo; "alTerminar" le avisa a la pantalla si se pudo (true) o no (false).
    fun eliminarCultivo(id: Long, alTerminar: (Boolean) -> Unit) {
        viewModelScope.launch {
            val cultivo = repository.obtenerPorId(id)
            val eliminado = cultivo != null && repository.eliminar(cultivo)
            if (eliminado) Mensajes.exito("Cultivo eliminado")
            else Mensajes.error("No se puede eliminar: el cultivo tiene tareas o reportes asociados")
            alTerminar(eliminado)
        }
    }
}
