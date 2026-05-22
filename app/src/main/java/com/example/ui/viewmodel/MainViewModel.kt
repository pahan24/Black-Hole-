package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = VideoRepository(database.videoDao())
    private val downloader = VideoDownloader(application)
    private val resolver = UrlResolver()

    val inputUrl = MutableStateFlow("")
    val isExtracting = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    val downloadProgress = MutableStateFlow(0.0f)
    val downloadSpeedMBs = MutableStateFlow(0.0)
    val downloadBytesFormatted = MutableStateFlow("")
    val downloadStatus = MutableStateFlow(DownloadStatus.IDLE)
    val currentDownloadingName = MutableStateFlow("")

    private var activeJob: Job? = null

    val allVideos: StateFlow<List<DownloadedVideo>> = repository.allVideos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentPlayingVideo = MutableStateFlow<DownloadedVideo?>(null)

    fun onUrlChange(newUrl: String) {
        inputUrl.value = newUrl
        errorMessage.value = null
    }

    fun clearErrorMessage() {
        errorMessage.value = null
    }

    fun pasteFromClipboard() {
        try {
            val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val pasteText = clip.getItemAt(0).text?.toString() ?: ""
                if (pasteText.startsWith("http")) {
                    inputUrl.value = pasteText
                    errorMessage.value = null
                } else {
                    errorMessage.value = "Clipboard does not contain a valid URL."
                }
            } else {
                errorMessage.value = "Clipboard is empty."
            }
        } catch (e: Exception) {
            errorMessage.value = "Unable to read clipboard content."
        }
    }

    fun startDownloadFlow() {
        val url = inputUrl.value.trim()
        if (url.isEmpty()) {
            errorMessage.value = "Please enter or paste a valid link."
            return
        }

        errorMessage.value = null
        activeJob?.cancel()

        activeJob = viewModelScope.launch {
            try {
                isExtracting.value = true
                downloadStatus.value = DownloadStatus.EXTRACTING

                val metadata = withContext(Dispatchers.IO) {
                    resolver.resolveVideoMetadata(url)
                }

                currentDownloadingName.value = metadata.title
                downloadStatus.value = DownloadStatus.DOWNLOADING
                isExtracting.value = false

                downloader.downloadVideo(metadata.downloadUrl, metadata.fileName)
                    .collect { progressState ->
                        when (progressState) {
                            is DownloadProgressState.Preparing -> {
                                downloadProgress.value = 0.0f
                                downloadSpeedMBs.value = 0.0
                                downloadBytesFormatted.value = "Initiating gravity tunnel..."
                            }
                            is DownloadProgressState.Downloading -> {
                                downloadProgress.value = progressState.progress
                                downloadSpeedMBs.value = progressState.speedMBs
                                val downloadedSize = formatSize(progressState.downloadedBytes)
                                val totalSize = if (progressState.totalBytes > 0) formatSize(progressState.totalBytes) else "Unknown"
                                downloadBytesFormatted.value = "$downloadedSize / $totalSize"
                            }
                            is DownloadProgressState.Success -> {
                                downloadProgress.value = 1.0f
                                downloadStatus.value = DownloadStatus.COMPLETED

                                val videoRecord = DownloadedVideo(
                                    originalUrl = metadata.originalUrl,
                                    downloadUrl = metadata.downloadUrl,
                                    title = metadata.title,
                                    fileName = metadata.fileName,
                                    filePath = progressState.filePath,
                                    fileSize = progressState.fileSize,
                                    durationSeconds = metadata.durationSeconds,
                                    platform = metadata.platform,
                                    status = DownloadStatus.COMPLETED,
                                    progress = 1.0f
                                )
                                repository.insertVideo(videoRecord)

                                inputUrl.value = ""
                                resetDownloaderStatusDelayed()
                            }
                            is DownloadProgressState.Failed -> {
                                errorMessage.value = progressState.error
                                downloadStatus.value = DownloadStatus.FAILED
                                resetDownloaderStatusDelayed()
                            }
                        }
                    }

            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Extraction failed."
                downloadStatus.value = DownloadStatus.FAILED
                isExtracting.value = false
                resetDownloaderStatusDelayed()
            }
        }
    }

    private fun resetDownloaderStatusDelayed() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(6000)
            if (downloadStatus.value == DownloadStatus.COMPLETED || downloadStatus.value == DownloadStatus.FAILED) {
                downloadStatus.value = DownloadStatus.IDLE
                downloadProgress.value = 0.0f
                downloadSpeedMBs.value = 0.0
                downloadBytesFormatted.value = ""
                currentDownloadingName.value = ""
            }
        }
    }

    fun deleteVideo(video: DownloadedVideo) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val file = java.io.File(video.filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                repository.deleteVideo(video)
            } catch (e: Exception) {
                repository.deleteVideo(video)
            }
        }
    }

    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}
