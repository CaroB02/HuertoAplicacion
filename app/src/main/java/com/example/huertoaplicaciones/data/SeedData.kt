package com.example.huertoaplicaciones.data

import com.example.huertoaplicaciones.data.entity.Usuario

suspend fun cargarDatosIniciales(db: AppDatabase) {
    val usuarioDao = db.usuarioDao()
    if (usuarioDao.contar() > 0) return

    usuarioDao.insertar(
        Usuario(nombre = "Docente Prueba", correo = "docente@huerta.cl",
            clave = "123456", perfil = "ADMIN", grupo = "Taller Huerto")
    )
    usuarioDao.insertar(
        Usuario(nombre = "Estudiante Uno", correo = "estudiante@huerta.cl",
            clave = "123456", perfil = "ESTUDIANTE", grupo = "Grupo A")
    )
    usuarioDao.insertar(
        Usuario(nombre = "Estudiante Dos", correo = "estudiante2@huerta.cl",
            clave = "123456", perfil = "ESTUDIANTE", grupo = "Grupo B")
    )
}