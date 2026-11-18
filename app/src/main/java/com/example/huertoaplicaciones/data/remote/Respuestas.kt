package com.example.huertoaplicaciones.data.remote

import com.google.gson.JsonParser
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

// Funciones de apoyo para revisar las respuestas HTTP. Las usan todos los repositorios.

// ¿Por qué revisar isSuccessful? Porque con Response<...> Retrofit entrega la respuesta
// aunque el servidor haya respondido 400, 404, 409 o 500. Si no fue exitosa, lanzo HttpException
// para que el ViewModel la atrape en su try/catch y muestre un mensaje.
fun comprobarRespuesta(respuesta: Response<*>) {
    if (!respuesta.isSuccessful) {
        throw HttpException(respuesta)
    }
}

// Para las búsquedas por id: 404 significa "no existe" (devuelvo null), no un error.
fun <T> cuerpoONulo(respuesta: Response<T>): T? {
    if (respuesta.code() == 404) return null
    comprobarRespuesta(respuesta)
    return respuesta.body()
}

// Convierte una excepción en un mensaje entendible para el usuario.
fun describirError(e: Throwable): String = when (e) {
    // El servidor respondió con un código de error: uso su mensaje {"mensaje": "..."} si lo trae
    is HttpException -> mensajeDelServidor(e) ?: when (e.code()) {
        400 -> "El servidor rechazó los datos enviados"
        401 -> "No autorizado"
        404 -> "No se encontró el registro"
        409 -> "La operación entra en conflicto con otros datos"
        else -> "Error del servidor (${e.code()})"
    }
    // No hubo respuesta: el microservicio está apagado, la IP es incorrecta o no hay red
    is IOException -> "No se pudo conectar con el servidor. Revisa que los microservicios estén encendidos"
    else -> e.message ?: "Error inesperado"
}

private fun mensajeDelServidor(e: HttpException): String? = try {
    val texto = e.response()?.errorBody()?.string()
    if (texto.isNullOrBlank()) null
    else JsonParser.parseString(texto).asJsonObject.get("mensaje")?.asString
} catch (ex: Exception) {
    null
}
