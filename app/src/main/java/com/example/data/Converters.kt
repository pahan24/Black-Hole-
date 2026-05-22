package com.example.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromPlatform(platform: VideoPlatform): String {
        return platform.name
    }

    @TypeConverter
    fun toPlatform(value: String): VideoPlatform {
        return try {
            VideoPlatform.valueOf(value)
        } catch (e: Exception) {
            VideoPlatform.OTHER
        }
    }

    @TypeConverter
    fun fromStatus(status: DownloadStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(value: String): DownloadStatus {
        return try {
            DownloadStatus.valueOf(value)
        } catch (e: Exception) {
            DownloadStatus.COMPLETED
        }
    }
}
