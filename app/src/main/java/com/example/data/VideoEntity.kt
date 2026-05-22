package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VideoPlatform {
    INSTAGRAM, TIKTOK, YOUTUBE, FACEBOOK, OTHER
}

enum class DownloadStatus {
    IDLE, EXTRACTING, DOWNLOADING, COMPLETED, FAILED
}

@Entity(tableName = "downloaded_videos")
data class DownloadedVideo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val originalUrl: String,
    val downloadUrl: String,
    val title: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val durationSeconds: Int = 0,
    val platform: VideoPlatform,
    val timestamp: Long = System.currentTimeMillis(),
    val status: DownloadStatus = DownloadStatus.COMPLETED,
    val progress: Float = 1.0f
)
