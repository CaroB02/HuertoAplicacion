package com.example.huertoaplicaciones.data

import android.content.Context
import com.example.huertoaplicaciones.data.entity.Cultivo
import com.example.huertoaplicaciones.data.entity.Evidencia
import com.example.huertoaplicaciones.data.entity.LecturaSensor
import com.example.huertoaplicaciones.data.entity.Problema
import com.example.huertoaplicaciones.data.entity.Sector
import com.example.huertoaplicaciones.data.entity.Tarea
import com.example.huertoaplicaciones.data.entity.Usuario
import com.example.huertoaplicaciones.data.entity.Validacion
import com.example.huertoaplicaciones.util.EstadoCultivo
import com.example.huertoaplicaciones.util.EstadoLectura
import com.example.huertoaplicaciones.util.EstadoProblema
import com.example.huertoaplicaciones.util.EstadoSector
import com.example.huertoaplicaciones.util.EstadoTarea
import com.example.huertoaplicaciones.util.FotoUtils
import com.example.huertoaplicaciones.util.MILIS_POR_DIA
import com.example.huertoaplicaciones.util.Perfil
import com.example.huertoaplicaciones.util.TipoProblema
import com.example.huertoaplicaciones.util.TipoTarea

// Datos ficticios para poder demostrar todos los flujos de la aplicación.
// Solo se cargan cuando la base de datos está vacía.
suspend fun cargarDatosIniciales(db: AppDatabase, context: Context) {
    val usuarioDao = db.usuarioDao()
    // Si ya hay usuarios, la base YA tiene datos: salgo sin cargar nada.
    // Así los datos de prueba no se duplican cada vez que abro la app.
    if (usuarioDao.contar() > 0) return

    val ahora = System.currentTimeMillis()
    // Pequeña función auxiliar: dias(-3) = hace 3 días, dias(2) = dentro de 2 días.
    // Uso fechas RELATIVAS a hoy para que los datos de prueba siempre se vean actuales
    // (tareas atrasadas, cosechas próximas, etc.) sin importar cuándo corra la app.
    fun dias(n: Int) = ahora + n * MILIS_POR_DIA
    // Si la imagen de ejemplo no se pudo crear, los cultivos y reportes quedan sin foto
    val fotoUriDemo: String? = FotoUtils.crearFotoDemo(context)
    val fotoDemo = fotoUriDemo ?: ""

    // El ORDEN de inserción importa: primero las tablas "padre" (usuarios, sectores, cultivos)
    // y después las que dependen de ellas (tareas, evidencias...). Si no, las claves foráneas fallan.
    // insertar() devuelve el id que la base le asignó; lo guardo para enlazar las demás filas.
    // ---------- Usuarios: 1 docente y 3 estudiantes ----------
    // Los nombres son INVENTADOS (el caso exige datos ficticios), aunque parezcan reales.
    val docente = usuarioDao.insertar(
        Usuario(nombre = "Carolina Muñoz Soto", correo = "carolina.munoz@huerta.cl",
            clave = "123456", perfil = Perfil.ADMIN, grupo = "Taller Huerto")
    )
    val uno = usuarioDao.insertar(
        Usuario(nombre = "Matías Fuentes Rojas", correo = "matias.fuentes@huerta.cl",
            clave = "123456", perfil = Perfil.ESTUDIANTE, grupo = "Grupo A")
    )
    val dos = usuarioDao.insertar(
        Usuario(nombre = "Valentina Pérez Díaz", correo = "valentina.perez@huerta.cl",
            clave = "123456", perfil = Perfil.ESTUDIANTE, grupo = "Grupo B")
    )
    val tres = usuarioDao.insertar(
        Usuario(nombre = "Benjamín Castro Vera", correo = "benjamin.castro@huerta.cl",
            clave = "123456", perfil = Perfil.ESTUDIANTE, grupo = "Grupo C")
    )

    // Los sectores apuntan al docente (responsableId) con el id que obtuve arriba.
    // ---------- Sectores ----------
    val sectorDao = db.sectorDao()
    val norte = sectorDao.insertar(
        Sector(codigo = "S-01", nombre = "Huerta Norte", descripcion = "Camas de cultivo al aire libre",
            ubicacion = "Patio norte, junto al gimnasio", estado = EstadoSector.EN_USO,
            responsableId = docente, fechaCreacion = dias(-60), fechaActualizacion = dias(-5))
    )
    val invernadero = sectorDao.insertar(
        Sector(codigo = "S-02", nombre = "Invernadero", descripcion = "Estructura cubierta para cultivos sensibles",
            ubicacion = "Costado de la biblioteca", estado = EstadoSector.EN_USO,
            responsableId = docente, fechaCreacion = dias(-60), fechaActualizacion = dias(-10))
    )
    val hierbas = sectorDao.insertar(
        Sector(codigo = "S-03", nombre = "Jardín de hierbas", descripcion = "Macetas y camas pequeñas de hierbas aromáticas",
            ubicacion = "Entrada del colegio", estado = EstadoSector.DISPONIBLE,
            responsableId = null, fechaCreacion = dias(-45), fechaActualizacion = dias(-45))
    )

    // Cada cultivo necesita el id de su sector (sectorId = norte / invernadero / hierbas).
    // ---------- Cultivos ----------
    val cultivoDao = db.cultivoDao()
    val lechuga = cultivoDao.insertar(
        Cultivo(nombre = "Lechuga", descripcion = "Lechuga costina para ensaladas", sectorId = norte,
            fechaSiembra = dias(-20), fechaTrasplante = dias(-10), fechaCosecha = dias(5),
            frecuenciaRiegoDias = 2, estado = EstadoCultivo.EN_CRECIMIENTO,
            observaciones = "Crece bien, hojas firmes", fotoUri = fotoUriDemo)
    )
    val zanahoria = cultivoDao.insertar(
        Cultivo(nombre = "Zanahoria", descripcion = "Zanahoria orgánica", sectorId = norte,
            fechaSiembra = dias(-10), fechaCosecha = dias(50),
            frecuenciaRiegoDias = 4, estado = EstadoCultivo.SEMBRADO)
    )
    val tomate = cultivoDao.insertar(
        Cultivo(nombre = "Tomate cherry", descripcion = "Tomate cherry con tutor", sectorId = invernadero,
            fechaSiembra = dias(-40), fechaTrasplante = dias(-25), fechaCosecha = dias(25),
            frecuenciaRiegoDias = 3, estado = EstadoCultivo.EN_CRECIMIENTO, fotoUri = fotoUriDemo)
    )
    cultivoDao.insertar(
        Cultivo(nombre = "Albahaca", descripcion = "Albahaca para el taller de cocina", sectorId = hierbas,
            fechaSiembra = dias(5), fechaCosecha = dias(60),
            frecuenciaRiegoDias = 2, estado = EstadoCultivo.PLANIFICADO)
    )
    val cilantro = cultivoDao.insertar(
        Cultivo(nombre = "Cilantro", descripcion = "Cilantro en maceta", sectorId = hierbas,
            fechaSiembra = dias(-15), fechaCosecha = dias(20),
            frecuenciaRiegoDias = 2, estado = EstadoCultivo.CON_PROBLEMAS,
            observaciones = "Hojas con manchas")
    )

    // Creo una tarea en CADA estado posible para poder demostrar todos los flujos:
    // pendiente, atrasada, en desarrollo, por validar, validada y rechazada.
    // ---------- Tareas en distintos estados ----------
    val tareaDao = db.tareaDao()
    val evidenciaDao = db.evidenciaDao()
    val validacionDao = db.validacionDao()

    // Pendiente (próxima)
    tareaDao.insertar(
        Tarea(tipo = TipoTarea.RIEGO, descripcion = "Regar la lechuga",
            instrucciones = "Regar con regadera, sin mojar las hojas, hasta que la tierra esté húmeda.",
            sectorId = norte, cultivoId = lechuga, fechaCreacion = dias(-2), fechaProgramada = dias(1),
            asignadoAId = uno, creadoPorId = docente, estado = EstadoTarea.PENDIENTE)
    )
    // Pendiente y vencida: se muestra como Atrasada
    tareaDao.insertar(
        Tarea(tipo = TipoTarea.MALEZAS, descripcion = "Retirar malezas de la zanahoria",
            instrucciones = "Sacar las malezas con la mano, cuidando no arrancar las plántulas.",
            sectorId = norte, cultivoId = zanahoria, fechaCreacion = dias(-6), fechaProgramada = dias(-3),
            asignadoAId = dos, creadoPorId = docente, estado = EstadoTarea.PENDIENTE)
    )
    // En desarrollo
    tareaDao.insertar(
        Tarea(tipo = TipoTarea.OBSERVACION, descripcion = "Observar el crecimiento del tomate",
            instrucciones = "Medir la altura de tres plantas y anotar el color de las hojas.",
            sectorId = invernadero, cultivoId = tomate, fechaCreacion = dias(-3), fechaProgramada = dias(2),
            asignadoAId = tres, creadoPorId = docente, estado = EstadoTarea.EN_DESARROLLO)
    )
    // Pendiente de validación (con evidencia)
    val porValidar = tareaDao.insertar(
        Tarea(tipo = TipoTarea.RIEGO, descripcion = "Regar el tomate",
            instrucciones = "Regar en la base de la planta, temprano en la mañana.",
            sectorId = invernadero, cultivoId = tomate, fechaCreacion = dias(-4), fechaProgramada = dias(-1),
            asignadoAId = uno, creadoPorId = docente, estado = EstadoTarea.PENDIENTE_VALIDACION,
            fechaRealizacion = dias(-1), comentarioEstudiante = "Regué las seis plantas del invernadero.")
    )
    evidenciaDao.insertar(Evidencia(tareaId = porValidar, uri = fotoDemo, fecha = dias(-1), subidaPorId = uno))
    // Validada (con evidencia y registro de validación)
    val validada = tareaDao.insertar(
        Tarea(tipo = TipoTarea.SIEMBRA, descripcion = "Sembrar zanahoria",
            instrucciones = "Sembrar a 1 cm de profundidad con 5 cm entre semillas.",
            sectorId = norte, cultivoId = zanahoria, fechaCreacion = dias(-14), fechaProgramada = dias(-10),
            asignadoAId = dos, creadoPorId = docente, estado = EstadoTarea.VALIDADA,
            fechaRealizacion = dias(-10), comentarioEstudiante = "Sembramos dos hileras.",
            comentarioDocente = "Muy buen trabajo.", validadoPorId = docente, fechaValidacion = dias(-9))
    )
    evidenciaDao.insertar(Evidencia(tareaId = validada, uri = fotoDemo, fecha = dias(-10), subidaPorId = dos))
    validacionDao.insertar(
        Validacion(tareaId = validada, validadorId = docente, resultado = EstadoTarea.VALIDADA,
            comentario = "Muy buen trabajo.", fecha = dias(-9))
    )
    // Riego validado: sirve para calcular el próximo riego de la lechuga
    val riegoValidado = tareaDao.insertar(
        Tarea(tipo = TipoTarea.RIEGO, descripcion = "Regar la lechuga",
            instrucciones = "Regar hasta que la tierra esté húmeda.",
            sectorId = norte, cultivoId = lechuga, fechaCreacion = dias(-6), fechaProgramada = dias(-3),
            asignadoAId = uno, creadoPorId = docente, estado = EstadoTarea.VALIDADA,
            fechaRealizacion = dias(-3), comentarioEstudiante = "Listo, tierra húmeda.",
            comentarioDocente = "Correcto.", validadoPorId = docente, fechaValidacion = dias(-3))
    )
    evidenciaDao.insertar(Evidencia(tareaId = riegoValidado, uri = fotoDemo, fecha = dias(-3), subidaPorId = uno))
    validacionDao.insertar(
        Validacion(tareaId = riegoValidado, validadorId = docente, resultado = EstadoTarea.VALIDADA,
            comentario = "Correcto.", fecha = dias(-3))
    )
    // Rechazada: el estudiante debe corregir y reenviar
    val rechazada = tareaDao.insertar(
        Tarea(tipo = TipoTarea.LIMPIEZA, descripcion = "Limpiar las macetas del cilantro",
            instrucciones = "Retirar hojas secas y restos de tierra alrededor de las macetas.",
            sectorId = hierbas, cultivoId = cilantro, fechaCreacion = dias(-7), fechaProgramada = dias(-4),
            asignadoAId = tres, creadoPorId = docente, estado = EstadoTarea.RECHAZADA,
            fechaRealizacion = dias(-4), comentarioEstudiante = "Limpié las macetas.",
            comentarioDocente = "Faltó limpiar la maceta del fondo. Corrígelo y vuelve a enviar.",
            validadoPorId = docente, fechaValidacion = dias(-3))
    )
    evidenciaDao.insertar(Evidencia(tareaId = rechazada, uri = fotoDemo, fecha = dias(-4), subidaPorId = tres))
    validacionDao.insertar(
        Validacion(tareaId = rechazada, validadorId = docente, resultado = EstadoTarea.RECHAZADA,
            comentario = "Faltó limpiar la maceta del fondo. Corrígelo y vuelve a enviar.", fecha = dias(-3))
    )

    // Un reporte abierto (Informado) y uno ya cerrado, para ver ambos casos en la pantalla.
    // ---------- Reportes de problemas ----------
    val problemaDao = db.problemaDao()
    problemaDao.insertar(
        Problema(sectorId = hierbas, cultivoId = cilantro, tipo = TipoProblema.PLAGA,
            descripcion = "Se ven manchas y pequeños bichos en las hojas del cilantro.",
            fechaReporte = dias(-2), reportadoPorId = tres, fotoUri = fotoUriDemo,
            estado = EstadoProblema.INFORMADO)
    )
    problemaDao.insertar(
        Problema(sectorId = invernadero, cultivoId = tomate, tipo = TipoProblema.FALTA_AGUA,
            descripcion = "La tierra del tomate estaba muy seca por la mañana.",
            fechaReporte = dias(-8), reportadoPorId = uno,
            estado = EstadoProblema.CERRADO,
            comentarioDocente = "Se ajustó la frecuencia de riego y se revisó el goteo.",
            fechaCierre = dias(-6))
    )

    // Datos de prueba, NO vienen de sensores reales. El del invernadero está en estado "Bajo"
    // a propósito, para que el panel del docente muestre el aviso de lectura fuera de rango.
    // ---------- Lecturas simuladas de sensores ----------
    val sensorDao = db.lecturaSensorDao()
    sensorDao.insertar(
        LecturaSensor(sectorId = norte, fecha = dias(0) - 3_600_000L, humedadSuelo = 62,
            temperatura = 21.5, humedadAmbiente = 55, estado = EstadoLectura.NORMAL)
    )
    sensorDao.insertar(
        LecturaSensor(sectorId = invernadero, fecha = dias(0) - 1_800_000L, humedadSuelo = 48,
            temperatura = 27.8, humedadAmbiente = 70, estado = EstadoLectura.BAJO)
    )
    sensorDao.insertar(
        LecturaSensor(sectorId = hierbas, fecha = dias(0) - 7_200_000L, humedadSuelo = 71,
            temperatura = 19.2, humedadAmbiente = 60, estado = EstadoLectura.NORMAL)
    )
}
