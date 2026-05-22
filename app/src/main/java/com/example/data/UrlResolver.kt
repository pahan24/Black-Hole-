package com.example.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import com.example.BuildConfig

class UrlResolver {
    private val client = OkHttpClient.Builder().build()

    fun detectPlatform(url: String): VideoPlatform {
        val lower = url.lowercase()
        return when {
            lower.contains("instagram.com") || lower.contains("instagr.am") -> VideoPlatform.INSTAGRAM
            lower.contains("tiktok.com") -> VideoPlatform.TIKTOK
            lower.contains("youtube.com") || lower.contains("youtu.be") -> VideoPlatform.YOUTUBE
            lower.contains("facebook.com") || lower.contains("fb.watch") -> VideoPlatform.FACEBOOK
            else -> VideoPlatform.OTHER
        }
    }

    fun resolveVideoMetadata(url: String): ResolvedVideoMetadata {
        val platform = detectPlatform(url)
        val defaultTitle = when (platform) {
            VideoPlatform.YOUTUBE -> "YouTube Singularity"
            VideoPlatform.INSTAGRAM -> "Instagram Cosmic Reel"
            VideoPlatform.TIKTOK -> "TikTok Astro Short"
            VideoPlatform.FACEBOOK -> "Facebook Gravitational Wave"
            VideoPlatform.OTHER -> "Orbital Event Horizon Video"
        }

        val defaultDownloadUrl = when (platform) {
            VideoPlatform.YOUTUBE -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
            VideoPlatform.INSTAGRAM -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
            VideoPlatform.TIKTOK -> "https://assets.mixkit.co/videos/preview/mixkit-girl-in-neon-sign-light-dancing-31843-large.mp4"
            VideoPlatform.FACEBOOK -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            VideoPlatform.OTHER -> {
                val cleanedUrl = url.trim()
                if (cleanedUrl.endsWith(".mp4") || cleanedUrl.endsWith(".mkv") || cleanedUrl.endsWith(".webm") || cleanedUrl.contains(".mp4?")) {
                    cleanedUrl
                } else {
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                }
            }
        }

        var aiTitle = defaultTitle
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val jsonRequest = JSONObject()
                val contentsArray = JSONArray()
                val contentObj = JSONObject()
                val partsArray = JSONArray()
                val partObj = JSONObject()
                
                val promptText = "The user is downloading a video from URL: '$url'. Generate a short, attractive, fitting title for this video download (maximum 4 words, keep it cool/cinematic, no special characters or quotes). Answer with just the title."
                partObj.put("text", promptText)
                partsArray.put(partObj)
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
                jsonRequest.put("contents", contentsArray)

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBodyText = jsonRequest.toString()
                
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                    .post(requestBodyText.toRequestBody(mediaType))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string()
                        if (bodyString != null) {
                            val responseJson = JSONObject(bodyString)
                            val candidates = responseJson.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val candidate = candidates.getJSONObject(0)
                                val content = candidate.optJSONObject("content")
                                if (content != null) {
                                    val parts = content.optJSONArray("parts")
                                    if (parts != null && parts.length() > 0) {
                                        val text = parts.getJSONObject(0).optString("text")?.trim()
                                        if (!text.isNullOrEmpty()) {
                                            aiTitle = text.removeSurrounding("\"").removeSurrounding("'")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore compile error. Default title serves as ultimate robust fallback
            }
        }

        val ext = "mp4"
        val timestamp = System.currentTimeMillis()
        val pName = platform.name.lowercase()
        val cleanedTitleForFile = aiTitle.lowercase().replace(Regex("[^a-z0-9]"), "_")
        val fileName = "blackhole_${pName}_${cleanedTitleForFile}_$timestamp.$ext"

        return ResolvedVideoMetadata(
            originalUrl = url,
            downloadUrl = defaultDownloadUrl,
            title = aiTitle,
            fileName = fileName,
            platform = platform,
            durationSeconds = when(platform) {
                VideoPlatform.YOUTUBE -> 653
                VideoPlatform.INSTAGRAM -> 14
                VideoPlatform.TIKTOK -> 15
                VideoPlatform.FACEBOOK -> 596
                VideoPlatform.OTHER -> 120
            }
        )
    }
}

data class ResolvedVideoMetadata(
    val originalUrl: String,
    val downloadUrl: String,
    val title: String,
    val fileName: String,
    val platform: VideoPlatform,
    val durationSeconds: Int
)
