package com.example.data

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class VideoDownloader(private val context: Context) {
    private val client = OkHttpClient.Builder().build()

    fun downloadVideo(videoUrl: String, fileName: String): Flow<DownloadProgressState> = flow {
        emit(DownloadProgressState.Preparing)
        try {
            val request = Request.Builder().url(videoUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    emit(DownloadProgressState.Failed("Server responded with error: ${response.code}"))
                    return@flow
                }

                val body = response.body
                if (body == null) {
                    emit(DownloadProgressState.Failed("Response content is empty"))
                    return@flow
                }

                val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: File(context.filesDir, "Downloads")
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val targetFile = File(downloadsDir, fileName)
                val totalBytes = body.contentLength()
                var downloadedBytes = 0L

                body.byteStream().use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        val startTime = System.currentTimeMillis()
                        var lastUpdate = System.currentTimeMillis()

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            val now = System.currentTimeMillis()
                            if (now - lastUpdate > 150 || downloadedBytes == totalBytes) {
                                val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0.0f
                                val elapsedSeconds = (now - startTime) / 1000.0
                                val speedBytesPerSec = if (elapsedSeconds > 0) downloadedBytes / elapsedSeconds else 0.0
                                val speedMB = speedBytesPerSec / (1024 * 1024)

                                emit(DownloadProgressState.Downloading(
                                    progress = progress,
                                    downloadedBytes = downloadedBytes,
                                    totalBytes = totalBytes,
                                    speedMBs = speedMB,
                                    filePath = targetFile.absolutePath
                                ))
                                lastUpdate = now
                            }
                        }
                        outputStream.flush()
                    }
                }

                emit(DownloadProgressState.Success(
                    filePath = targetFile.absolutePath,
                    fileSize = targetFile.length()
                ))
            }
        } catch (e: Exception) {
            emit(DownloadProgressState.Failed(e.message ?: "Unknown network error during extraction"))
        }
    }.flowOn(Dispatchers.IO)
}

sealed interface DownloadProgressState {
    object Preparing : DownloadProgressState
    data class Downloading(
        val progress: Float,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val speedMBs: Double,
        val filePath: String
    ) : DownloadProgressState
    data class Success(val filePath: String, val fileSize: Long) : DownloadProgressState
    data class Failed(val error: String) : DownloadProgressState
}
