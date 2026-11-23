package com.example.huertoaplicaciones

import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.data.entity.Problema
import com.example.huertoaplicaciones.data.entity.Sector
import com.example.huertoaplicaciones.data.entity.Tarea
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.util.EstadoCultivo
import com.example.huertoaplicaciones.util.EstadoProblema
import com.example.huertoaplicaciones.util.EstadoSector
import com.example.huertoaplicaciones.util.EstadoTarea
import com.example.huertoaplicaciones.util.MILIS_POR_DIA
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.util.TipoProblema
import com.example.huertoaplicaciones.util.TipoTarea
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

// Datos ficticios que comparten todas las pruebas, para no repetirlos en cada archivo.
object DatosPrueba {

    val ahora: Long = System.currentTimeMillis()

    val docente = Usuario(
        id = 1, nombre = "Carolina Muñoz Soto", correo = "carolina.munoz@huerta.cl",
        perfil = Perfil.ADMIN, grupo = "Taller Huerto"
    )
    val estudiante = Usuario(
        id = 2, nombre = "Matías Fuentes Rojas", correo = "matias.fuentes@huerta.cl",
        perfil = Perfil.ESTUDIANTE, grupo = "Grupo A"
    )
    val otroEstudiante = Usuario(
        id = 3, nombre = "Valentina Pérez Díaz", correo = "valentina.perez@huerta.cl",
        perfil = Perfil.ESTUDIANTE, grupo = "Grupo B"
    )

    val sector = Sector(
        id = 1, codigo = "S-01", nombre = "Huerta Norte", descripcion = "Camas al aire libre",
        ubicacion = "Patio norte", estado = EstadoSector.EN_USO, responsableId = 1,
        fechaCreacion = ahora, fechaActualizacion = ahora
    )

    val cultivo = Cultivo(
        id = 1, nombre = "Lechuga", descripcion = "Lechuga costina", sectorId = 1,
        fechaSiembra = ahora - 10 * MILIS_POR_DIA, fechaCosecha = ahora + 20 * MILIS_POR_DIA,
        frecuenciaRiegoDias = 2, estado = EstadoCultivo.EN_CRECIMIENTO
    )

    // Una tarea asignada al estudiante 2, en el estado que se indique
    fun tarea(id: Long = 10, estado: String = EstadoTarea.PENDIENTE, asignadoA: Long = 2) = Tarea(
        id = id, tipo = TipoTarea.RIEGO, descripcion = "Regar la lechuga", instrucciones = "Regar sin mojar hojas",
        sectorId = 1, cultivoId = 1, fechaCreacion = ahora, fechaProgramada = ahora + MILIS_POR_DIA,
        asignadoAId = asignadoA, creadoPorId = 1, estado = estado
    )

    val problema = Problema(
        id = 5, sectorId = 1, cultivoId = 1, tipo = TipoProblema.PLAGA, descripcion = "Bichos en las hojas",
        fechaReporte = ahora, reportadoPorId = 2, estado = EstadoProblema.INFORMADO
    )

    // Respuestas HTTP simuladas (como las que entregaría Retrofit)
    fun exito(): Response<Void> = Response.success<Void>(null)
    fun <T> error(codigo: Int, json: String = ""): Response<T> = Response.error(codigo, json.toResponseBody(null))
}
