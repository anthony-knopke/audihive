package com.darkaquadigital.audihive.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkaquadigital.audihive.TagEditorActivity
import com.darkaquadigital.audihive.data.PlaylistViewModel
import com.darkaquadigital.audihive.data.Song
import com.darkaquadigital.audihive.player.PlayerViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongItem(
    song: Song,
    placeholderImage: ImageBitmap,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel, // Used to get List of playlists
    onAddToPlaylist: (Song, Int) -> Unit, // Callback to add a song to a playlist
    onCreatePlaylist: (String) -> Unit, // Callback to create a new playlist
    onSongClick: () -> Unit
) {
    val albumImageBitmap = song.albumImagePath?.let { BitmapFactory.decodeFile(it) }

    // Bottom sheet state
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) } // State for playlist dialog
    var newPlaylistName by remember { mutableStateOf("") } // New playlist name input

    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
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

        IconButton(
            onClick = { playerViewModel.playSong(song) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = "Play",
                modifier = Modifier.size(32.dp)
            )
        }

        IconButton(
            onClick = { showBottomSheet = true },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "More options",
                modifier = Modifier.size(32.dp)
            )
        }
    }

    // **Modal Bottom Sheet**
    if (showBottomSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Options for ${song.title}", fontSize = 20.sp)

                Spacer(modifier = Modifier.height(8.dp))

                // Button to open Playlist Selection Dialog
                Button(onClick = { showDialog = true }) {
                    Text("Add to Playlist")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    val intent = Intent(context, TagEditorActivity::class.java).apply {
                        putExtra("song", song)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Edit Tags")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { showBottomSheet = false }) {
                    Text("Close")
                }
            }
        }
    }

    // **Playlist Selection Dialog**
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Choose a Playlist") },
            text = {
                Column {
                    // Existing Playlists
                    playlistViewModel.allPlaylists.value.forEach { playlist ->
                        TextButton(onClick = {
                            onAddToPlaylist(song, playlist.playlist.playlistId)
                            showDialog = false
                            showBottomSheet = false
                        }) {
                            Text(playlist.playlist.name)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Create New Playlist Input
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("New Playlist Name") }
                    )

                    Button(
                        onClick = {
                            if (newPlaylistName.isNotBlank()) {
                                onCreatePlaylist(newPlaylistName)
                                newPlaylistName = ""
                                showDialog = false
                                showBottomSheet = false
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Create New Playlist")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


// Helper function to format duration
fun formatDuration(duration: Long): String {
    val minutes = duration / 60000
    val seconds = (duration % 60000) / 1000
    return "%d:%02d".format(minutes, seconds)
}
