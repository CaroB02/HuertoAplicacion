package com.example.huertoaplicaciones.viewmodel

import com.example.huertoaplicaciones.DatosPrueba
import com.example.huertoaplicaciones.ReglaMainDispatcher
import com.example.huertoaplicaciones.data.entity.Evidencia
import com.example.huertoaplicaciones.data.entity.Tarea
import com.example.huertoaplicaciones.data.repository.CultivoRepository
import com.example.huertoaplicaciones.data.repository.SectorRepository
import com.example.huertoaplicaciones.data.repository.TareaRepository
import com.example.huertoaplicaciones.data.repository.UsuarioRepository
import com.example.huertoaplicaciones.escucharMensajes
import com.example.huertoaplicaciones.observar
import com.example.huertoaplicaciones.util.EstadoTarea
import com.example.huertoaplicaciones.util.MILIS_POR_DIA
import com.example.huertoaplicaciones.util.TipoTarea
import com.example.huertoaplicaciones.util.formatearFecha
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Pruebas del ViewModel de TAREAS: aquí están las reglas de negocio más importantes del proyecto
// (quién puede crear, iniciar, entregar, validar, rechazar y eliminar).
// Qué se MOCKEA: los 4 repositorios. Qué se PRUEBA: que cada regla deje pasar o bloquee la acción.
class TareaViewModelTest {

    @get:Rule
    val reglaMain = ReglaMainDispatcher()

    private val repository = mockk<TareaRepository>(relaxed = true)
    private val sectorRepository = mockk<SectorRepository>(relaxed = true)
    private val cultivoRepository = mockk<CultivoRepository>(relaxed = true)
    private val usuarioRepository = mockk<UsuarioRepository>(relaxed = true)

    private val ahora = System.currentTimeMillis()
    private val tareas = MutableStateFlow<List<Tarea>>(emptyList())

    private fun crearViewModel(): TareaViewModel {
        every { repository.obtenerTodas() } returns tareas
        every { repository.obtenerPorEstudiante(2) } returns flowOf(listOf(DatosPrueba.tarea()))
        every { repository.evidenciasDe(any()) } returns flowOf(
            listOf(Evidencia(id = 1, tareaId = 10, uri = "foto.jpg", fecha = 1L, subidaPorId = 2))
        )
        every { repository.validacionesDe(any()) } returns flowOf(emptyList())
        every { sectorRepository.obtenerTodos() } returns MutableStateFlow(listOf(DatosPrueba.sector))
        every { cultivoRepository.obtenerTodos() } returns MutableStateFlow(listOf(DatosPrueba.cultivo))
        every { usuarioRepository.obtenerEstudiantes() } returns MutableStateFlow(listOf(DatosPrueba.estudiante))
        every { usuarioRepository.obtenerTodos() } returns
            MutableStateFlow(listOf(DatosPrueba.docente, DatosPrueba.estudiante))
        coEvery { usuarioRepository.obtenerPorId(1) } returns DatosPrueba.docente
        coEvery { usuarioRepository.obtenerPorId(2) } returns DatosPrueba.estudiante
        coEvery { cultivoRepository.obtenerPorId(1) } returns DatosPrueba.cultivo
        return TareaViewModel(repository, sectorRepository, cultivoRepository, usuarioRepository)
    }

    private fun llenarFormulario(vm: TareaViewModel, estudianteId: Long = 2) {
        vm.nuevaTarea()
        vm.cambiarTipo(TipoTarea.RIEGO)
        vm.cambiarDescripcion("Regar la lechuga")
        vm.cambiarInstrucciones("Con regadera, sin mojar las hojas")
        vm.cambiarSector(1)
        vm.cambiarCultivo(1)
        vm.cambiarEstudiante(estudianteId)
        vm.cambiarFecha(formatearFecha(ahora + 2 * MILIS_POR_DIA))
    }

    // ---------- Crear y editar ----------

    @Test
    fun unEstudianteNoPuedeCrearTareas() = runTest {
        val vm = crearViewModel()
        llenarFormulario(vm)

        vm.guardar(DatosPrueba.estudiante)

        coVerify(exactly = 0) { repository.guardar(any()) }
    }

    @Test
    fun formularioVacioMarcaTodosLosErrores() = runTest {
        val vm = crearViewModel()
        vm.nuevaTarea()

        vm.guardar(DatosPrueba.docente)

        val f = vm.form.value
        assertTrue(f.errorDescripcion && f.errorInstrucciones && f.errorSector)
        assertTrue(f.errorCultivo && f.errorEstudiante && f.errorFecha)
        coVerify(exactly = 0) { repository.guardar(any()) }
    }

    @Test
    fun laFechaNoPuedeSerPasada() = runTest {
        val vm = crearViewModel()
        llenarFormulario(vm)

        vm.cambiarFecha(formatearFecha(ahora - 3 * MILIS_POR_DIA))

        assertTrue(vm.form.value.errorFecha)
    }

    @Test
    fun elResponsableDebeSerUnEstudiante() = runTest {
        val vm = crearViewModel()
        llenarFormulario(vm, estudianteId = 1)   // el usuario 1 es el docente

        vm.guardar(DatosPrueba.docente)

        assertTrue(vm.form.value.errorEstudiante)
        coVerify(exactly = 0) { repository.guardar(any()) }
    }

    @Test
    fun elCultivoDebePertenecerAlSectorElegido() = runTest {
        val vm = crearViewModel()
        coEvery { cultivoRepository.obtenerPorId(1) } returns DatosPrueba.cultivo.copy(sectorId = 9)
        llenarFormulario(vm)

        vm.guardar(DatosPrueba.docente)

        assertTrue(vm.form.value.errorCultivo)
        coVerify(exactly = 0) { repository.guardar(any()) }
    }

    @Test
    fun tareaValidaSeCreaPendienteYAsignada() = runTest {
        val vm = crearViewModel()
        llenarFormulario(vm)

        vm.guardar(DatosPrueba.docente)

        coVerify {
            repository.guardar(match {
                it.estado == EstadoTarea.PENDIENTE && it.creadoPorId == 1L && it.asignadoAId == 2L
            })
        }
        assertTrue(vm.form.value.guardado)
    }

    @Test
    fun editarUnaTareaPendienteHacePut() = runTest {
        coEvery { repository.obtenerPorId(10) } returns DatosPrueba.tarea()
        val vm = crearViewModel()

        vm.editarTarea(10)
        assertEquals("Regar la lechuga", vm.form.value.descripcion)

        vm.cambiarDescripcion("Regar la lechuga temprano")
        vm.guardar(DatosPrueba.docente)

        coVerify { repository.actualizar(match { it.id == 10L && it.descripcion == "Regar la lechuga temprano" }) }
        assertTrue(vm.form.value.guardado)
    }

    @Test
    fun noSePuedeEditarUnaTareaYaIniciada() = runTest {
        coEvery { repository.obtenerPorId(10) } returns DatosPrueba.tarea(estado = EstadoTarea.EN_DESARROLLO)
        val vm = crearViewModel()
        vm.editarTarea(10)

        vm.guardar(DatosPrueba.docente)

        coVerify(exactly = 0) { repository.actualizar(any()) }
        assertFalse(vm.form.value.guardado)
    }

    @Test
    fun sinConexionAlGuardarSeAvisaYNoSeCierraLaApp() = runTest {
        val mensajes = escucharMensajes()
        val vm = crearViewModel()
        coEvery { usuarioRepository.obtenerPorId(2) } throws IOException("sin red")
        coEvery { repository.obtenerPorId(77) } throws IOException("sin red")
        llenarFormulario(vm)

        vm.guardar(DatosPrueba.docente)
        vm.editarTarea(77)
        runCurrent()

        assertFalse(vm.form.value.guardado)
        assertTrue(mensajes.all { it.esError })
        assertEquals(2, mensajes.size)
    }

    // ---------- Detalle, iniciar y entregar ----------

    @Test
    fun seleccionarCargaLaTareaYSusFotos() = runTest {
        coEvery { repository.obtenerPorId(10) } returns DatosPrueba.tarea()
        val vm = crearViewModel()
        observar(vm.evidencias)
        observar(vm.validaciones)

        vm.seleccionar(10)

        assertEquals(10L, vm.seleccionada.value?.id)
        assertEquals(1, vm.evidencias.value.size)
        assertTrue(vm.validaciones.value.isEmpty())
    }

    @Test
    fun seleccionarSinConexionNoCierraLaApp() = runTest {
        coEvery { repository.obtenerPorId(10) } throws IOException("sin red")
        val vm = crearViewModel()

        vm.seleccionar(10)

        assertNull(vm.seleccionada.value)
    }

    @Test
    fun soloElEstudianteAsignadoPuedeIniciarLaTarea() = runTest {
        val vm = crearViewModel()
        val tarea = DatosPrueba.tarea(asignadoA = 2)

        vm.iniciar(tarea, DatosPrueba.otroEstudiante)
        coVerify(exactly = 0) { repository.actualizar(any()) }

        vm.iniciar(tarea, DatosPrueba.estudiante)
        coVerify { repository.actualizar(match { it.estado == EstadoTarea.EN_DESARROLLO }) }
        assertEquals(EstadoTarea.EN_DESARROLLO, vm.seleccionada.value?.estado)
    }

    @Test
    fun iniciarSinConexionNoCambiaElEstado() = runTest {
        coEvery { repository.actualizar(any()) } throws IOException("sin red")
        val vm = crearViewModel()

        vm.iniciar(DatosPrueba.tarea(), DatosPrueba.estudiante)

        assertNull(vm.seleccionada.value)
    }

    @Test
    fun laEntregaExigeComentarioYFoto() = runTest {
        val vm = crearViewModel()
        val tarea = DatosPrueba.tarea(estado = EstadoTarea.EN_DESARROLLO)

        vm.marcarRealizada(tarea, DatosPrueba.estudiante, "", emptyList())

        assertTrue(vm.errorEntrega.value.comentario)
        assertTrue(vm.errorEntrega.value.foto)
        coVerify(exactly = 0) { repository.entregar(any(), any(), any()) }

        vm.quitarErrorEntrega()
        assertFalse(vm.errorEntrega.value.foto)
    }

    @Test
    fun entregaCorrectaQuedaPendienteDeValidacion() = runTest {
        val vm = crearViewModel()
        val tarea = DatosPrueba.tarea(estado = EstadoTarea.EN_DESARROLLO)

        vm.marcarRealizada(tarea, DatosPrueba.estudiante, " Regué todo ", listOf("foto.jpg"))

        coVerify {
            repository.entregar(
                match { it.estado == EstadoTarea.PENDIENTE_VALIDACION && it.comentarioEstudiante == "Regué todo" },
                listOf("foto.jpg"),
                2L
            )
        }
        assertTrue(vm.accionHecha.value)
    }

    @Test
    fun unaTareaRechazadaSePuedeReenviarPeroNoUnaPendiente() = runTest {
        val vm = crearViewModel()

        vm.marcarRealizada(DatosPrueba.tarea(estado = EstadoTarea.PENDIENTE), DatosPrueba.estudiante, "ok", listOf("f.jpg"))
        coVerify(exactly = 0) { repository.entregar(any(), any(), any()) }

        vm.marcarRealizada(DatosPrueba.tarea(estado = EstadoTarea.RECHAZADA), DatosPrueba.estudiante, "ok", listOf("f.jpg"))
        coVerify(exactly = 1) { repository.entregar(any(), any(), any()) }
    }

    @Test
    fun entregaSinConexionNoMarcaAccionHecha() = runTest {
        coEvery { repository.entregar(any(), any(), any()) } throws IOException("sin red")
        val vm = crearViewModel()

        vm.marcarRealizada(DatosPrueba.tarea(estado = EstadoTarea.EN_DESARROLLO), DatosPrueba.estudiante, "ok", listOf("f.jpg"))

        assertFalse(vm.accionHecha.value)
    }

    // ---------- Validar y rechazar ----------

    @Test
    fun elDocenteValidaUnaEntrega() = runTest {
        val vm = crearViewModel()
        val tarea = DatosPrueba.tarea(estado = EstadoTarea.PENDIENTE_VALIDACION)

        vm.validar(tarea, DatosPrueba.docente, "Muy bien")

        coVerify {
            repository.registrarValidacion(
                match { it.estado == EstadoTarea.VALIDADA && it.validadoPorId == 1L },
                match { it.resultado == EstadoTarea.VALIDADA && it.comentario == "Muy bien" }
            )
        }
        assertTrue(vm.accionHecha.value)
    }

    @Test
    fun unEstudianteNuncaValidaNiUnaTareaQueNoEstaPorValidar() = runTest {
        val vm = crearViewModel()

        vm.validar(DatosPrueba.tarea(estado = EstadoTarea.PENDIENTE_VALIDACION), DatosPrueba.estudiante, "")
        vm.validar(DatosPrueba.tarea(estado = EstadoTarea.PENDIENTE), DatosPrueba.docente, "")
        // Nadie aprueba su propio trabajo: tarea asignada al mismo docente
        vm.validar(DatosPrueba.tarea(estado = EstadoTarea.PENDIENTE_VALIDACION, asignadoA = 1), DatosPrueba.docente, "")

        coVerify(exactly = 0) { repository.registrarValidacion(any(), any()) }
    }

    @Test
    fun rechazarExigeElMotivo() = runTest {
        val vm = crearViewModel()
        val tarea = DatosPrueba.tarea(estado = EstadoTarea.PENDIENTE_VALIDACION)

        vm.rechazar(tarea, DatosPrueba.docente, "  ")
        assertTrue(vm.errorComentario.value)
        coVerify(exactly = 0) { repository.registrarValidacion(any(), any()) }

        vm.limpiarErrorComentario()
        assertFalse(vm.errorComentario.value)

        vm.rechazar(tarea, DatosPrueba.docente, "Falta la foto del fondo")
        coVerify {
            repository.registrarValidacion(
                match { it.estado == EstadoTarea.RECHAZADA },
                match { it.resultado == EstadoTarea.RECHAZADA && it.comentario == "Falta la foto del fondo" }
            )
        }
    }

    @Test
    fun validarORechazarSinConexionNoMarcaAccionHecha() = runTest {
        coEvery { repository.registrarValidacion(any(), any()) } throws IOException("sin red")
        val vm = crearViewModel()
        val tarea = DatosPrueba.tarea(estado = EstadoTarea.PENDIENTE_VALIDACION)

        vm.validar(tarea, DatosPrueba.docente, "ok")
        vm.rechazar(tarea, DatosPrueba.docente, "motivo")

        assertFalse(vm.accionHecha.value)
    }

    // ---------- Eliminar ----------

    @Test
    fun soloSeEliminanTareasPendientesYSoloElDocente() = runTest {
        coEvery { repository.eliminar(any()) } returns true
        val vm = crearViewModel()

        vm.eliminar(DatosPrueba.tarea(estado = EstadoTarea.EN_DESARROLLO), DatosPrueba.docente)
        vm.eliminar(DatosPrueba.tarea(), DatosPrueba.estudiante)
        coVerify(exactly = 0) { repository.eliminar(any()) }

        vm.eliminar(DatosPrueba.tarea(), DatosPrueba.docente)
        assertTrue(vm.accionHecha.value)
    }

    @Test
    fun eliminarRechazadoPorElServidorOSinConexion() = runTest {
        coEvery { repository.eliminar(any()) } returns false
        val vm = crearViewModel()
        vm.eliminar(DatosPrueba.tarea(), DatosPrueba.docente)
        assertFalse(vm.accionHecha.value)

        coEvery { repository.eliminar(any()) } throws IOException("sin red")
        vm.eliminar(DatosPrueba.tarea(), DatosPrueba.docente)
        assertFalse(vm.accionHecha.value)
    }

    // ---------- Listas, filtros y recarga ----------

    @Test
    fun filtrosDelListadoDelDocente() = runTest {
        val vm = crearViewModel()
        observar(vm.tareasFiltradas)
        tareas.value = listOf(
            DatosPrueba.tarea(id = 1, estado = EstadoTarea.PENDIENTE, asignadoA = 2),
            DatosPrueba.tarea(id = 2, estado = EstadoTarea.VALIDADA, asignadoA = 3),
            DatosPrueba.tarea(id = 3, estado = EstadoTarea.PENDIENTE, asignadoA = 3)
                .copy(fechaProgramada = ahora + 30 * MILIS_POR_DIA)
        )
        assertEquals(3, vm.tareasFiltradas.value.size)

        vm.filtrarPorEstado(EstadoTarea.VALIDADA)
        assertEquals(listOf(2L), vm.tareasFiltradas.value.map { it.id })
        assertTrue(vm.filtros.value.hayFiltros)

        vm.limpiarFiltros()
        vm.filtrarPorResponsable(3)
        assertEquals(listOf(2L, 3L), vm.tareasFiltradas.value.map { it.id })

        vm.limpiarFiltros()
        vm.filtrarPorFecha(FiltroFecha.SEMANA)
        assertEquals(listOf(1L, 2L), vm.tareasFiltradas.value.map { it.id })

        vm.filtrarPorFecha(FiltroFecha.HOY)
        assertTrue(vm.tareasFiltradas.value.isEmpty())

        vm.limpiarFiltros()
        assertFalse(vm.filtros.value.hayFiltros)
    }

    @Test
    fun listasDeApoyoYTareasDelEstudiante() = runTest {
        val vm = crearViewModel()
        observar(vm.tareas)
        observar(vm.sectores)
        observar(vm.cultivos)
        observar(vm.estudiantes)
        observar(vm.usuarios)

        assertEquals(1, vm.sectores.value.size)
        assertEquals(1, vm.cultivos.value.size)
        assertEquals(listOf(DatosPrueba.estudiante), vm.estudiantes.value)
        assertEquals(2, vm.usuarios.value.size)
        assertEquals(1, vm.tareasDeEstudiante(2).first().size)
    }

    @Test
    fun recargarPideTodoYAvisaSinConexion() = runTest {
        val mensajes = escucharMensajes()
        val vm = crearViewModel()

        vm.recargar()
        coVerify { repository.recargar() }
        coVerify { sectorRepository.recargar() }
        coVerify { cultivoRepository.recargar() }
        coVerify { usuarioRepository.recargar() }

        coEvery { repository.recargar() } throws IOException("sin red")
        vm.recargar()
        runCurrent()
        assertTrue(mensajes.last().esError)
    }
}
