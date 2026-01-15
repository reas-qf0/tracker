package org.reas.tracker.supabase

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.scale
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import org.reas.tracker.util.Secrets
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object CustomImageStorage {
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://nmanwiuqmhhcbebwobmh.supabase.co",
        supabaseKey = Secrets.SUPABASE_KEY
    ) {
        install(Storage)
    }
    private val bucket = supabase.storage.from("images")

    @OptIn(ExperimentalUuidApi::class)
    suspend fun save(image: Uri, contentResolver: ContentResolver): String {
        // get image from uri
        val image = MediaStore.Images.Media.getBitmap(contentResolver, image)

        // resize bitmap to SIZExSIZE with cropping
        val w = image.width
        val h = image.height
        val squaredImage = Bitmap.createBitmap(
            image,
            max(0, (w - h) / 2), max(0, (h - w) / 2),
            min(w, h), min(w, h)
        )
        val resizedImage = squaredImage.scale(
            min(SIZE, min(w, h)), min(SIZE, min(w, h))
        )

        // convert bitmap to PNG
        val stream = ByteArrayOutputStream()
        resizedImage.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val content = stream.toByteArray()

        // save bitmap to supabase
        val filename = "${Uuid.random().toHexDashString()}.png"
        bucket.upload(filename, content)
        return filename
    }

    suspend fun get(filename: String): String {
        val url = bucket.createSignedUrl(filename, expiresIn = 1.minutes)
        Log.d("CustomImageStorage", url)
        return url
    }


    private const val SIZE = 512
}