package com.example.ui.components

import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.DownloadedVideo
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.SingularityCyan
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun VideoPlayerView(
    video: DownloadedVideo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoViewRef = remember { VideoViewRef() }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPos by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0f) }

    // Cleanly stop and release VideoView playback when removed
    DisposableEffect(Unit) {
        onDispose {
            try {
                videoViewRef.view?.stopPlayback()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    // Keep scanning progress timeline
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            try {
                videoViewRef.view?.let { vv ->
                    if (vv.isPlaying) {
                        currentPos = vv.currentPosition.toFloat()
                        duration = duration.coerceAtLeast(vv.duration.toFloat())
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
            delay(250)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
    ) {
        // Native High performance Video Renderer
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    try {
                        val file = File(video.filePath)
                        if (file.exists()) {
                            setVideoPath(file.absolutePath)
                        } else {
                            // Fallback to active stream if file got deleted
                            setVideoPath(video.downloadUrl)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    setOnPreparedListener { mp ->
                        try {
                            mp.isLooping = true
                            start()
                            isPlaying = true
                            duration = duration.coerceAtLeast(mp.duration.toFloat())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    setOnErrorListener { mp, what, extra ->
                        // Silent error handler preventing crash or default dialog loops
                        true
                    }
                    // Assign the instance once on creation to avoid update block state writes
                    videoViewRef.view = this
                }
            },
            update = { /* no-op to prevent state-write recomposition cascades */ },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .align(Alignment.Center)
        )

        // Overlay Space Controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Display Title & Back trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = video.platform.name,
                        color = SingularityCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                IconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Player",
                        tint = Color.White
                    )
                }
            }

            // Central Pause / Play tap target
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable {
                        videoViewRef.view?.let { vv ->
                            try {
                                if (vv.isPlaying) {
                                    vv.pause()
                                    isPlaying = false
                                } else {
                                    vv.start()
                                    isPlaying = true
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!isPlaying) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f)),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = SingularityCyan,
                            modifier = Modifier
                                .padding(16.dp)
                                .size(48.dp)
                        )
                    }
                }
            }

            // Footer Timeline scrubber
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(12.dp)
            ) {
                Slider(
                    value = currentPos,
                    onValueChange = { value ->
                        currentPos = value
                    },
                    onValueChangeFinished = {
                        try {
                            videoViewRef.view?.seekTo(currentPos.toInt())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    valueRange = 0f..duration.coerceAtLeast(100f),
                    colors = SliderDefaults.colors(
                        thumbColor = SingularityCyan,
                        activeTrackColor = SingularityCyan,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPos.toInt()),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatTime(duration.toInt()),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun formatTime(millis: Int): String {
    val sec = (millis / 1000) % 60
    val min = (millis / (1000 * 60)) % 60
    val hr = (millis / (1000 * 60 * 60)) % 24
    return if (hr > 0) {
        String.format("%02d:%02d:%02d", hr, min, sec)
    } else {
        String.format("%02d:%02d", min, sec)
    }
}

class VideoViewRef {
    var view: VideoView? = null
}
