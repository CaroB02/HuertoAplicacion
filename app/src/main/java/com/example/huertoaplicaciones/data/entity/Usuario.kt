package com.example.huertoaplicaciones.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    //val usuario: String,
    val correo: String,
    val clave: String,
    val perfil: String,          // "ADMIN" o "ESTUDIANTE"
    val grupo: String = "",
    val activo: Boolean = true
)