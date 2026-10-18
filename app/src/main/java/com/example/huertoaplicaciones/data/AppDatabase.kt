package com.example.huertoaplicaciones.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.huertoaplicaciones.data.dao.*
import com.example.huertoaplicaciones.data.entity.*

// Esta clase ES la base de datos. Hace tres cosas:
//  1) Lista todas las entidades (tablas) que existen.
//  2) Entrega los DAOs para poder consultarlas.
//  3) Se asegura de que exista UNA SOLA instancia de la base en toda la app (patrón Singleton).
//
// El archivo real se llama "huerto_db" y vive dentro del teléfono (no dentro del proyecto):
// /data/data/com.example.huertoaplicaciones/databases/huerto_db
@Database(
    entities = [Usuario::class, Sector::class, Cultivo::class, Tarea::class,
        Evidencia::class, Validacion::class, Problema::class, LecturaSensor::class],
    // La versión sube cada vez que cambio el esquema (agregar o quitar tablas o columnas).
    // Estoy en la 2 porque agregué Evidencia, Validacion y las claves foráneas.
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // Una función por cada DAO. Son "abstract": Room escribe su implementación automáticamente.
    abstract fun usuarioDao(): UsuarioDao
    abstract fun sectorDao(): SectorDao
    abstract fun cultivoDao(): CultivoDao
    abstract fun tareaDao(): TareaDao
    abstract fun evidenciaDao(): EvidenciaDao
    abstract fun validacionDao(): ValidacionDao
    abstract fun problemaDao(): ProblemaDao
    abstract fun lecturaSensorDao(): LecturaSensorDao

    // companion object = lo que está aquí se usa sin crear un objeto, como en Java con "static".
    companion object {
        // @Volatile: todos los hilos ven siempre el valor más reciente de INSTANCE.
        @Volatile private var INSTANCE: AppDatabase? = null

        // Devuelve la base. Si ya existe la reutiliza; si no, la crea.
        // "synchronized" evita que dos hilos la creen a la vez y terminen con dos bases distintas.
        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    // applicationContext vive mientras vive la app (no se filtra memoria de una pantalla).
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huerto_db"
                )
                    // Proyecto académico: si cambia el esquema se recrea la base
                    // y se vuelven a cargar los datos de prueba
                    // (en una app real habría que escribir una migración para no perder los datos).
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { INSTANCE = it }
            }
    }
}
