package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DownloadStatus
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BlackHoleButton(
    status: DownloadStatus,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blackhole_rotation")
    
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (status) {
                    DownloadStatus.EXTRACTING -> 800
                    DownloadStatus.DOWNLOADING -> 1300
                    else -> 5000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val orbitWave by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle_orbit"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(240.dp)
            .graphicsLayer {
                scaleX = glowScale
                scaleY = glowScale
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val centerVal = Offset(cx, cy)
            val radius = size.minDimension / 2f

            // 1. Ambient outer aura background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NebulaCard.copy(alpha = 0.5f),
                        Color.Transparent
                    ),
                    center = centerVal,
                    radius = radius
                ),
                radius = radius,
                center = centerVal
            )

            // 2. Beautiful celestial particle rings
            for (i in 0 until 18) {
                val offsetAngle = i * (360f / 18f)
                val totalRad = Math.toRadians((rotationAngle + offsetAngle).toDouble())
                
                val orbitDist = radius * 0.72f + orbitWave + (i % 3) * 6f
                val px = cx + cos(totalRad).toFloat() * orbitDist
                val py = cy + sin(totalRad).toFloat() * orbitDist
                
                val particleColor = when {
                    status == DownloadStatus.EXTRACTING -> SingularityCyan.copy(alpha = 0.85f)
                    status == DownloadStatus.DOWNLOADING -> HorizonOrange.copy(alpha = 0.85f)
                    i % 3 == 0 -> SingularityCyan.copy(alpha = 0.65f)
                    i % 3 == 1 -> NeonViolet.copy(alpha = 0.55f)
                    else -> AccretionPurple.copy(alpha = 0.45f)
                }
                
                drawCircle(
                    color = particleColor,
                    radius = 3.2f + (i % 3) * 1.5f,
                    center = Offset(px, py)
                )
            }

            // 3. Sweep download glowing progress ring or ambient outline
            val ringDiameter = radius * 1.5f
            val ringTopLeft = Offset(cx - ringDiameter / 2f, cy - ringDiameter / 2f)
            val ringSize = Size(ringDiameter, ringDiameter)

            if (status == DownloadStatus.DOWNLOADING) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(HorizonOrange, SingularityCyan, HorizonOrange),
                        center = centerVal
                    ),
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    topLeft = ringTopLeft,
                    size = ringSize,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            } else {
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            SingularityCyan.copy(alpha = 0.2f),
                            AccretionPurple.copy(alpha = 0.05f)
                        )
                    ),
                    startAngle = rotationAngle,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = ringTopLeft,
                    size = ringSize,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // 4. Central ink black event horizon circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(136.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Custom visual feedback managed manually
                    onClick = onClick
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val centerVal = Offset(cx, cy)
                val radius = size.minDimension / 2f

                drawCircle(
                    color = Color.Black,
                    radius = radius * 0.92f,
                    center = centerVal
                )

                val ringColor = when (status) {
                    DownloadStatus.EXTRACTING -> SingularityCyan
                    DownloadStatus.DOWNLOADING -> HorizonOrange
                    else -> NeonViolet
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ringColor.copy(alpha = 0.7f),
                            ringColor.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = centerVal,
                        radius = radius
                    ),
                    radius = radius,
                    center = centerVal
                )

                drawCircle(
                    color = ringColor.copy(alpha = 0.85f),
                    radius = radius * 0.92f,
                    center = centerVal,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            val coreTransition = rememberInfiniteTransition(label = "core_puls")
            val coreScale by coreTransition.animateFloat(
                initialValue = 0.93f,
                targetValue = 1.07f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "core_scale"
            )

            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = coreScale
                    scaleY = coreScale
                },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (status) {
                        DownloadStatus.EXTRACTING -> "RESOLVING..."
                        DownloadStatus.DOWNLOADING -> "DOWNLOADING\n${(progress * 100).toInt()}%"
                        else -> "TAP TO\nDOWNLOAD"
                    },
                    color = when (status) {
                        DownloadStatus.EXTRACTING -> SingularityCyan
                        DownloadStatus.DOWNLOADING -> HorizonOrange
                        else -> StarWhite
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
