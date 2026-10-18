# HuertoAplicacion

Aplicación móvil Android para la **gestión colaborativa del huerto escolar** (asignatura DSY1105, Desarrollo de Aplicaciones Móviles).
Reemplaza la coordinación verbal por un registro central de sectores, cultivos, tareas, evidencias, problemas e historial.

## Integrantes

- _Integrante 1: completar nombre_
- _Integrante 2: completar nombre_

## Planificación y control de versiones

- **Trello (planificación y distribución de tareas):** _pegar aquí el enlace del tablero_
- **GitHub (código y commits):** https://github.com/CaroB02/HuertoAplicacion

El tablero de Trello está compartido con el docente y con ambos integrantes.

## Funcionalidades

**Docente / tallerista (Administrador)**
- Dashboard con resumen de tareas, barra de avance y avisos (atrasadas, por validar, problemas, cosechas y riegos próximos).
- Sectores: listado, detalle con sus cultivos y datos de sensores simulados, crear, editar y eliminar.
- Cultivos: crear, editar y eliminar, con fechas de siembra, trasplante y cosecha, frecuencia de riego y foto referencial.
- Tareas: crear, editar, asignar a un estudiante, filtrar por estado, responsable y fecha, eliminar las pendientes.
- Revisión: validar o rechazar entregas (el rechazo exige un motivo). Cada revisión queda registrada con quién, cuándo y resultado.
- Problemas reportados: consultar y cambiar el estado de atención con comentario.
- Historial cronológico de actividades.

**Estudiante (Colaborador)**
- Dashboard personal con sus tareas y avisos.
- Crear cuenta desde el login (estudiante o docente). Para crear una cuenta de docente se pide un código de autorización.
- Mis tareas: ver instrucciones, sector, cultivo y fecha; iniciar la tarea; entregarla con comentario y fotografía (galería o cámara).
- Reenviar una tarea rechazada después de corregirla.
- Reportar problemas con descripción y foto, y consultar solo sus reportes.
- Historial propio.

**Flujo de estados de una tarea:** Pendiente → En desarrollo → Pendiente de validación → Validada / Rechazada (una rechazada vuelve a entregarse). «Atrasada» se calcula al mostrar, según la fecha programada.

## Tecnologías

Kotlin · Jetpack Compose · Material Design 3 · Navigation Compose · Room (SQLite) + KSP · StateFlow / ViewModel (MVVM) · Coil 3 · Activity Result API (Photo Picker y cámara) · FileProvider.

## Arquitectura

```
data/        Room: entity, dao, repository, AppDatabase, SeedData
viewmodel/   Estado de pantallas con StateFlow (Auth, Sector, Cultivo, Tarea, Problema, Panel, Historial)
ui/          screens, components, navigation, theme (colores y tipografía centralizados)
util/        Validaciones, fechas, constantes, avisos/recordatorios, fotos, mensajes
```

Modelo relacional con claves foráneas: `Usuario`, `Sector`, `Cultivo`, `Tarea`, `Evidencia`, `Validacion`, `Problema`, `LecturaSensor`.
La tabla `Tarea` conserva el resultado de la última revisión para consulta rápida; el detalle completo está en `Validacion`.

## Cuentas de prueba

Todas usan la clave `123456` (también hay botones de acceso rápido en el login).
Los nombres son inventados: el caso exige trabajar solo con datos ficticios.

### Crear cuentas nuevas

En el login, el botón «¿No tienes cuenta? Crear cuenta» abre el registro.

- **Estudiante:** nombre y apellido, correo `.cl`, contraseña de 8 o más caracteres, y su grupo.
- **Docente:** lo mismo, más un **código de autorización** (para la demostración es `HUERTO2026`, definido en `util/Constantes.kt`). Se pide porque una cuenta de docente puede administrar el huerto y validar tareas.
- No se permiten dos cuentas con el mismo correo.

| Rol | Correo |
|---|---|
| Docente (Carolina Muñoz Soto) | carolina.munoz@huerta.cl |
| Estudiante (Matías Fuentes Rojas) | matias.fuentes@huerta.cl |
| Estudiante (Valentina Pérez Díaz) | valentina.perez@huerta.cl |
| Estudiante (Benjamín Castro Vera) | benjamin.castro@huerta.cl |

Al primer arranque se cargan sectores, cultivos, tareas en todos los estados, evidencias, problemas y lecturas de sensores ficticias.

## Cómo ejecutar

1. Abrir la carpeta en Android Studio y esperar la sincronización de Gradle.
2. Elegir un emulador (por ejemplo Pixel 8, API 36) o un dispositivo con depuración USB.
3. Ejecutar `app`. También por consola: `./gradlew installDebug`.
4. Pruebas unitarias: `./gradlew testDebugUnitTest`.

Si ya tenías una versión anterior instalada, la base de datos se recrea automáticamente (el esquema cambió a la versión 2).

## Notas sobre fotos y permisos

- El **Photo Picker** no requiere permisos.
- La **cámara** se abre con la aplicación de cámara del sistema, por lo que la app no declara el permiso `CAMERA`.
- Las fotos se copian a `files/fotos` (almacenamiento privado) y se guardan sus rutas en Room, así que persisten al reiniciar la app.
- Si el usuario cancela la cámara o el selector, no se guarda nada y se muestra un aviso.
