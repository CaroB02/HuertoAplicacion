package com.example.huertoaplicaciones.data.remote

// Direcciones de los servicios que consume la app. Están todas aquí para cambiarlas en un solo lugar.
//
// 10.0.2.2 es la forma en que el EMULADOR llega al "localhost" del PC donde corren los microservicios.
// Si se prueba en un TELÉFONO FÍSICO, hay que poner la IP del PC en la red Wi-Fi (por ejemplo
// "http://192.168.1.10") y agregar esa misma IP en res/xml/network_security_config.xml.
object ApiConfig {
    private const val HOST = "http://10.0.2.2"

    // Microservicios propios (Spring Boot), cada uno en su puerto
    const val URL_USUARIOS = "$HOST:8081/"
    const val URL_HUERTO = "$HOST:8082/"
    const val URL_TAREAS = "$HOST:8083/"

    // API externa: pronóstico del clima de Open-Meteo (gratuita, no necesita clave)
    const val URL_CLIMA = "https://api.open-meteo.com/"

    // Ubicación del huerto escolar para pedir el clima (Santiago centro por defecto)
    const val LATITUD = -33.45
    const val LONGITUD = -70.66
}
