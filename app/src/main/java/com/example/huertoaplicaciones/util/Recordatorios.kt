package com.example.huertoaplicaciones.util

import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.data.entity.LecturaSensor
import com.example.huertoaplicaciones.data.entity.Problema
import com.example.huertoaplicaciones.data.entity.Sector
import com.example.huertoaplicaciones.data.entity.Tarea
import com.example.huertoaplicaciones.data.entity.Usuario

// Este archivo calcula los AVISOS Y RECORDATORIOS del panel principal.
// Son funciones "puras": reciben listas y devuelven un resultado, sin tocar la base ni la pantalla.
// Por eso son fáciles de probar con tests unitarios (LogicaHuertoTest).

// Tipos de aviso: el panel usa este tipo para elegir el ícono y el color de cada aviso.
enum class TipoAviso {
    ATRASADA, POR_VALIDAR, RECHAZADA, PROBLEMA, COSECHA, RIEGO, PROXIMA, MANTENIMIENTO, SENSOR
}

// Un aviso = su tipo (para el ícono) + el texto que se muestra al usuario.
data class Aviso(val tipo: TipoAviso, val texto: String)

// Los números del "Resumen de tareas" del panel.
data class ResumenTareas(
    val total: Int = 0,
    val pendientes: Int = 0,
    val enDesarrollo: Int = 0,
    val porValidar: Int = 0,
    val validadas: Int = 0,
    val rechazadas: Int = 0,
    val atrasadas: Int = 0
) {
    // Propiedad calculada (con "get()"): el avance es la fracción de tareas validadas, entre 0 y 1.
    // Si no hay tareas devuelvo 0 para no dividir por cero.
    val avance: Float get() = if (total == 0) 0f else validadas.toFloat() / total
}

// Cuenta cuántas tareas hay en cada estado.
// Uso estadoMostrado() y no tarea.estado para que las tareas vencidas cuenten como "Atrasadas".
fun resumirTareas(tareas: List<Tarea>, ahora: Long = System.currentTimeMillis()): ResumenTareas {
    val estados = tareas.map { estadoMostrado(it, ahora) }
    return ResumenTareas(
        total = tareas.size,
        pendientes = estados.count { it == EstadoTarea.PENDIENTE },
        enDesarrollo = estados.count { it == EstadoTarea.EN_DESARROLLO },
        porValidar = estados.count { it == EstadoTarea.PENDIENTE_VALIDACION },
        validadas = estados.count { it == EstadoTarea.VALIDADA },
        rechazadas = estados.count { it == EstadoTarea.RECHAZADA },
        atrasadas = estados.count { it == EstadoTarea.ATRASADA }
    )
}

// Con cuántos días de anticipación aviso de una cosecha o un mantenimiento.
private const val DIAS_AVISO_COSECHA = 7L

// Próximo riego = último riego realizado (o la siembra) + la frecuencia planificada.
// Cuenta el riego aunque aún esté pendiente de validación: el cultivo ya fue regado.
fun proximoRiego(cultivo: Cultivo, tareas: List<Tarea>): Long {
    val ultimoRiego = tareas
        // Me quedo solo con los riegos ya hechos de ESTE cultivo.
        .filter {
            it.cultivoId == cultivo.id && it.tipo == TipoTarea.RIEGO &&
                    (it.estado == EstadoTarea.VALIDADA ||
                            it.estado == EstadoTarea.PENDIENTE_VALIDACION)
        }
        // De esos tomo el más reciente. "?:" (operador Elvis): si no hay ninguno uso la fecha de siembra.
        .maxOfOrNull { it.fechaRealizacion ?: it.fechaProgramada }
        ?: cultivo.fechaSiembra
    return ultimoRiego + cultivo.frecuenciaRiegoDias * MILIS_POR_DIA
}

// Solo los cultivos que están creciendo necesitan avisos de riego y de cosecha.
private val ESTADOS_ACTIVOS = listOf(
    EstadoCultivo.SEMBRADO, EstadoCultivo.EN_CRECIMIENTO, EstadoCultivo.LISTO_COSECHA
)

// Avisos de cosecha y riego, compartidos por docentes y estudiantes
private fun avisosDeCultivos(
    cultivos: List<Cultivo>, tareas: List<Tarea>, ahora: Long
): List<Aviso> {
    val avisos = mutableListOf<Aviso>()
    cultivos.filter { it.estado in ESTADOS_ACTIVOS }.forEach { c ->
        // Aviso de cosecha: si falta una semana o menos (y todavía no pasó la fecha).
        val diasCosecha = (c.fechaCosecha - ahora) / MILIS_POR_DIA
        if (c.fechaCosecha >= ahora && diasCosecha <= DIAS_AVISO_COSECHA) {
            avisos += Aviso(
                TipoAviso.COSECHA,
                "Cosecha de ${c.nombre} el ${formatearFecha(c.fechaCosecha)}"
            )
        }
        // Aviso de riego: si el próximo riego es hoy, mañana o ya se pasó.
        val riego = proximoRiego(c, tareas)
        if (riego <= ahora + MILIS_POR_DIA) {
            avisos += Aviso(TipoAviso.RIEGO, "Toca regar ${c.nombre} (${formatearFecha(riego)})")
        }
    }
    return avisos
}

// Todos los avisos que ve el DOCENTE: tareas atrasadas, por validar, problemas abiertos,
// mantenimientos próximos, lecturas de sensores fuera de rango, y cosechas y riegos.
fun avisosDocente(
    tareas: List<Tarea>,
    cultivos: List<Cultivo>,
    problemas: List<Problema>,
    usuarios: List<Usuario>,
    lecturas: List<LecturaSensor> = emptyList(),
    sectores: List<Sector> = emptyList(),
    ahora: Long = System.currentTimeMillis()
): List<Aviso> {
    val avisos = mutableListOf<Aviso>()
    // 1) Un aviso por cada tarea atrasada, con el nombre del estudiante responsable.
    tareas.filter { estadoMostrado(it, ahora) == EstadoTarea.ATRASADA }.forEach { t ->
        val alumno = usuarios.firstOrNull { it.id == t.asignadoAId }?.nombre ?: "Sin asignar"
        avisos += Aviso(
            TipoAviso.ATRASADA,
            "Atrasada: ${t.tipo} de $alumno, era para el ${formatearFecha(t.fechaProgramada)}"
        )
    }
    // 2) Un solo aviso con la cantidad de tareas que esperan revisión.
    val porValidar = tareas.count { it.estado == EstadoTarea.PENDIENTE_VALIDACION }
    if (porValidar > 0) {
        avisos += Aviso(TipoAviso.POR_VALIDAR, "$porValidar tarea(s) esperan tu validación")
    }
    // 3) Problemas que todavía nadie ha atendido.
    val abiertos = problemas.count {
        it.estado == EstadoProblema.INFORMADO || it.estado == EstadoProblema.EN_REVISION
    }
    if (abiertos > 0) {
        avisos += Aviso(TipoAviso.PROBLEMA, "$abiertos problema(s) del huerto sin resolver")
    }
    // Mantenimientos programados para los próximos días
    tareas.filter {
        it.tipo == TipoTarea.MANTENIMIENTO &&
                (it.estado == EstadoTarea.PENDIENTE || it.estado == EstadoTarea.EN_DESARROLLO) &&
                it.fechaProgramada >= ahora && it.fechaProgramada - ahora <= DIAS_AVISO_COSECHA * MILIS_POR_DIA
    }.forEach { t ->
        avisos += Aviso(
            TipoAviso.MANTENIMIENTO,
            "Mantenimiento programado: ${t.descripcion} el ${formatearFecha(t.fechaProgramada)}"
        )
    }
    // Última lectura simulada de cada sector; se avisa si está fuera del rango normal
    // groupBy agrupa las lecturas por sector, y maxBy toma la más reciente de cada grupo.
    lecturas.groupBy { it.sectorId }
        .map { (_, delSector) -> delSector.maxBy { it.fecha } }
        .filter { it.estado != EstadoLectura.NORMAL }
        .forEach { l ->
            val sector = sectores.firstOrNull { it.id == l.sectorId }?.nombre ?: "Sector"
            avisos += Aviso(
                TipoAviso.SENSOR,
                "Lectura simulada fuera de rango en $sector: ${l.estado.lowercase()} (dato de prueba)"
            )
        }
    // Al final agrego los avisos de cosecha y riego.
    return avisos + avisosDeCultivos(cultivos, tareas, ahora)
}

// Avisos que ve un ESTUDIANTE: solo los de SUS tareas (atrasadas, rechazadas y próximas),
// más los riegos y cosechas de los cultivos donde él trabaja.
fun avisosEstudiante(
    tareasPropias: List<Tarea>,
    cultivos: List<Cultivo>,
    todasLasTareas: List<Tarea> = tareasPropias,
    ahora: Long = System.currentTimeMillis()
): List<Aviso> {
    val avisos = mutableListOf<Aviso>()
    tareasPropias.forEach { t ->
        // "when" es como un switch: según el estado mostrado, genero un aviso distinto.
        when (estadoMostrado(t, ahora)) {
            EstadoTarea.ATRASADA -> avisos += Aviso(
                TipoAviso.ATRASADA,
                "Atrasada: ${t.tipo}, era para el ${formatearFecha(t.fechaProgramada)}"
            )
            EstadoTarea.RECHAZADA -> avisos += Aviso(
                TipoAviso.RECHAZADA, "Corrige y reenvía: ${t.tipo}"
            )
            // Si todavía no la hace y vence en menos de 3 días, la marco como "próxima".
            EstadoTarea.PENDIENTE, EstadoTarea.EN_DESARROLLO ->
                if (t.fechaProgramada - ahora < 3 * MILIS_POR_DIA) {
                    avisos += Aviso(
                        TipoAviso.PROXIMA,
                        "Próxima: ${t.tipo} el ${formatearFecha(t.fechaProgramada)}"
                    )
                }
        }
    }
    // Solo los cultivos con tareas de este estudiante.
    val cultivosPropios = tareasPropias.map { it.cultivoId }.toSet()
    return avisos + avisosDeCultivos(cultivos.filter { it.id in cultivosPropios }, todasLasTareas, ahora)
}
