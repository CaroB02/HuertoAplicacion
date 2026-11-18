package com.example.huertoaplicaciones.data.entity

// Una LECTURA SENSOR es una medición de humedad y temperatura de un sector.
// OJO: son datos SIMULADOS, no vienen de un sensor real. Por eso "origen" dice "simulación académica"
// y en la pantalla se muestran con un estilo distinto. Viene de ms-huerto (GET /lecturas).
data class LecturaSensor(
    val id: Long = 0,
    val sectorId: Long = 0,
    val fecha: Long = 0,
    val humedadSuelo: Int = 0,          // porcentaje
    val temperatura: Double = 0.0,      // grados Celsius
    val humedadAmbiente: Int = 0,       // porcentaje
    val origen: String = "simulación académica",
    val estado: String = ""             // Normal, Bajo, Alto o Pendiente de revisión
)
