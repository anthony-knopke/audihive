package com.darkaquadigital.audihive.ui.screens

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.darkaquadigital.audihive.R
import com.darkaquadigital.audihive.data.PlaylistViewModel
import com.darkaquadigital.audihive.database.PlaylistEntity
import com.darkaquadigital.audihive.database.PlaylistWithSongs
import com.darkaquadigital.audihive.data.Song
import com.darkaquadigital.audihive.ui.components.formatDuration
import com.darkaquadigital.audihive.utilities.toSong
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlaylistScreen(
    playlists: StateFlow<List<PlaylistWithSongs>>,
    navController: NavController,
    playlistViewModel: PlaylistViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
        ) {
            Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add Playlist")
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Create Playlist")
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(playlists.value) { playlist ->
                PlaylistItem(playlist = playlist) {
                    navController.navigate("playlist/${playlist.playlist.playlistId}")
                }
            }
        }
    }

    // Playlist creation dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Create Playlist") },
            text = {
                Column {
                    Text("Enter a name for your new playlist:")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        placeholder = { Text("Playlist name") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            playlistViewModel.createPlaylist(playlistName)
                            playlistName = "" // Reset input field
                            showDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PlaylistItem(
    playlist: PlaylistWithSongs,
    onPlaylistClick: (PlaylistWithSongs) -> Unit
) {
    val song = playlist.songs.firstOrNull()
    val albumImageBitmap = song?.albumImagePath?.let { BitmapFactory.decodeFile(it) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlaylistClick(playlist) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (albumImageBitmap != null)
            Image(
                bitmap = albumImageBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        else
            Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = "Playlist Icon", modifier = Modifier.size(48.dp))

        Spacer(modifier = Modifier.width(12.dp))
        Text(text = playlist.playlist.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PlaylistDetailScreen(
    playlistId: Int,
    playlistViewModel: PlaylistViewModel,
    onSongClick: (Song) -> Unit,
    onPlaylistOptions: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit
) {
    var playlistWithSongs: PlaylistWithSongs? by remember { mutableStateOf(null) }

    LaunchedEffect(playlistId) {
        playlistViewModel.getPlaylistWithSongs(playlistId) { result ->
            playlistWithSongs = result
        }
    }

    Log.d("PlaylistDetailScreen", "playlistWithSongs: ${playlistWithSongs?.songs?.size}")

    playlistWithSongs?.let { playlist ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp), // Adds spacing between items
            contentPadding = PaddingValues(24.dp) // Adds padding around entire column
        ) {
            // **Album Art**
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val albumArt = playlist.songs.firstOrNull()?.albumImagePath
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
                        ?: ImageBitmap.imageResource(R.drawable.music_icon)

                    Image(
                        bitmap = albumArt,
                        contentDescription = "Album Art",
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            // **Playlist Name**
            item {
                Text(
                    text = playlist.playlist.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            // **Play Buttons**
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
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

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onPlaylistOptions,
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Add to Playlist")
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }

            // **Song List**
            items(playlist.songs) { song ->
                SongItem(
                    song = song.toSong(),
                    onSongClick = { onSongClick(song.toSong()) },
                    onAddToPlaylist = { onPlaylistOptions() },
                    onRemoveClick = { playlistViewModel.removeSongFromPlaylist(playlist.playlist.playlistId, song.songId) }
                )
            }
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}


@Composable
fun SongItem(
    song: Song,
    onSongClick: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onRemoveClick: () -> Unit) {

    val albumImageBitmap = song.albumImagePath?.let { BitmapFactory.decodeFile(it) }
    val placeholderImage = ImageBitmap.imageResource(R.drawable.music_icon)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSongClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = albumImageBitmap?.asImageBitmap() ?: placeholderImage,
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = song.title, fontSize = 20.sp)
            Text(text = "${song.artist} - ${formatDuration(song.duration)}", fontSize = 14.sp)
        }

        IconButton(onClick = onAddToPlaylist) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Add to Playlist")
        }
        IconButton(onClick = onRemoveClick) {
            Icon(Icons.Default.Delete, contentDescription = "Remove Song")
        }
    }
}
