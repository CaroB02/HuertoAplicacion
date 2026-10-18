package com.example.huertoaplicaciones.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ENTIDAD = una tabla de la base de datos. Cada objeto Usuario es una fila de la tabla "usuarios".
// Aquí guardo a las personas que pueden entrar a la app: el docente (ADMIN) y los estudiantes.
// Es la tabla "padre" de casi todas las demás (tareas, reportes, etc. apuntan a un usuario).
@Entity(
    tableName = "usuarios",
    // Índice único en el correo: no pueden existir dos usuarios con el mismo correo.
    indices = [Index(value = ["correo"], unique = true)]
)
data class Usuario(
    // Clave primaria: Room genera el id solo (1, 2, 3...). Por eso el valor por defecto es 0.
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    // Contraseña en texto plano: sirve para este proyecto académico, en una app real iría cifrada.
    val clave: String,
    val perfil: String,          // "ADMIN" o "ESTUDIANTE"
    val grupo: String = "",
    // Si activo = false la persona no puede iniciar sesión ni recibir tareas.
    val activo: Boolean = true
)
