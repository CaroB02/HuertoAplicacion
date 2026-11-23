# Guía de los cambios de la Evaluación Parcial 4

## Qué cambió

| Antes (EP2/EP3) | Ahora (EP4) |
|---|---|
| Datos en Room (SQLite dentro del teléfono) | Datos en **3 microservicios Spring Boot + MySQL** |
| `dao/`, `AppDatabase`, `SeedData` | **Eliminados**. Los datos de prueba los cargan los microservicios (`config/DatosIniciales.java`) |
| Repositorios llamaban a los DAO | Repositorios llaman a **Retrofit** (`data/remote/*Api.kt`) |
| ViewModels heredaban de `AndroidViewModel(application)` | Heredan de `ViewModel()` y reciben los repositorios **por constructor** (para poder probarlos con MockK) |
| Las listas se actualizaban solas (Flow de Room) | Se recargan con `LifecycleEventEffect(ON_RESUME)` en `AppNavigation.kt` |
| Sin API externa | **Clima de Open-Meteo** en el panel (`ClimaApi`, `ClimaRepository`, `ClimaViewModel`, `TarjetaClima`) |
| 12 pruebas de `util/` | 120 pruebas nuevas de repositorios, ViewModels y Retrofit con **JUnit + MockK**, cobertura medida con **Kover** |
| Sin firma | `signingConfigs` en `app/build.gradle.kts` + `keystore.properties` |

## Dónde está cada concepto de las PPT de clase

| Concepto (EA3.1 / EA3.2) | Archivo del proyecto |
|---|---|
| Permiso `INTERNET` + `network_security_config.xml` | `AndroidManifest.xml`, `res/xml/network_security_config.xml` |
| Dependencias de Retrofit | `gradle/libs.versions.toml`, `app/build.gradle.kts` |
| `RetrofitClient` con `BASE_URL` y `GsonConverterFactory` | `data/remote/RetrofitClient.kt`, `data/remote/ApiConfig.kt` |
| Interfaz con `@GET`, `@POST`, `@PUT`, `@Path`, `@Body` | `data/remote/HuertoApi.kt`, `TareasApi.kt`, `UsuariosApi.kt` |
| Modelo de escritura separado (`ClienteRequest`) | `data/remote/Requests.kt` (`SectorRequest`, `TareaRequest`...) |
| `comprobarRespuesta` + `HttpException` | `data/remote/Respuestas.kt`, usado en todos los repositorios |
| `sealed interface` Cargando / Exito / Error | `viewmodel/ClimaViewModel.kt` (`ClimaUiState`) |
| `FormState` con `cargando`, `guardando`, `guardado` | `viewmodel/SectorFormUiState.kt` + `SectorFormScreen.kt` |
| `guardar()` con `try` / `catch (CancellationException)` / `finally` | `SectorViewModel.guardar()` |
| Formulario único crear/editar según el id | `SectorFormScreen.kt` (id 0 = POST, otro id = PUT) |
| Botón con `CircularProgressIndicator` mientras guarda | `SectorFormScreen.kt` |
| `LifecycleEventEffect(Lifecycle.Event.ON_RESUME)` | `ui/navigation/AppNavigation.kt` |
| `repository: X = X()` (valor por defecto en el constructor) | Todos los ViewModels, con `Repositorios.kt` |

## Recorrido de un dato (para explicar en la defensa)

Ejemplo: el docente crea un sector.

1. `SectorFormScreen` → el usuario toca **Guardar** → `viewModel.guardar()`.
2. `SectorViewModel.guardar()` valida los campos y llama a `repository.guardar(sector)`.
3. `SectorRepository.guardar()` convierte el sector en `SectorRequest` y llama a `api.crearSector(...)`.
4. Retrofit arma `POST http://10.0.2.2:8082/sectores` con el JSON (lo pueden ver en el **Logcat** filtrando por `okhttp`).
5. En ms-huerto, `SectorController.crear()` → `SectorService.crear()` → `SectorRepository.save()` → `INSERT` en MySQL (visible en la consola por `show-sql=true`).
6. Responde `201 Created` → `comprobarRespuesta` ve `isSuccessful` → el repositorio recarga la lista (`GET /sectores`).
7. El `StateFlow` del repositorio cambia → la pantalla de sectores se redibuja sola.

## Modificaciones en vivo que pueden pedir (40% de la defensa)

**Agregar un campo** (por ejemplo `superficieM2` a Sector):
1. Backend: atributo + getter/setter en `ms-huerto/model/Sector.java`. Hibernate agrega la columna solo (`ddl-auto=update`).
2. App: campo en `data/entity/Sector.kt` y en `SectorRequest` (`Requests.kt`), y su línea en `Sector.aRequest()`.
3. App: campo en `SectorFormUiState`, función `cambiarSuperficie()` en el ViewModel, `CampoTexto` en `SectorFormScreen`.

**Agregar un endpoint** (por ejemplo, sectores por estado):
1. Backend: `List<Sector> findByEstado(String estado);` en el repositorio JPA, un método en el servicio y `@GetMapping("/estado/{estado}")` en el controlador.
2. App: `@GET("sectores/estado/{estado}") suspend fun obtenerPorEstado(@Path("estado") estado: String): List<Sector>` en `HuertoApi`.

**Agregar una prueba**: copien una de `SectorViewModelTest` y cambien el `coEvery` (lo que responde el mock) y el `assert` (lo que se espera).

## Commits por integrante

La pauta revisa los **commits de cada integrante**, y en la defensa cada uno muestra los suyos. Una forma de repartir el trabajo:

| Carolina | Jorge |
|---|---|
| `backend/ms-usuarios` y `backend/ms-huerto` | `backend/ms-tareas` |
| `data/remote/` (Retrofit, Api, Requests) | `data/repository/` |
| ViewModels de Sector, Cultivo, Auth y Registro | ViewModels de Tarea, Problema, Panel e Historial |
| API externa de clima (`ClimaApi`, `ClimaViewModel`, `TarjetaClima`) | `AppNavigation` (recargas ON_RESUME) y `SectorFormScreen` |
| Pruebas de repositorios | Pruebas de ViewModels |
| Firma del APK (`docs/FIRMA_APK.md`) | README y capturas |

Hagan commits pequeños con mensajes claros (por ejemplo, `Agrega ms-huerto con CRUD de sectores`), y que cada uno **entienda también la parte del otro**: la defensa es individual.
