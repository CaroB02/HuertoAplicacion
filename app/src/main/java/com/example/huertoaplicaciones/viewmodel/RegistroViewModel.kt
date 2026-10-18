package com.example.huertoaplicaciones.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertoaplicaciones.data.AppDatabase
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.data.repository.UsuarioRepository
import com.example.huertoaplicaciones.util.CODIGO_DOCENTE
import com.example.huertoaplicaciones.util.Limites
import com.example.huertoaplicaciones.util.Mensajes
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.util.validarClave
import com.example.huertoaplicaciones.util.validarCorreo
import com.example.huertoaplicaciones.util.validarNombreCompleto
import com.example.huertoaplicaciones.util.validarTexto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado del formulario "Crear cuenta": lo que escribió la persona y qué campos tienen error.
data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val repetirClave: String = "",
    val perfil: String = Perfil.ESTUDIANTE,
    val grupo: String = "",
    val codigoDocente: String = "",

    val errorNombre: Boolean = false,
    val errorCorreo: Boolean = false,
    val correoRepetido: Boolean = false,    // el correo es válido pero ya existe una cuenta con él
    val errorClave: Boolean = false,
    val errorRepetir: Boolean = false,
    val errorGrupo: Boolean = false,
    val errorCodigo: Boolean = false,

    val guardando: Boolean = false,
    val registrado: Boolean = false         // pasa a true cuando la cuenta se creó: la pantalla vuelve al login
)

// ViewModel de "Crear cuenta". Valida el formulario, comprueba las reglas de seguridad y crea el usuario.
class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UsuarioRepository(AppDatabase.getDatabase(application).usuarioDao())

    private val _estado = MutableStateFlow(RegistroUiState())
    val estado: StateFlow<RegistroUiState> = _estado.asStateFlow()

    // Antes de abrir la pantalla la dejo en blanco, para que no queden datos de un registro anterior.
    fun nuevo() { _estado.value = RegistroUiState() }

    // Validación en tiempo real, igual que en los otros formularios.
    fun cambiarNombre(v: String) = _estado.update {
        it.copy(nombre = v, errorNombre = !validarNombreCompleto(v))
    }
    fun cambiarCorreo(v: String) = _estado.update {
        it.copy(correo = v, errorCorreo = !validarCorreo(v.trim()), correoRepetido = false)
    }
    fun cambiarClave(v: String) = _estado.update {
        it.copy(
            clave = v,
            errorClave = !validarClave(v),
            // Si ya escribió la repetición, vuelvo a comprobar que coincidan.
            errorRepetir = it.repetirClave.isNotEmpty() && it.repetirClave != v
        )
    }
    fun cambiarRepetirClave(v: String) = _estado.update {
        it.copy(repetirClave = v, errorRepetir = v != it.clave)
    }
    fun cambiarPerfil(v: String) = _estado.update {
        it.copy(perfil = v, errorCodigo = false, errorGrupo = false)
    }
    fun cambiarGrupo(v: String) = _estado.update {
        it.copy(grupo = v, errorGrupo = v.length > Limites.NOMBRE)
    }
    fun cambiarCodigo(v: String) = _estado.update { it.copy(codigoDocente = v, errorCodigo = false) }

    fun registrar() {
        val f = _estado.value
        if (f.guardando) return

        val esDocente = f.perfil == Perfil.ADMIN
        val eNombre = !validarNombreCompleto(f.nombre)
        val eCorreo = !validarCorreo(f.correo.trim())
        val eClave = !validarClave(f.clave)
        val eRepetir = f.repetirClave != f.clave || f.repetirClave.isEmpty()
        // El estudiante debe indicar su grupo; para el docente el grupo es opcional.
        val eGrupo = if (esDocente) false else !validarTexto(f.grupo, Limites.NOMBRE)
        // REGLA DE SEGURIDAD: solo se crea una cuenta de docente con el código de autorización correcto.
        val eCodigo = esDocente && f.codigoDocente.trim() != CODIGO_DOCENTE

        _estado.update {
            it.copy(
                errorNombre = eNombre, errorCorreo = eCorreo, errorClave = eClave,
                errorRepetir = eRepetir, errorGrupo = eGrupo, errorCodigo = eCodigo
            )
        }
        if (eNombre || eCorreo || eClave || eRepetir || eGrupo || eCodigo) {
            Mensajes.error(
                if (eCodigo) "El código de autorización del docente no es correcto"
                else "Revisa los campos marcados en rojo"
            )
            return
        }

        // Los correos se guardan siempre en minúscula (el login también lo convierte).
        val correo = f.correo.trim().lowercase()
        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            // Si ya existe una cuenta con ese correo, aviso y no intento guardar.
            if (repository.obtenerPorCorreo(correo) != null) {
                _estado.update { it.copy(guardando = false, correoRepetido = true) }
                Mensajes.error("Ya existe una cuenta con ese correo")
                return@launch
            }
            try {
                repository.registrar(
                    Usuario(
                        nombre = f.nombre.trim().split(" ").filter { it.isNotBlank() }.joinToString(" "),
                        correo = correo,
                        clave = f.clave,
                        perfil = f.perfil,
                        grupo = f.grupo.trim().ifEmpty { if (esDocente) "Taller Huerto" else "" },
                        activo = true
                    )
                )
                Mensajes.exito("Cuenta creada. Ya puedes iniciar sesión")
                _estado.update { it.copy(guardando = false, registrado = true) }
            } catch (e: Exception) {
                _estado.update { it.copy(guardando = false) }
                Mensajes.error("No se pudo crear la cuenta")
            }
        }
    }
}
