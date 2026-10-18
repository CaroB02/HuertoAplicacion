package com.example.huertoaplicaciones.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.huertoaplicaciones.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Aquí manejo el RECURSO NATIVO de las fotos (cámara y galería).
// Las fotos se guardan dentro del almacenamiento privado de la app (filesDir/fotos),
// así siguen disponibles después de cerrar y reabrir la aplicación.
// "object" = una sola instancia, sin necesidad de crearla (FotoUtils.algo() se llama directo).
object FotoUtils {

    // Devuelve la carpeta "fotos"; si todavía no existe, la crea (mkdirs).
    private fun carpeta(context: Context): File =
        File(context.filesDir, "fotos").also { if (!it.exists()) it.mkdirs() }

    // Cada foto se guarda con la hora en milisegundos en el nombre, así nunca se repite.
    private fun nuevoArchivo(context: Context): File =
        File(carpeta(context), "foto_${System.currentTimeMillis()}.jpg")

    // Prepara el archivo vacío y la Uri de FileProvider que recibirá la cámara.
    // La cámara es otra aplicación y no puede escribir en mi carpeta privada por sí sola:
    // el FileProvider (declarado en el AndroidManifest) le da un permiso temporal para ese archivo.
    fun prepararCaptura(context: Context): Pair<File, Uri> {
        val archivo = nuevoArchivo(context)
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", archivo
        )
        return archivo to uri
    }

    // Copia la imagen elegida en el Photo Picker (Uri temporal) al almacenamiento de la app.
    // El Photo Picker entrega una Uri que deja de servir más tarde, por eso copio el archivo
    // a mi carpeta: así la foto no se pierde al reiniciar la app.
    // "suspend" + Dispatchers.IO = copio el archivo en un hilo secundario para no congelar la pantalla.
    suspend fun copiarAAlmacenamiento(context: Context, origen: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                val destino = nuevoArchivo(context)
                // "use" cierra los archivos solo cuando termina (evita fugas de recursos).
                context.contentResolver.openInputStream(origen)?.use { entrada ->
                    destino.outputStream().use { salida -> entrada.copyTo(salida) }
                } ?: return@withContext null
                // Si el archivo quedó vacío, lo borro y aviso con null que falló.
                if (destino.length() == 0L) {
                    destino.delete()
                    null
                } else {
                    Uri.fromFile(destino).toString()
                }
            } catch (e: Exception) {
                // Cualquier error (sin espacio, archivo inválido...) devuelve null y la pantalla avisa.
                null
            }
        }

    fun uriDeArchivo(archivo: File): String = Uri.fromFile(archivo).toString()

    // Genera una imagen ficticia (planta de prueba) como archivo real para los datos de ejemplo.
    // Se usa un archivo y no un recurso porque así se muestra igual que cualquier foto del usuario.
    suspend fun crearFotoDemo(context: Context): String? = withContext(Dispatchers.IO) {
        try {
            val destino = File(carpeta(context), "foto_demo.png")
            // Solo la dibujo la primera vez; si ya existe la reutilizo.
            if (!destino.exists() || destino.length() == 0L) {
                val dibujo = ContextCompat.getDrawable(context, R.drawable.foto_demo)
                    ?: return@withContext null
                val lado = 480
                // Dibujo el vector (foto_demo.xml) sobre un Bitmap y lo guardo como PNG.
                val bitmap = Bitmap.createBitmap(lado, lado, Bitmap.Config.ARGB_8888)
                dibujo.setBounds(0, 0, lado, lado)
                dibujo.draw(Canvas(bitmap))
                destino.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
            Uri.fromFile(destino).toString()
        } catch (e: Exception) {
            null
        }
    }
}
