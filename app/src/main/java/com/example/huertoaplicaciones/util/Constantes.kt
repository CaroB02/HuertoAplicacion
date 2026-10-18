package com.example.huertoaplicaciones.util

// Aquí junto todos los textos "fijos" de la app (perfiles, estados, tipos).
// Los guardo como constantes (const val) para no escribir el mismo texto en muchos lados:
// si lo escribiera a mano y me equivocara en una letra, la comparación fallaría sin avisarme.
// "object" = un contenedor único: se usa así -> EstadoTarea.PENDIENTE

// Los dos roles de la app. El perfil decide qué pantallas y acciones puede usar cada persona.
object Perfil {
    const val ADMIN = "ADMIN"
    const val ESTUDIANTE = "ESTUDIANTE"
}

// Ciclo de vida de una tarea:
// Pendiente -> En desarrollo -> Pendiente de validación -> Validada o Rechazada.
// "Atrasada" NO se guarda en la base: se calcula al mostrar (mira estadoMostrado en TareaUtil.kt).
// Código que se pide para crear una cuenta de DOCENTE. Cualquiera puede registrarse como estudiante,
// pero ser docente da permisos de administración (crear tareas, validar, gestionar sectores...),
// así que solo se permite a quien conozca este código (se lo entrega el encargado del taller).
// Es un código de demostración: en una app real se verificaría contra un servidor.
const val CODIGO_DOCENTE = "HUERTO2026"

object EstadoTarea {
    const val PENDIENTE = "Pendiente"
    const val EN_DESARROLLO = "En desarrollo"
    const val REALIZADA = "Realizada"
    const val PENDIENTE_VALIDACION = "Pendiente de validación"
    const val VALIDADA = "Validada"
    const val RECHAZADA = "Rechazada"
    const val ATRASADA = "Atrasada"
}

object EstadoSector {
    const val DISPONIBLE = "Disponible"
    const val EN_USO = "En uso"
    const val EN_MANTENIMIENTO = "En mantenimiento"
    const val FUERA_DE_USO = "Fuera de uso"
}

object EstadoCultivo {
    const val PLANIFICADO = "Planificado"
    const val SEMBRADO = "Sembrado"
    const val EN_CRECIMIENTO = "En crecimiento"
    const val LISTO_COSECHA = "Listo para cosechar"
    const val COSECHADO = "Cosechado"
    const val CON_PROBLEMAS = "Con problemas"
    const val FINALIZADO = "Finalizado"
}

object EstadoProblema {
    const val INFORMADO = "Informado"
    const val EN_REVISION = "En revisión"
    const val ATENDIDO = "Atendido"
    const val CERRADO = "Cerrado"

    // Lista con todos los estados, en orden: la uso para dibujar los botones de selección.
    val todos = listOf(INFORMADO, EN_REVISION, ATENDIDO, CERRADO)
}

object TipoTarea {
    const val PREPARACION = "Preparación del sector"
    const val SIEMBRA = "Siembra"
    const val TRASPLANTE = "Trasplante"
    const val RIEGO = "Riego"
    const val LIMPIEZA = "Limpieza"
    const val MALEZAS = "Retiro de malezas"
    const val OBSERVACION = "Observación del cultivo"
    const val MANTENIMIENTO = "Mantenimiento"
    const val REVISION_SUELO = "Revisión del suelo"
    const val REGISTRO_CRECIMIENTO = "Registro del crecimiento"
    const val COSECHA = "Cosecha"
    const val OTRA = "Otra actividad autorizada"

    // Todos los tipos juntos: el formulario de tareas dibuja un botón por cada uno.
    val todos = listOf(
        RIEGO, SIEMBRA, TRASPLANTE, PREPARACION, LIMPIEZA, MALEZAS, OBSERVACION,
        MANTENIMIENTO, REVISION_SUELO, REGISTRO_CRECIMIENTO, COSECHA, OTRA
    )
}

object TipoProblema {
    const val FALTA_AGUA = "Falta de agua"
    const val PLANTA_DANADA = "Daño en la planta"
    const val PLAGA = "Plaga simulada"
    const val DETERIORO = "Deterioro del sector"
    const val MANTENIMIENTO = "Problema de mantenimiento"
    const val OTRO = "Otro problema observado"

    val todos = listOf(FALTA_AGUA, PLANTA_DANADA, PLAGA, DETERIORO, MANTENIMIENTO, OTRO)
}

// Estados de una lectura simulada de sensores
object EstadoLectura {
    const val NORMAL = "Normal"
    const val BAJO = "Bajo"
    const val ALTO = "Alto"
    const val PENDIENTE_REVISION = "Pendiente de revisión"
}

// Marca que identifica los datos de sensores como no reales
const val ORIGEN_SIMULADO = "simulación académica"

// Longitudes máximas de los campos de texto de los formularios.
// Las uso en las validaciones (validarTexto) y en los mensajes de error "máx. N caracteres".
object Limites {
    const val CODIGO = 10
    const val NOMBRE = 60
    const val DESCRIPCION = 200
    const val COMENTARIO = 300
}
