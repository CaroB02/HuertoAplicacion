# Cómo generar el APK firmado

La pauta pide el APK firmado con una llave `.jks`, y en la defensa hay que explicar el proceso paso a paso.

## Qué es la firma

Android solo instala aplicaciones firmadas digitalmente. La firma demuestra quién publicó la app y garantiza que nadie la modificó después. Las versiones de prueba (debug) se firman solas con una llave de depuración. Para publicar se necesita una llave propia, guardada en un **keystore** (archivo `.jks`).

Un keystore contiene:

- **La contraseña del almacén** (`storePassword`), que protege el archivo completo.
- **Un alias** (`keyAlias`), que es el nombre de la llave dentro del almacén.
- **La contraseña de la llave** (`keyPassword`).
- **Los datos del certificado**: nombre, organización y país, más su validez en años.

## Paso 1: crear la llave (una sola vez)

1. En Android Studio, abran **Build > Generate Signed App Bundle / APK…**
2. Elijan **APK** y presionen **Next**.
3. En *Key store path* presionen **Create new…** y completen:
   - **Key store path:** `<carpeta del proyecto>/keystore/huerto-release.jks`
   - **Password / Confirm:** `Huerto2026`
   - **Alias:** `huerto`
   - **Password de la llave:** `Huerto2026`
   - **Validity (years):** 25
   - **Certificate:** sus nombres, `Duoc UC`, ciudad y `CL`
4. Presionen **OK**. El archivo `keystore/huerto-release.jks` queda creado.

> Las contraseñas y el alias deben ser los mismos que están en `keystore.properties`, porque Gradle los lee de ahí.

## Paso 2: generar el APK firmado

**Opción A, con el asistente:** continúen en la misma ventana, elijan la variante **release** y presionen **Create**. El APK queda en `app/release/app-release.apk`.

**Opción B, con Gradle** (usa la configuración de `app/build.gradle.kts`):

```bash
./gradlew assembleRelease
```

El APK queda en `app/build/outputs/apk/release/app-release.apk`.

## Paso 3: comprobar que quedó firmado

```bash
# apksigner viene en el SDK de Android: <SDK>/build-tools/<versión>/apksigner
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

Debe mostrar el certificado con los datos que ingresaron en el paso 1.

## Cómo está configurado en el proyecto

En `app/build.gradle.kts`:

```kotlin
// 1) Se leen los datos de la llave desde keystore.properties
val propiedadesFirma = Properties().apply { ... }

android {
    // 2) Se declara la configuración de firma "release"
    signingConfigs {
        create("release") {
            storeFile = rootProject.file(propiedadesFirma.getProperty("storeFile"))
            storePassword = propiedadesFirma.getProperty("storePassword")
            keyAlias = propiedadesFirma.getProperty("keyAlias")
            keyPassword = propiedadesFirma.getProperty("keyPassword")
        }
    }
    // 3) La variante release usa esa firma
    buildTypes {
        release { signingConfig = signingConfigs.getByName("release") }
    }
}
```

## Para el README (punto 6 de la pauta)

Saquen dos capturas y guárdenlas en `docs/capturas/`:

1. `apk-firmado.png`: la carpeta con `app-release.apk`, o el resultado de `apksigner verify`.
2. `jks.png`: la ventana de creación de la llave, o la carpeta `keystore/` con el `.jks`.

> En un proyecto real, el `.jks` y `keystore.properties` **nunca** se suben a GitHub: quien tenga la llave puede publicar actualizaciones falsas de la app. Aquí se suben solo porque la pauta lo exige.
