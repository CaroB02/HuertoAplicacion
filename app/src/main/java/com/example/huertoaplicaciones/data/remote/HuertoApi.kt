package com.example.huertoaplicaciones.data.remote

import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.data.entity.LecturaSensor
import com.example.huertoaplicaciones.data.entity.Sector
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Endpoints del microservicio ms-huerto (http://10.0.2.2:8082/): sectores, cultivos y sensores.
//  @GET    = leer          @POST = crear (los datos van en el @Body como JSON)
//  @PUT    = modificar     @DELETE = eliminar
//  @Path   = reemplaza {id} en la ruta por el valor del parámetro
interface HuertoApi {

    // ---------- Sectores ----------
    @GET("sectores")
    suspend fun obtenerSectores(): List<Sector>

    @GET("sectores/{id}")
    suspend fun obtenerSector(@Path("id") id: Long): Response<Sector>

    @POST("sectores")
    suspend fun crearSector(@Body sector: SectorRequest): Response<Void>

    @PUT("sectores/{id}")
    suspend fun actualizarSector(@Path("id") id: Long, @Body sector: SectorRequest): Response<Void>

    // 409 Conflict si el sector tiene cultivos, tareas o reportes
    @DELETE("sectores/{id}")
    suspend fun eliminarSector(@Path("id") id: Long): Response<Void>

    // ---------- Cultivos ----------
    @GET("cultivos")
    suspend fun obtenerCultivos(): List<Cultivo>

    @GET("cultivos/{id}")
    suspend fun obtenerCultivo(@Path("id") id: Long): Response<Cultivo>

    @POST("cultivos")
    suspend fun crearCultivo(@Body cultivo: CultivoRequest): Response<Void>

    @PUT("cultivos/{id}")
    suspend fun actualizarCultivo(@Path("id") id: Long, @Body cultivo: CultivoRequest): Response<Void>

    @DELETE("cultivos/{id}")
    suspend fun eliminarCultivo(@Path("id") id: Long): Response<Void>

    // ---------- Lecturas simuladas de sensores ----------
    @GET("lecturas")
    suspend fun obtenerLecturas(): List<LecturaSensor>
}
