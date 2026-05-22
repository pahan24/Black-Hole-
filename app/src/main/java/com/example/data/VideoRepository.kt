package com.example.data

import kotlinx.coroutines.flow.Flow

class VideoRepository(private val videoDao: VideoDao) {
    val allVideos: Flow<List<DownloadedVideo>> = videoDao.getAllVideos()

    suspend fun getVideoById(id: Long) = videoDao.getVideoById(id)
    suspend fun getVideoByOriginalUrl(url: String) = videoDao.getVideoByOriginalUrl(url)
    suspend fun insertVideo(video: DownloadedVideo) = videoDao.insertVideo(video)
    suspend fun updateVideo(video: DownloadedVideo) = videoDao.updateVideo(video)
    suspend fun deleteVideo(video: DownloadedVideo) = videoDao.deleteVideo(video)
    suspend fun deleteVideoById(id: Long) = videoDao.deleteVideoById(id)
}
