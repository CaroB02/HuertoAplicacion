package com.example.huertoaplicaciones.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.huertoaplicaciones.data.dao.*
import com.example.huertoaplicaciones.data.entity.*

@Database(
    entities = [Usuario::class, Sector::class, Cultivo::class,
        Tarea::class, Problema::class, LecturaSensor::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun sectorDao(): SectorDao
    abstract fun cultivoDao(): CultivoDao
    abstract fun tareaDao(): TareaDao
    abstract fun problemaDao(): ProblemaDao
    abstract fun lecturaSensorDao(): LecturaSensorDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huerto_db"
                ).build().also { INSTANCE = it }
            }
    }
}