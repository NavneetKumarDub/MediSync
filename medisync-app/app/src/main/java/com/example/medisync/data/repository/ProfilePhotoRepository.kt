package com.example.medisync.data.repository

import com.example.medisync.data.local.ProfilePhotoCacheDao
import com.example.medisync.data.local.ProfilePhotoCacheEntity
import com.example.medisync.networks.ApiService
import com.example.medisync.utils.ProfilePhotoFileStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class ProfilePhotoRepository(
    private val api: ApiService,
    private val dao: ProfilePhotoCacheDao,
    private val fileStore: ProfilePhotoFileStore
) {
    private val userLocks = ConcurrentHashMap<Int, Mutex>()

    fun observePhoto(userId: Int): Flow<File?> {
        return dao.observePhoto(userId).map { cached ->
            cached?.localPath
                ?.let(::File)
                ?.takeIf { file ->
                    file.exists() && file.length() > 0L
                }
        }
    }

    suspend fun getCachedPhoto(userId: Int): File? {
        return dao.getPhoto(userId)
            ?.localPath
            ?.let(::File)
            ?.takeIf { file ->
                file.exists() && file.length() > 0L
            }
    }

    suspend fun refresh(
        userId: Int,
        token: String,
        expectedPhotoKey: String? = null,
        force: Boolean = false
    ): File? {
        if (userId <= 0 || token.isBlank()) {
            return getCachedPhoto(userId)
        }

        val mutex = userLocks.getOrPut(userId) { Mutex() }

        return mutex.withLock {
            val cached = dao.getPhoto(userId)

            val cachedFile = cached
                ?.localPath
                ?.let(::File)
                ?.takeIf { file ->
                    file.exists() && file.length() > 0L
                }

            val expectedKeyMatches =
                expectedPhotoKey != null &&
                        cached?.photoKey == expectedPhotoKey

            if (!force && expectedKeyMatches && cachedFile != null) {
                return@withLock cachedFile
            }

            try {
                val response = api.getProfilePhotoUrl(
                    token = "Bearer $token",
                    userId = userId
                )

                val serverPhotoKey = response.photoKey

                val cachedVersionMatches =
                    cached?.photoKey == serverPhotoKey

                if (
                    !force &&
                    cachedVersionMatches &&
                    cachedFile != null
                ) {
                    return@withLock cachedFile
                }

                val downloadedFile = fileStore.downloadAndReplace(
                    userId = userId,
                    objectKey = serverPhotoKey,
                    viewUrl = response.viewUrl,
                    previousFile = cachedFile
                )

                dao.upsertPhoto(
                    ProfilePhotoCacheEntity(
                        userId = userId,
                        photoKey = serverPhotoKey,
                        localPath = downloadedFile.absolutePath,
                        cachedAt = System.currentTimeMillis()
                    )
                )

                downloadedFile
            } catch (error: CancellationException) {
                throw error
            } catch (error: HttpException) {
                if (error.code() == 404) {
                    cachedFile?.let(fileStore::delete)
                    dao.deletePhoto(userId)
                    null
                } else {
                    cachedFile
                }
            } catch (error: Exception) {
                cachedFile
            }
        }
    }

    suspend fun onPhotoChanged(
        userId: Int,
        token: String
    ): File? {
        return refresh(
            userId = userId,
            token = token,
            expectedPhotoKey = null,
            force = true
        )
    }

    suspend fun onPhotoDeleted(userId: Int) {
        if (userId <= 0) return

        val mutex = userLocks.getOrPut(userId) { Mutex() }

        mutex.withLock {
            val cached = dao.getPhoto(userId)

            cached?.localPath
                ?.let(::File)
                ?.let(fileStore::delete)

            dao.deletePhoto(userId)
        }
    }
}