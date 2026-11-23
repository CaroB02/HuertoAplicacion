package com.example.huertoaplicaciones.data.repository

import com.example.huertoaplicaciones.data.remote.RetrofitClient

// Contenedor con UNA sola instancia de cada repositorio para toda la app.
// Es importante que sea la misma instancia: si un ViewModel crea un sector, la lista que
// observa otro ViewModel se actualiza, porque ambos miran el mismo StateFlow del repositorio.
// Los ViewModels los reciben como parámetro con valor por defecto (igual que en clase:
// ClientesViewModel(repository: ClientesRepository = ClientesRepository())), así en las pruebas
// unitarias se les puede pasar un repositorio simulado con MockK.
object Repositorios {
    val usuarios by lazy { UsuarioRepository(RetrofitClient.usuariosApi) }
    val sectores by lazy { SectorRepository(RetrofitClient.huertoApi) }
    val cultivos by lazy { CultivoRepository(RetrofitClient.huertoApi) }
    val lecturas by lazy { LecturaSensorRepository(RetrofitClient.huertoApi) }
    val tareas by lazy { TareaRepository(RetrofitClient.tareasApi) }
    val problemas by lazy { ProblemaRepository(RetrofitClient.tareasApi) }
    val clima by lazy { ClimaRepository(RetrofitClient.climaApi) }
}
