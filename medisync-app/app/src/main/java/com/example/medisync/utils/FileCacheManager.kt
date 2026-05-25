package com.example.medisync.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest

object FileCacheManager {
    private const val CACHE_DIR = "medisync_file_cache"
    private val client = OkHttpClient()

    suspend fun getOrDownloadFile(
        context: Context,
        fileKey: String,
        fileName: String,
        fileType: String?,
        forceRefresh: Boolean = false,
        viewUrlProvider: suspend () -> String
    ): File = withContext(Dispatchers.IO) {
        val targetFile = targetFile(context, fileKey, fileName, fileType)
        if (!forceRefresh && targetFile.exists() && targetFile.length() > 0L) {
            return@withContext targetFile
        }

        targetFile.parentFile?.mkdirs()
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
        if (tempFile.exists()) tempFile.delete()

        val viewUrl = viewUrlProvider()
        val request = Request.Builder().url(viewUrl).get().build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("File download failed: ${response.code}")
            }

            val body = response.body ?: error("Empty file response")
            tempFile.outputStream().use { output ->
                body.byteStream().use { input ->
                    input.copyTo(output)
                }
            }
        }

        if (targetFile.exists()) targetFile.delete()
        if (!tempFile.renameTo(targetFile)) {
            tempFile.copyTo(targetFile, overwrite = true)
            tempFile.delete()
        }

        targetFile
    }

    fun contentUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun targetFile(
        context: Context,
        fileKey: String,
        fileName: String,
        fileType: String?
    ): File {
        val safeName = sanitize(fileName).ifBlank { "file" }
        val extension = safeName.substringAfterLast('.', missingDelimiterValue = "")
            .ifBlank { extensionFromMime(fileType) }
        val baseName = safeName.substringBeforeLast('.', missingDelimiterValue = safeName)
        val finalName = if (extension.isBlank()) {
            "${sha256(fileKey)}_$baseName"
        } else {
            "${sha256(fileKey)}_$baseName.$extension"
        }

        return File(File(context.filesDir, CACHE_DIR), finalName)
    }

    private fun extensionFromMime(fileType: String?): String {
        return when {
            fileType == "application/pdf" -> "pdf"
            fileType?.startsWith("image/") == true -> fileType.substringAfter('/').substringBefore('+')
            else -> ""
        }
    }

    private fun sanitize(value: String): String {
        return value.replace(Regex("[^A-Za-z0-9._-]"), "_")
            .trim('_')
            .take(120)
    }

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(24)
    }
}
