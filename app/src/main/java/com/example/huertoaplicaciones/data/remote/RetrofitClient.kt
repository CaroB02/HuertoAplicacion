package com.example.huertoaplicaciones.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// RETROFIT CLIENT: crea UNA instancia de cada Api (patrón Singleton con "object").
// Retrofit genera el código de las interfaces (UsuariosApi, HuertoApi...) y Gson convierte el JSON
// de las respuestas en objetos Kotlin (y los objetos Request en JSON al enviar).
object RetrofitClient {

    // Muestra en el Logcat cada petición y respuesta (filtrar por "okhttp").
    // Sirve para demostrar en la defensa que la app de verdad llama a los microservicios.
    private val registro = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Si un servicio no responde en estos segundos, la llamada falla y la app muestra un mensaje
    private val cliente = OkHttpClient.Builder()
        .addInterceptor(registro)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun crear(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // "by lazy" = se crea la primera vez que se usa, no al abrir la app
    val usuariosApi: UsuariosApi by lazy { crear(ApiConfig.URL_USUARIOS).create(UsuariosApi::class.java) }
    val huertoApi: HuertoApi by lazy { crear(ApiConfig.URL_HUERTO).create(HuertoApi::class.java) }
    val tareasApi: TareasApi by lazy { crear(ApiConfig.URL_TAREAS).create(TareasApi::class.java) }
    val climaApi: ClimaApi by lazy { crear(ApiConfig.URL_CLIMA).create(ClimaApi::class.java) }
}
