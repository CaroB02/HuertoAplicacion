package com.example.huertoaplicaciones

import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.data.entity.LecturaSensor
import com.example.huertoaplicaciones.data.entity.Tarea
import com.example.huertoaplicaciones.util.EstadoCultivo
import com.example.huertoaplicaciones.util.EstadoLectura
import com.example.huertoaplicaciones.util.EstadoTarea
import com.example.huertoaplicaciones.util.MILIS_POR_DIA
import com.example.huertoaplicaciones.util.TipoAviso
import com.example.huertoaplicaciones.util.TipoTarea
import com.example.huertoaplicaciones.util.avisosDocente
import com.example.huertoaplicaciones.util.estadoMostrado
import com.example.huertoaplicaciones.util.proximoRiego
import com.example.huertoaplicaciones.util.resumirTareas
import com.example.huertoaplicaciones.util.formatearFecha
import com.example.huertoaplicaciones.util.parsearFecha
import com.example.huertoaplicaciones.util.validarFechaProgramada
import com.example.huertoaplicaciones.util.validarClave
import com.example.huertoaplicaciones.util.validarCorreo
import com.example.huertoaplicaciones.util.validarNombreCompleto
import com.example.huertoaplicaciones.util.validarNumeroPositivo
import com.example.huertoaplicaciones.util.validarTexto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// PRUEBAS UNITARIAS de la lógica pura (no necesitan emulador ni base de datos).
// Cada @Test es una función que prepara datos, llama a la función real y comprueba el resultado con assert.
// Se ejecutan con: ./gradlew testDebugUnitTest
class LogicaHuertoTest {

    // Una fecha fija de referencia, para que los resultados de los tests no dependan del día en que se corren.
    private val ahora = 1_800_000_000_000L

    // Fabrica una tarea de prueba con solo los datos que cambian en cada test; el resto son valores de relleno.
    private fun tarea(estado: String, programada: Long, tipo: String = TipoTarea.RIEGO) = Tarea(
        id = 1, tipo = tipo, descripcion = "d", instrucciones = "i", sectorId = 1, cultivoId = 1,
        fechaCreacion = ahora, fechaProgramada = programada, asignadoAId = 2, creadoPorId = 1,
        estado = estado
    )

    // Un cultivo de prueba: se sembró hace 10 días y se riega cada 3.
    private val cultivo = Cultivo(
        id = 1, nombre = "Lechuga", descripcion = "d", sectorId = 1,
        fechaSiembra = ahora - 10 * MILIS_POR_DIA, fechaCosecha = ahora + 20 * MILIS_POR_DIA,
        frecuenciaRiegoDias = 3, estado = EstadoCultivo.EN_CRECIMIENTO
    )

    // Una tarea pendiente cuya fecha ya pasó debe mostrarse como "Atrasada".
    @Test fun tareaPendienteVencidaSeMuestraAtrasada() {
        val t = tarea(EstadoTarea.PENDIENTE, ahora - 2 * MILIS_POR_DIA)
        assertEquals(EstadoTarea.ATRASADA, estadoMostrado(t, ahora))
    }

    // Una tarea ya validada no puede aparecer atrasada, aunque su fecha sea antigua.
    @Test fun tareaValidadaNuncaSeMuestraAtrasada() {
        val t = tarea(EstadoTarea.VALIDADA, ahora - 5 * MILIS_POR_DIA)
        assertEquals(EstadoTarea.VALIDADA, estadoMostrado(t, ahora))
    }

    // El resumen del panel cuenta bien cuántas tareas hay en cada estado y calcula el avance (1 de 4 = 25%).
    @Test fun resumenContabilizaPorEstado() {
        val tareas = listOf(
            tarea(EstadoTarea.PENDIENTE, ahora + MILIS_POR_DIA),
            tarea(EstadoTarea.PENDIENTE, ahora - 3 * MILIS_POR_DIA),
            tarea(EstadoTarea.VALIDADA, ahora - MILIS_POR_DIA),
            tarea(EstadoTarea.PENDIENTE_VALIDACION, ahora)
        )
        val r = resumirTareas(tareas, ahora)
        assertEquals(4, r.total)
        assertEquals(1, r.pendientes)
        assertEquals(1, r.atrasadas)
        assertEquals(1, r.validadas)
        assertEquals(1, r.porValidar)
        assertEquals(0.25f, r.avance, 0.001f)
    }

    // Si el cultivo nunca se ha regado, el próximo riego se calcula desde la fecha de siembra.
    @Test fun proximoRiegoParteDeLaSiembraSiNuncaSeRego() {
        assertEquals(cultivo.fechaSiembra + 3 * MILIS_POR_DIA, proximoRiego(cultivo, emptyList()))
    }

    // Si ya hubo un riego, el siguiente se calcula desde ese riego + la frecuencia del cultivo.
    @Test fun proximoRiegoUsaElUltimoRiegoRealizado() {
        val riego = tarea(EstadoTarea.VALIDADA, ahora - MILIS_POR_DIA)
            .copy(fechaRealizacion = ahora - MILIS_POR_DIA)
        assertEquals(ahora + 2 * MILIS_POR_DIA, proximoRiego(cultivo, listOf(riego)))
    }

    // validarTexto: acepta texto normal y rechaza el vacío, los espacios y el texto demasiado largo.
    @Test fun validacionesDeTexto() {
        assertTrue(validarTexto("Lechuga", 60))
        assertFalse(validarTexto("   ", 60))
        assertFalse(validarTexto("a".repeat(11), 10))
    }

    // Frecuencia de riego (número > 0) y correo (con @ y terminado en .cl).
    @Test fun validacionesNumericasYDeCorreo() {
        assertTrue(validarNumeroPositivo("3"))
        assertFalse(validarNumeroPositivo("0"))
        assertFalse(validarNumeroPositivo("abc"))
        assertTrue(validarCorreo("docente@huerta.cl"))
        assertFalse(validarCorreo("docente@huerta.com"))
    }

    // Registro: la contraseña necesita 8 o más caracteres y el nombre al menos nombre y apellido.
    @Test fun validacionesDeRegistro() {
        assertTrue(validarClave("huerto2026"))
        assertFalse(validarClave("1234567"))
        assertTrue(validarNombreCompleto("Sofía Navarro Lagos"))
        assertFalse(validarNombreCompleto("Sofía"))
        assertFalse(validarNombreCompleto("   "))
    }

    // La fecha programada de una tarea no puede ser anterior a hoy: hoy y el futuro sí, el pasado no.
    @Test fun fechaProgramadaNoPuedeSerPasada() {
        assertTrue(validarFechaProgramada(formatearFecha(ahora), ahora = ahora))
        assertTrue(validarFechaProgramada(formatearFecha(ahora + 3 * MILIS_POR_DIA), ahora = ahora))
        assertFalse(validarFechaProgramada(formatearFecha(ahora - 3 * MILIS_POR_DIA), ahora = ahora))
        assertFalse(validarFechaProgramada("", ahora = ahora))
    }

    // Al editar una tarea se puede conservar su fecha original aunque ya haya pasado, pero no elegir otra pasada.
    @Test fun alEditarSePuedeConservarLaFechaOriginal() {
        val pasada = ahora - 5 * MILIS_POR_DIA
        val textoPasada = formatearFecha(pasada)
        assertTrue(validarFechaProgramada(textoPasada, fechaOriginal = parsearFecha(textoPasada), ahora = ahora))
        assertFalse(validarFechaProgramada(textoPasada, fechaOriginal = null, ahora = ahora))
        // La fecha guardada en la base trae también la hora (no es medianoche): igual debe conservarse.
        assertTrue(validarFechaProgramada(textoPasada, fechaOriginal = pasada, ahora = ahora))
    }

    // Una lectura simulada en estado "Bajo" debe generar un aviso para el docente.
    @Test fun lecturaFueraDeRangoGeneraAviso() {
        val lectura = LecturaSensor(
            sectorId = 1, fecha = ahora, humedadSuelo = 40, temperatura = 20.0,
            humedadAmbiente = 50, estado = EstadoLectura.BAJO
        )
        val avisos = avisosDocente(emptyList(), emptyList(), emptyList(), emptyList(), listOf(lectura), emptyList(), ahora)
        assertTrue(avisos.any { it.tipo == TipoAviso.SENSOR })
    }

    // Una tarea de mantenimiento programada en los próximos días debe generar un aviso para el docente.
    @Test fun mantenimientoProximoGeneraAviso() {
        val mant = tarea(EstadoTarea.PENDIENTE, ahora + 2 * MILIS_POR_DIA, TipoTarea.MANTENIMIENTO)
        val avisos = avisosDocente(listOf(mant), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), ahora)
        assertTrue(avisos.any { it.tipo == TipoAviso.MANTENIMIENTO })
    }
}
