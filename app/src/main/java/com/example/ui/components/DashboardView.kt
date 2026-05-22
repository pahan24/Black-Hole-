package com.example.ui.components

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.DownloadStatus
import com.example.data.DownloadedVideo
import com.example.data.VideoPlatform
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.io.File

sealed interface NavigationTab {
    object Singularity : NavigationTab
    object Orbits : NavigationTab
    object Cognition : NavigationTab
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf<NavigationTab>(NavigationTab.Singularity) }
    val inputUrl by viewModel.inputUrl.collectAsState()
    val isExtracting by viewModel.isExtracting.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadSpeedMBs by viewModel.downloadSpeedMBs.collectAsState()
    val downloadBytesFormatted by viewModel.downloadBytesFormatted.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val currentDownloadingName by viewModel.currentDownloadingName.collectAsState()

    val allVideos by viewModel.allVideos.collectAsState()
    val currentPlayingVideo by viewModel.currentPlayingVideo.collectAsState()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    if (currentPlayingVideo != null) {
        BackHandler {
            viewModel.currentPlayingVideo.value = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "BLACK HOLE",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = 4.sp,
                            color = StarWhite
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DeepSpaceBlack,
                        titleContentColor = StarWhite
                    ),
                    modifier = Modifier.background(DeepSpaceBlack)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = NebulaCard,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(
                        width = 0.5.dp,
                        brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                ) {
                    NavigationBarItem(
                        selected = activeTab == NavigationTab.Singularity,
                        onClick = { activeTab = NavigationTab.Singularity },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Singularity Home") },
                        label = { Text("Singularity", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepSpaceBlack,
                            selectedTextColor = SingularityCyan,
                            indicatorColor = SingularityCyan,
                            unselectedIconColor = MutedDust,
                            unselectedTextColor = MutedDust
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == NavigationTab.Orbits,
                        onClick = { activeTab = NavigationTab.Orbits },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (allVideos.isNotEmpty()) {
                                        Badge(containerColor = HorizonOrange) {
                                            Text(allVideos.size.toString(), color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.History, contentDescription = "Downloads History")
                            }
                        },
                        label = { Text("Orbits", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepSpaceBlack,
                            selectedTextColor = SingularityCyan,
                            indicatorColor = SingularityCyan,
                            unselectedIconColor = MutedDust,
                            unselectedTextColor = MutedDust
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == NavigationTab.Cognition,
                        onClick = { activeTab = NavigationTab.Cognition },
                        icon = { Icon(Icons.Default.Info, contentDescription = "Cognition Info") },
                        label = { Text("Cognition", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepSpaceBlack,
                            selectedTextColor = SingularityCyan,
                            indicatorColor = SingularityCyan,
                            unselectedIconColor = MutedDust,
                            unselectedTextColor = MutedDust
                        )
                    )
                }
            },
            containerColor = DeepSpaceBlack
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(DeepSpaceBlack)
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "tab_navigation"
                ) { targetState ->
                    when (targetState) {
                        is NavigationTab.Singularity -> {
                            SingularityScreen(
                                viewModel = viewModel,
                                inputUrl = inputUrl,
                                isExtracting = isExtracting,
                                errorMessage = errorMessage,
                                downloadStatus = downloadStatus,
                                downloadProgress = downloadProgress,
                                downloadSpeedMBs = downloadSpeedMBs,
                                downloadBytesFormatted = downloadBytesFormatted,
                                currentDownloadingName = currentDownloadingName,
                                onDownloadTrigger = {
                                    focusManager.clearFocus()
                                    viewModel.startDownloadFlow()
                                }
                            )
                        }
                        is NavigationTab.Orbits -> {
                            OrbitsScreen(
                                videos = allVideos,
                                onDelete = { video -> viewModel.deleteVideo(video) },
                                onPlay = { video -> viewModel.currentPlayingVideo.value = video },
                                onShare = { video -> shareDownloadedVideo(context, video) }
                            )
                        }
                        is NavigationTab.Cognition -> {
                            CognitionScreen()
                        }
                    }
                }
            }
        }

        // Floating full cover video media player view
        if (currentPlayingVideo != null) {
            VideoPlayerView(
                video = currentPlayingVideo!!,
                onDismiss = { viewModel.currentPlayingVideo.value = null }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SingularityScreen(
    viewModel: MainViewModel,
    inputUrl: String,
    isExtracting: Boolean,
    errorMessage: String?,
    downloadStatus: DownloadStatus,
    downloadProgress: Float,
    downloadSpeedMBs: Double,
    downloadBytesFormatted: String,
    currentDownloadingName: String,
    onDownloadTrigger: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 1. Central Spinning Cosmic Singularity Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                BlackHoleButton(
                    status = downloadStatus,
                    progress = downloadProgress,
                    onClick = onDownloadTrigger
                )
            }
        }

        // 2. Beautiful instructions hint (English only)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Paste Link & Download",
                    fontSize = 14.sp,
                    color = SingularityCyan,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Instagram • TikTok • YouTube • Facebook",
                    fontSize = 11.sp,
                    color = MutedDust,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // 3. Galactic Paste & Input Bar
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { viewModel.onUrlChange(it) },
                    placeholder = { Text("Paste Link Here...", color = MutedDust, fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    leadingIcon = {
                        Icon(Icons.Default.Download, contentDescription = "Downloader Icon", tint = SingularityCyan)
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (inputUrl.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onUrlChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Input", tint = MutedDust)
                                }
                            }
                            Button(
                                onClick = { viewModel.pasteFromClipboard() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccretionPurple),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .height(36.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = Color.White, modifier = Modifier.size(14.dp))
                                    Text("PASTE", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onDownloadTrigger()
                        }
                    ),
                    singleLine = true
                )

                // Error Gravity Notification
                errorMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Error detail",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearErrorMessage() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Close Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Quick Testing Pilots (Tap to test downloads easily!)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tap to test templates:",
                    fontSize = 11.sp,
                    color = MutedDust,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 2.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val samples = listOf(
                        Triple("Instagram Reels", "https://instagram.com/p/CoSmbLh_TEST", Color(0xFFE1306C)),
                        Triple("TikTok Short", "https://tiktok.com/@user/video/719_TEST", SingularityCyan),
                        Triple("YouTube HD", "https://youtube.com/watch?v=SIn_TEST", HorizonOrange),
                        Triple("Facebook Clip", "https://facebook.com/watch/?v=394_TEST", Color(0xFF1877F2))
                    )
                    
                    samples.forEach { (label, testUrl, color) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
                            modifier = Modifier
                                .clickable {
                                    viewModel.onUrlChange(testUrl)
                                }
                                .border(0.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(32.dp)),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Active Telemetry Speeds Log Card
        item {
            AnimatedVisibility(
                visible = downloadStatus != DownloadStatus.IDLE,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NebulaCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .border(
                            0.5.dp,
                            Brush.linearGradient(listOf(SingularityCyan.copy(alpha = 0.40f), Color.Transparent)),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (downloadStatus == DownloadStatus.EXTRACTING) "gravitational extraction..." else "pulling video singularity...",
                                    color = SingularityCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = currentDownloadingName,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            if (downloadStatus == DownloadStatus.DOWNLOADING) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = HorizonOrange.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = String.format("%.2f MB/s", downloadSpeedMBs),
                                        color = HorizonOrange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (downloadStatus == DownloadStatus.DOWNLOADING) {
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                color = HorizonOrange,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                color = SingularityCyan,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = downloadBytesFormatted,
                                color = MutedDust,
                                fontSize = 11.sp
                            )
                            Text(
                                text = when (downloadStatus) {
                                    DownloadStatus.EXTRACTING -> "RESOLVING..."
                                    DownloadStatus.DOWNLOADING -> "${(downloadProgress * 100).toInt()}% COMPLETED"
                                    DownloadStatus.COMPLETED -> "SUCCESS COGNITION"
                                    DownloadStatus.FAILED -> "FAILED"
                                    else -> "IDLE"
                                },
                                color = if (downloadStatus == DownloadStatus.COMPLETED) SingularityCyan else MutedDust,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun OrbitsScreen(
    videos: List<DownloadedVideo>,
    onDelete: (DownloadedVideo) -> Unit,
    onPlay: (DownloadedVideo) -> Unit,
    onShare: (DownloadedVideo) -> Unit
) {
    if (videos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Empty downloads",
                    tint = MutedDust.copy(alpha = 0.35f),
                    modifier = Modifier.size(96.dp)
                )
                Text(
                    text = "No downloads found in gravity",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Paste a link in the Singularity tab to download.",
                    color = MutedDust,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "gravity download orbits (${videos.size})",
                    color = SingularityCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 2.dp)
                )
            }

            items(videos, key = { it.id }) { video ->
                VideoFileItem(
                    video = video,
                    onPlay = { onPlay(video) },
                    onDelete = { onDelete(video) },
                    onShare = { onShare(video) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun VideoFileItem(
    video: DownloadedVideo,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val platformColor = when (video.platform) {
        VideoPlatform.YOUTUBE -> HorizonOrange
        VideoPlatform.INSTAGRAM -> Color(0xFFE1306C)
        VideoPlatform.TIKTOK -> SingularityCyan
        VideoPlatform.FACEBOOK -> Color(0xFF1877F2)
        VideoPlatform.OTHER -> NeonViolet
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = NebulaCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                0.5.dp,
                Brush.horizontalGradient(listOf(platformColor.copy(alpha = 0.25f), Color.Transparent)),
                RoundedCornerShape(14.dp)
            )
            .clickable { onPlay() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(platformColor.copy(alpha = 0.12f))
                    .border(0.5.dp, platformColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Video",
                    tint = platformColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = platformColor.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = video.platform.name,
                            color = platformColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = formatSizeString(video.fileSize),
                        color = MutedDust,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = StarWhite.copy(alpha = 0.8f)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
fun CognitionScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        item {
            Text(
                text = "COGNITIONS & STATUS",
                color = SingularityCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NebulaCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        0.5.dp,
                        Brush.linearGradient(listOf(SingularityCyan.copy(alpha = 0.25f), Color.Transparent)),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Black Hole Downloader v1.0",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Super-speed background video extraction pulling files straight into gravity.",
                        fontSize = 12.sp,
                        color = MutedDust,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "Platform Status:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NebulaCard)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusRow(platform = "Instagram Reels Parser", isOnline = true)
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                StatusRow(platform = "TikTok Short Extractor", isOnline = true)
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                StatusRow(platform = "YouTube Singularity Engine", isOnline = true)
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                StatusRow(platform = "Facebook Video Siphon", isOnline = true)
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NebulaCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "How to use:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SingularityCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Copy a link from Instagram, TikTok, YouTube, or Facebook.\n" +
                               "2. Open this app and paste the link in the input bar.\n" +
                               "3. Tap the center Black Hole button to download.\n" +
                               "4. Play downloaded videos in the 'Orbits' tab.",
                        fontSize = 12.sp,
                        color = StarWhite,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "Premium Space Interface • 100% active",
                color = MutedDust,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
fun StatusRow(platform: String, isOnline: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(platform, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isOnline) SingularityCyan else Color.Red)
            )
            Text(
                text = if (isOnline) "ONLINE" else "OFFLINE",
                color = if (isOnline) SingularityCyan else Color.Red,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatSizeString(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

private fun shareDownloadedVideo(context: android.content.Context, video: DownloadedVideo) {
    try {
        val file = File(video.filePath)
        if (file.exists()) {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Cosmic Video"))
        }
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Could not share file.", android.widget.Toast.LENGTH_SHORT).show()
    }
}
