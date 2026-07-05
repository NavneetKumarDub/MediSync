package com.example.medisync.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest

class ProfilePhotoFileStore(
    context: Context,
    private val client: OkHttpClient = OkHttpClient()
) {
    private val directory =
        File(context.applicationContext.filesDir, "profile_photos").apply {
            mkdirs()
        }

    suspend fun downloadAndReplace(
        userId: Int,
        objectKey: String,
        viewUrl: String,
        previousFile: File?
    ): File = withContext(Dispatchers.IO) {
        val extension = objectKey.substringAfterLast('.', "jpg")
        val version = hash(objectKey)
        val target = File(directory, "${userId}_$version.$extension")
        val temporary = File(directory, "${target.name}.tmp")

        if (target.exists() && target.length() > 0L) {
            return@withContext target
        }

        val request = Request.Builder().url(viewUrl).get().build()

        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) {
                "Photo download failed: ${response.code}"
            }

            val body = response.body ?: error("Empty photo response")

            temporary.outputStream().use { output ->
                body.byteStream().use { input ->
                    input.copyTo(output)
                }
            }
        }

        check(temporary.renameTo(target)) {
            "Unable to save profile photo"
        }

        if (previousFile != null && previousFile != target) {
            previousFile.delete()
        }

        target
    }

    fun delete(file: File) {
        if (file.exists()) file.delete()
    }

    private fun hash(value: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(16)
    }
}