package com.example.huertoaplicaciones.util

object Perfil {
    const val ADMIN = "ADMIN"
    const val ESTUDIANTE = "ESTUDIANTE"
}

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
    const val COSECHA = "Cosecha"
}