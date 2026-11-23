# HuertoAplicacion

Aplicación móvil Android para la **gestión colaborativa del huerto escolar** (DSY1105, Desarrollo de Aplicaciones Móviles).
Reemplaza la coordinación verbal por un registro central de sectores, cultivos, tareas, evidencias, problemas e historial. Los datos se guardan en **microservicios propios en Spring Boot con MySQL** y la app los consume con **Retrofit**. Además, la app muestra el pronóstico del clima de una **API externa** (Open-Meteo).

## 1. Nombre del proyecto

**HuertoAplicacion**: gestión del huerto escolar.

## 2. Integrantes

- Carolina Belén Bravo Bravo
- Jorge Alejandro Carmona González

Planificación en **Trello**: https://trello.com/b/kwsPPgBP/huerto-escolar
Código en **GitHub**: https://github.com/CaroB02/HuertoAplicacion

## 3. Funcionalidades

**Docente / tallerista (Administrador)**
- Panel con resumen de tareas, barra de avance, **clima del día con recomendación de riego** y avisos (tareas atrasadas, por validar, problemas, cosechas y riegos próximos, sensores fuera de rango).
- Sectores: listado, detalle con sus cultivos y lecturas de sensores simuladas, crear, editar y eliminar.
- Cultivos: crear, editar y eliminar, con fechas de siembra, trasplante y cosecha, frecuencia de riego y foto referencial.
- Tareas: crear, editar, asignar a un estudiante, filtrar por estado, responsable y fecha, y eliminar las pendientes.
- Revisión: validar o rechazar entregas (el rechazo exige motivo). Cada revisión queda registrada.
- Problemas: consultar reportes y cambiar su estado de atención con un comentario.
- Historial cronológico de actividades.

**Estudiante (Colaborador)**
- Panel personal con sus tareas, el clima y avisos.
- Crear cuenta desde el login (estudiante, o docente con código de autorización).
- Mis tareas: ver instrucciones, iniciar y entregar con comentario y fotografía (galería o cámara); reenviar si fue rechazada.
- Reportar problemas con descripción y foto, y ver solo sus reportes.
- Historial propio.

**Flujo de una tarea:** Pendiente → En desarrollo → Pendiente de validación → Validada / Rechazada. «Atrasada» se calcula al mostrar.

## 4. Endpoints utilizados

### Arquitectura

```
App Android (Kotlin + Compose, MVVM)
 ├── Retrofit ──► ms-usuarios  :8081 ──► MySQL huerto_usuarios
 ├── Retrofit ──► ms-huerto    :8082 ──► MySQL huerto_huerto
 │                    └── RestClient ──► ms-tareas (¿hay tareas asociadas antes de borrar?)
 ├── Retrofit ──► ms-tareas    :8083 ──► MySQL huerto_tareas
 └── Retrofit ──► API externa Open-Meteo (https://api.open-meteo.com)
```

Desde el emulador, los microservicios se llaman con `http://10.0.2.2:<puerto>/`, que es el `localhost` del PC.

### Microservicio ms-usuarios (puerto 8081)

| Método | Endpoint | Uso en la app |
|---|---|---|
| GET | `/usuarios` | Nombres de las personas en tareas, reportes e historial |
| GET | `/usuarios/estudiantes` | Elegir a quién asignar una tarea |
| GET | `/usuarios/{id}` | Validar que el responsable exista y sea estudiante |
| GET | `/usuarios/buscar?correo=` | Avisar si el correo ya está registrado (404 si no existe) |
| POST | `/usuarios` | Crear cuenta (409 si el correo ya existe) |
| POST | `/usuarios/login` | Iniciar sesión (401 si el correo o la clave no coinciden) |

### Microservicio ms-huerto (puerto 8082)

| Método | Endpoint | Uso en la app |
|---|---|---|
| GET | `/sectores` · `/sectores/{id}` | Listado, detalle y edición de sectores |
| POST | `/sectores` | Crear sector |
| PUT | `/sectores/{id}` | Editar sector |
| DELETE | `/sectores/{id}` | Eliminar (409 si tiene cultivos, tareas o reportes) |
| GET | `/cultivos` · `/cultivos?sectorId=` · `/cultivos/{id}` | Listado y edición de cultivos |
| POST | `/cultivos` | Crear cultivo (400 si el sector no existe o las fechas no cuadran) |
| PUT | `/cultivos/{id}` | Editar cultivo |
| DELETE | `/cultivos/{id}` | Eliminar (409 si tiene tareas o reportes) |
| GET | `/lecturas` | Lecturas simuladas de sensores |
| POST | `/lecturas` | Registrar una lectura simulada (para pruebas con Postman) |

### Microservicio ms-tareas (puerto 8083)

| Método | Endpoint | Uso en la app |
|---|---|---|
| GET | `/tareas` · `/tareas?estudianteId=` · `/tareas/{id}` | Listados y detalle |
| POST | `/tareas` | Crear tarea (siempre parte en «Pendiente») |
| PUT | `/tareas/{id}` | Editar o iniciar una tarea |
| DELETE | `/tareas/{id}` | Eliminar (409 si ya se inició) |
| POST | `/tareas/{id}/entrega` | El estudiante entrega con comentario y fotos (transacción) |
| POST | `/tareas/{id}/validaciones` | El docente valida o rechaza (transacción) |
| GET | `/tareas/{id}/evidencias` | Fotos de la tarea |
| GET | `/tareas/{id}/validaciones` · `/validaciones` | Historial de revisiones |
| GET | `/problemas` · `/problemas?usuarioId=` · `/problemas/{id}` | Reportes |
| POST | `/problemas` | Reportar un problema |
| PUT | `/problemas/{id}` | Cambiar el estado de atención |
| GET | `/referencias?sectorId=&cultivoId=` | **Interno**: lo consulta ms-huerto antes de borrar |

### API externa: Open-Meteo

| Método | Endpoint | Uso en la app |
|---|---|---|
| GET | `https://api.open-meteo.com/v1/forecast?latitude=-33.45&longitude=-70.66&current=...&daily=...` | Clima actual y pronóstico de 3 días en el panel, con una recomendación de riego |

Es gratuita y no necesita clave. Se consume con su propia instancia de Retrofit (`ClimaApi`), separada de los microservicios: si no hay internet, solo la tarjeta del clima muestra el error y el resto de la app sigue funcionando.

**Diferencia entre la API externa y los microservicios:** los microservicios son nuestros: definimos sus endpoints, su base de datos y sus reglas, y la app hace CRUD completo (GET, POST, PUT, DELETE). La API externa no es nuestra: solo la consultamos con GET, no podemos cambiar su formato (por eso usamos `@SerializedName` para traducir `temperature_2m` → `temperatura`) y dependemos de su disponibilidad.

## 5. Pasos para ejecutar

### Requisitos
- Android Studio (con un emulador, por ejemplo Pixel 8 API 36).
- JDK 17 o superior.
- MySQL en ejecución (Laragon o XAMPP) con el usuario `root` sin contraseña. Si tienen contraseña, cámbienla en el `application.properties` de cada microservicio.
- IntelliJ IDEA o VS Code (con Spring Boot Extension Pack) para los microservicios.

### a) Base de datos
Enciendan MySQL. **No hace falta crear las bases**: cada microservicio crea la suya (`huerto_usuarios`, `huerto_huerto`, `huerto_tareas`) y sus tablas al arrancar, y carga datos de prueba si están vacías.

### b) Microservicios (en este orden)
Abran cada carpeta de `backend/` como proyecto Maven y ejecuten su clase `...Application`:

1. `backend/ms-usuarios`: `UsuariosApplication` (puerto 8081)
2. `backend/ms-huerto`: `HuertoApplication` (puerto 8082)
3. `backend/ms-tareas`: `TareasApplication` (puerto 8083)

Con Maven instalado también se puede usar `mvn spring-boot:run` dentro de cada carpeta.
Para comprobarlo, abran en el navegador `http://localhost:8082/sectores`: debe aparecer un JSON con 3 sectores.

> Si reinician los datos de prueba, borren **las tres** bases (`DROP DATABASE huerto_usuarios;` etc.), porque los datos de un microservicio usan los id de los otros.

### c) App móvil
1. Abran la carpeta del proyecto en Android Studio y esperen la sincronización de Gradle.
2. Ejecuten `app` en el emulador.
3. **Teléfono físico:** cambien `HOST` en `data/remote/ApiConfig.kt` por la IP del PC en la red Wi-Fi y agreguen esa IP en `res/xml/network_security_config.xml`.

### d) Pruebas unitarias y cobertura (JUnit + Kover)
```bash
./gradlew testDebugUnitTest        # ejecuta las pruebas
./gradlew koverHtmlReportDebug     # genera el reporte de cobertura con Kover
./gradlew koverVerifyDebug         # comprueba que la cobertura sea de al menos 80%
```
En Android Studio: panel **Gradle > Execute Gradle Task** y escribir el nombre de la tarea.
El reporte queda en `app/build/reports/kover/htmlDebug/index.html`.

Del cálculo se excluyen las clases que no se pueden probar con pruebas unitarias: las pantallas de Compose (`ui.*`), `MainActivity` y `FotoUtils` (cámara y archivos). Se verifican ejecutando la app. La configuración está en el bloque `kover { }` de `app/build.gradle.kts`.

![Cobertura](docs/capturas/cobertura.png)

Las pruebas usan **JUnit 4 + MockK + kotlinx-coroutines-test**. Se mockean los repositorios (para probar los ViewModels) y las interfaces de Retrofit (para probar los repositorios), así no se necesita el servidor ni internet.

### Cuentas de prueba
Todas usan la clave `123456` (también hay botones de acceso rápido en el login). Los nombres son inventados.

| Rol | Correo |
|---|---|
| Docente (Carolina Muñoz Soto) | carolina.munoz@huerta.cl |
| Estudiante (Matías Fuentes Rojas) | matias.fuentes@huerta.cl |
| Estudiante (Valentina Pérez Díaz) | valentina.perez@huerta.cl |
| Estudiante (Benjamín Castro Vera) | benjamin.castro@huerta.cl |

Para crear una cuenta de docente se pide el código `HUERTO2026` (definido en `util/Constantes.kt`).

## 6. APK firmado y llave .jks

El proceso completo está en [`docs/FIRMA_APK.md`](docs/FIRMA_APK.md). La llave queda en `keystore/huerto-release.jks` y su configuración en `keystore.properties` y en `signingConfigs` de `app/build.gradle.kts`.

![APK firmado](docs/capturas/apk-firmado.png)
![Llave .jks](docs/capturas/jks.png)

## Tecnologías

**App:** Kotlin · Jetpack Compose · Material Design 3 · Navigation Compose · MVVM con StateFlow · Retrofit 3 + Gson · OkHttp (registro de peticiones) · Coil 3 · Activity Result API (Photo Picker y cámara) · FileProvider.
**Backend:** Java 17 · Spring Boot 3.5 · Spring Web · Spring Data JPA (Hibernate) · Bean Validation · MySQL · RestClient (comunicación entre microservicios).
**Pruebas:** JUnit 4 · MockK · kotlinx-coroutines-test · Kover.

## Estructura

```
app/src/main/java/.../
  data/entity/      Modelos (Usuario, Sector, Cultivo, Tarea...) que llegan como JSON
  data/remote/      Retrofit: ApiConfig, RetrofitClient, UsuariosApi, HuertoApi, TareasApi,
                    ClimaApi, Requests (modelos de escritura) y Respuestas (manejo de errores HTTP)
  data/repository/  Repositorios: llaman a las Api y guardan la última lista en un StateFlow
  viewmodel/        Estado de cada pantalla con StateFlow
  ui/               screens, components (incluye TarjetaClima), navigation, theme
  util/             Validaciones, fechas, constantes, avisos, clima, fotos, mensajes
app/src/test/       Pruebas unitarias de repositorios y ViewModels
backend/
  ms-usuarios/      model · repository · service · controller · dto · config
  ms-huerto/        model · repository · service (+ TareasClient) · controller · config
  ms-tareas/        model · repository · service · controller · dto · config
```

## Notas sobre fotos y permisos
- La app declara el permiso `INTERNET` para Retrofit, y `network_security_config.xml` permite HTTP solo hacia `10.0.2.2` (los microservicios locales).
- El **Photo Picker** no requiere permisos; la **cámara** se abre con la app de cámara del sistema.
- Las fotos se guardan en el almacenamiento privado del teléfono y en el servidor se guarda su **ruta**. Por eso una foto se ve en el teléfono que la tomó (proyecto académico; en producción se subiría el archivo a un almacenamiento en la nube).
