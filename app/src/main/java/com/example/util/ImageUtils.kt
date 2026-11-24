package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {

    const val SAMPLE_IMG_KOPI_AREN = "drawable://img_prod_kopi_aren"
    const val SAMPLE_IMG_ROTI_BAKAR = "drawable://img_prod_roti_bakar"
    const val SAMPLE_IMG_BERAS_PREMIUM = "drawable://img_prod_beras_premium"
    const val SAMPLE_IMG_MINYAK_GORENG = "drawable://img_prod_minyak_goreng"

    /**
     * Resolves a stored Product.imageUri into a Coil-compatible data model.
     * Bundled sample product URIs map directly to @DrawableRes Int IDs so they
     * render reliably across builds and never get stripped by resource shrinking.
     */
    fun resolveProductImageModel(imageUri: String?): Any? {
        if (imageUri.isNullOrBlank()) return null
        return resolveBundledDrawableRes(imageUri) ?: imageUri
    }

    private fun resolveBundledDrawableRes(imageUri: String): Int? {
        return when (imageUri) {
            SAMPLE_IMG_KOPI_AREN -> R.drawable.img_prod_kopi_aren
            SAMPLE_IMG_ROTI_BAKAR -> R.drawable.img_prod_roti_bakar
            SAMPLE_IMG_BERAS_PREMIUM -> R.drawable.img_prod_beras_premium
            SAMPLE_IMG_MINYAK_GORENG -> R.drawable.img_prod_minyak_goreng
            else -> null
        }
    }

    /**
     * Copies an image from a content URI into app's private internal storage
     * so it never expires when the app is closed or restarted.
     * Returns the absolute file path.
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri, filenamePrefix: String): String? {
        return try {
            val imagesDir = File(context.filesDir, "app_images").apply { mkdirs() }
            val destFile = File(imagesDir, "${filenamePrefix}_${System.currentTimeMillis()}.jpg")

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap != null) {
                    val fos = FileOutputStream(destFile)
                    // Scale down slightly if massive to preserve memory & fast loading
                    val scaled = if (bitmap.width > 1200 || bitmap.height > 1200) {
                        val ratio = minOf(1200f / bitmap.width, 1200f / bitmap.height)
                        Bitmap.createScaledBitmap(
                            bitmap,
                            (bitmap.width * ratio).toInt(),
                            (bitmap.height * ratio).toInt(),
                            true
                        )
                    } else {
                        bitmap
                    }
                    scaled.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                    fos.flush()
                    fos.close()
                    destFile.absolutePath
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Loads a Bitmap from a bundled drawable URI, absolute file path, or Uri string safely
     */
    fun loadBitmap(context: Context, pathOrUri: String?): Bitmap? {
        if (pathOrUri.isNullOrBlank()) return null
        return try {
            val bundledRes = resolveBundledDrawableRes(pathOrUri)
            if (bundledRes != null) {
                return BitmapFactory.decodeResource(context.resources, bundledRes)
            }
            val file = File(pathOrUri)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                val uri = Uri.parse(pathOrUri)
                val stream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(stream)
                stream?.close()
                bmp
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
