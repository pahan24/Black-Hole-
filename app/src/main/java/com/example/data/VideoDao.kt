package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM downloaded_videos ORDER BY timestamp DESC")
    fun getAllVideos(): Flow<List<DownloadedVideo>>

    @Query("SELECT * FROM downloaded_videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): DownloadedVideo?

    @Query("SELECT * FROM downloaded_videos WHERE originalUrl = :url LIMIT 1")
    suspend fun getVideoByOriginalUrl(url: String): DownloadedVideo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: DownloadedVideo): Long

    @Update
    suspend fun updateVideo(video: DownloadedVideo)

    @Delete
    suspend fun deleteVideo(video: DownloadedVideo)

    @Query("DELETE FROM downloaded_videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)
}
