package com.example.huertoaplicaciones.data.remote

import com.example.huertoaplicaciones.data.entity.Evidencia
import com.example.huertoaplicaciones.data.entity.Problema
import com.example.huertoaplicaciones.data.entity.Tarea
import com.example.huertoaplicaciones.data.entity.Validacion
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Endpoints del microservicio ms-tareas (http://10.0.2.2:8083/):
// tareas, evidencias (fotos), validaciones del docente y reportes de problemas.
interface TareasApi {

    // ---------- Tareas ----------
    @GET("tareas")
    suspend fun obtenerTareas(): List<Tarea>

    @GET("tareas/{id}")
    suspend fun obtenerTarea(@Path("id") id: Long): Response<Tarea>

    @POST("tareas")
    suspend fun crearTarea(@Body tarea: TareaRequest): Response<Void>

    @PUT("tareas/{id}")
    suspend fun actualizarTarea(@Path("id") id: Long, @Body tarea: TareaRequest): Response<Void>

    // 409 si la tarea ya se inició
    @DELETE("tareas/{id}")
    suspend fun eliminarTarea(@Path("id") id: Long): Response<Void>

    // El estudiante entrega con comentario y fotos (el servidor lo guarda en una transacción)
    @POST("tareas/{id}/entrega")
    suspend fun entregarTarea(@Path("id") id: Long, @Body entrega: EntregaRequest): Response<Void>

    // El docente valida o rechaza
    @POST("tareas/{id}/validaciones")
    suspend fun validarTarea(@Path("id") id: Long, @Body validacion: ValidacionRequest): Response<Void>

    @GET("tareas/{id}/evidencias")
    suspend fun obtenerEvidencias(@Path("id") tareaId: Long): List<Evidencia>

    @GET("tareas/{id}/validaciones")
    suspend fun obtenerValidacionesDeTarea(@Path("id") tareaId: Long): List<Validacion>

    @GET("validaciones")
    suspend fun obtenerValidaciones(): List<Validacion>

    // ---------- Reportes de problemas ----------
    @GET("problemas")
    suspend fun obtenerProblemas(): List<Problema>

    @POST("problemas")
    suspend fun crearProblema(@Body problema: ProblemaRequest): Response<Void>

    @PUT("problemas/{id}")
    suspend fun actualizarProblema(@Path("id") id: Long, @Body problema: ProblemaRequest): Response<Void>
}
