import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Kover: mide qué porcentaje del código ejecutan las pruebas unitarias (JUnit)
    alias(libs.plugins.kover)
}

// Datos de la llave de firma (.jks). Se leen del archivo keystore.properties en la raíz del proyecto
// para no escribir las contraseñas directamente aquí. Ver docs/FIRMA_APK.md.
val propiedadesFirma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

android {
    namespace = "com.example.huertoaplicaciones"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.huertoaplicaciones"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // FIRMA DEL APK: la llave huerto-release.jks firma la versión release de la app
    signingConfigs {
        create("release") {
            if (propiedadesFirma.isNotEmpty()) {
                storeFile = rootProject.file(propiedadesFirma.getProperty("storeFile"))
                storePassword = propiedadesFirma.getProperty("storePassword")
                keyAlias = propiedadesFirma.getProperty("keyAlias")
                keyPassword = propiedadesFirma.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        // Si una prueba toca una clase de Android sin querer, devuelve un valor por defecto en vez de fallar
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)

    // Retrofit: consumo de los microservicios y de la API externa de clima
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Pruebas unitarias de ViewModels y repositorios
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

// COBERTURA DE PRUEBAS CON KOVER (exigencia: 80% como mínimo)
// Comandos (panel Gradle > Execute Gradle Task):
//   koverHtmlReportDebug  -> ejecuta las pruebas y genera el reporte
//                            app/build/reports/kover/htmlDebug/index.html
//   koverVerifyDebug      -> falla si la cobertura queda bajo 80%
//
// Se EXCLUYEN las clases que no se pueden probar con pruebas unitarias (corren en la JVM, sin Android):
//  - ui.* (pantallas, componentes, navegación y tema de Jetpack Compose): necesitan un
//    dispositivo para dibujarse; se verifican ejecutando la app en el emulador.
//  - MainActivity: solo arranca la app y crea los ViewModels.
//  - FotoUtils: usa la cámara y los archivos del teléfono (Context de Android).
//  - Funciones @Composable y clases que genera Compose automáticamente.
kover {
    reports {
        filters {
            excludes {
                classes(
                    "com.example.huertoaplicaciones.ui.*",
                    "com.example.huertoaplicaciones.MainActivity*",
                    "com.example.huertoaplicaciones.util.FotoUtils*",
                    "*ComposableSingletons*",
                    "*.BuildConfig",
                    "*.R",
                    "*.R$*"
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
        verify {
            rule {
                minBound(80)
            }
        }
    }
}
