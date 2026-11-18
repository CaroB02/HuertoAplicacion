package com.example.huertoaplicaciones.data.remote

import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.data.entity.Problema
import com.example.huertoaplicaciones.data.entity.Sector
import com.example.huertoaplicaciones.data.entity.Tarea
import com.example.huertoaplicaciones.data.entity.Usuario

// MODELOS DE ESCRITURA (Request), igual que ClienteRequest en clase.
// Separamos lo que se LEE del servidor (Sector, Tarea...) de lo que se ENVÍA (SectorRequest...):
// al crear o modificar no se manda el id (lo asigna la base de datos o va en la URL con @Path).

data class LoginRequest(
    val correo: String,
    val clave: String
)

data class UsuarioRequest(
    val nombre: String,
    val correo: String,
    val clave: String,
    val perfil: String,
    val grupo: String
)

data class SectorRequest(
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val ubicacion: String,
    val estado: String,
    val responsableId: Long?,
    val fechaCreacion: Long,
    val fechaActualizacion: Long
)

data class CultivoRequest(
    val nombre: String,
    val descripcion: String,
    val sectorId: Long,
    val fechaSiembra: Long,
    val fechaTrasplante: Long?,
    val fechaCosecha: Long,
    val frecuenciaRiegoDias: Int,
    val estado: String,
    val observaciones: String,
    val fotoUri: String?
)

data class TareaRequest(
    val tipo: String,
    val descripcion: String,
    val instrucciones: String,
    val sectorId: Long,
    val cultivoId: Long,
    val fechaCreacion: Long,
    val fechaProgramada: Long,
    val asignadoAId: Long,
    val creadoPorId: Long,
    val estado: String,
    val fechaRealizacion: Long?,
    val comentarioEstudiante: String,
    val comentarioDocente: String,
    val validadoPorId: Long?,
    val fechaValidacion: Long?
)

// Entrega del estudiante: comentario + rutas de sus fotos
data class EntregaRequest(
    val estudianteId: Long,
    val comentario: String,
    val fotos: List<String>,
    val fechaRealizacion: Long
)

// Revisión del docente: resultado "Validada" o "Rechazada" y su comentario
data class ValidacionRequest(
    val validadorId: Long,
    val resultado: String,
    val comentario: String,
    val fecha: Long
)

data class ProblemaRequest(
    val sectorId: Long,
    val cultivoId: Long,
    val tipo: String,
    val descripcion: String,
    val fechaReporte: Long,
    val reportadoPorId: Long,
    val fotoUri: String?,
    val estado: String,
    val comentarioDocente: String,
    val fechaCierre: Long?
)

// ---------- Conversión de modelo de lectura a modelo de escritura ----------
// Funciones de extensión: se usan como sector.aRequest() dentro de los repositorios.

fun Usuario.aRequest() = UsuarioRequest(
    nombre = nombre, correo = correo, clave = clave, perfil = perfil, grupo = grupo
)

fun Sector.aRequest() = SectorRequest(
    codigo = codigo, nombre = nombre, descripcion = descripcion, ubicacion = ubicacion,
    estado = estado, responsableId = responsableId,
    fechaCreacion = fechaCreacion, fechaActualizacion = fechaActualizacion
)

fun Cultivo.aRequest() = CultivoRequest(
    nombre = nombre, descripcion = descripcion, sectorId = sectorId,
    fechaSiembra = fechaSiembra, fechaTrasplante = fechaTrasplante, fechaCosecha = fechaCosecha,
    frecuenciaRiegoDias = frecuenciaRiegoDias, estado = estado,
    observaciones = observaciones, fotoUri = fotoUri
)

fun Tarea.aRequest() = TareaRequest(
    tipo = tipo, descripcion = descripcion, instrucciones = instrucciones,
    sectorId = sectorId, cultivoId = cultivoId,
    fechaCreacion = fechaCreacion, fechaProgramada = fechaProgramada,
    asignadoAId = asignadoAId, creadoPorId = creadoPorId, estado = estado,
    fechaRealizacion = fechaRealizacion, comentarioEstudiante = comentarioEstudiante,
    comentarioDocente = comentarioDocente, validadoPorId = validadoPorId,
    fechaValidacion = fechaValidacion
)

fun Problema.aRequest() = ProblemaRequest(
    sectorId = sectorId, cultivoId = cultivoId, tipo = tipo, descripcion = descripcion,
    fechaReporte = fechaReporte, reportadoPorId = reportadoPorId, fotoUri = fotoUri,
    estado = estado, comentarioDocente = comentarioDocente, fechaCierre = fechaCierre
)
