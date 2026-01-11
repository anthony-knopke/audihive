package com.darkaquadigital.audihive.ui.screens

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.darkaquadigital.audihive.R
import com.darkaquadigital.audihive.data.PlaylistViewModel
import com.darkaquadigital.audihive.data.Song
import com.darkaquadigital.audihive.data.SongViewModel
import com.darkaquadigital.audihive.database.SongEntity
import com.darkaquadigital.audihive.player.PlayerViewModel
import com.darkaquadigital.audihive.ui.components.SongItem
import com.darkaquadigital.audihive.utilities.getSongEntity

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SongsScreen(
    songs: List<Song>,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    onSongClick: (SongEntity) -> Unit,
    isLoading: MutableState<Boolean>,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit
) {
    val context = LocalContext.current.applicationContext as Application
    val hasPermission = remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val placeholderImage = ImageBitmap.imageResource(id = R.drawable.music_icon)

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission.value = granted
    }

    // Observe lifecycle to refresh when returning from settings
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            hasPermission.value = granted
        }
    }

    // Request permission initially
    LaunchedEffect(Unit) {
        if (!hasPermission.value) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading.value -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent
                    )
                }
            }

            !hasPermission.value -> { // Check permission first
                Text(
                    text = "Permission Denied: Cannot access music files.",
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            songs.isEmpty() -> {
                Text(
                    text = "No songs found",
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    stickyHeader {
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            tonalElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = onPlayAll,
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play All")
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = onShuffle,
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                ) {
                                    Icon(Icons.Filled.Shuffle, contentDescription = "Shuffle Play")
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                            }
                        }
                    }

                    items(songs) { thisSong ->
                        SongItem(
                            song = thisSong,
                            placeholderImage = placeholderImage,
                            playerViewModel = playerViewModel,
                            playlistViewModel = playlistViewModel,
                            onAddToPlaylist = { song, playlistId ->
                                playlistViewModel.addSongToPlaylist(song.id, playlistId)
                            },
                            onCreatePlaylist = { name ->
                                playlistViewModel.createPlaylist(name)
                            }
                        ) { }
                    }
                }
            }
        }
    }

}

