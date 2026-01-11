package com.darkaquadigital.audihive.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.darkaquadigital.audihive.R
import com.darkaquadigital.audihive.data.Album
import com.darkaquadigital.audihive.data.PlaylistViewModel
import com.darkaquadigital.audihive.data.Song
import com.darkaquadigital.audihive.data.SongViewModel
import com.darkaquadigital.audihive.player.PlayerViewModel
import com.darkaquadigital.audihive.ui.components.SongItem

@Composable
fun AlbumListScreen(albums: List<Album>, navController: NavController) {
    LazyColumn {
        items(albums) { album ->
            AlbumItem(album = album) {
                navController.navigate("album/${album.id}")
            }
        }
    }
}


@Composable
fun AlbumItem(album: Album, onAlbumClick: (Album) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAlbumClick(album) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = album.coverImage.takeIf { it.isNotEmpty() }?.let {
                BitmapFactory.decodeFile(it)?.asImageBitmap()
            } ?: ImageBitmap.imageResource(R.drawable.music_icon),
            contentDescription = "Album Art",
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(album.name, fontSize = 18.sp)
            Text(album.artist, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AlbumDetailScreen(
    album: Album,
    songs: List<Song>,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Album Art & Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val albumArt = album.coverImage.takeIf { it.isNotEmpty() }?.let {
                BitmapFactory.decodeFile(it)?.asImageBitmap()
            } ?: ImageBitmap.imageResource(R.drawable.music_icon)

            Image(
                bitmap = albumArt,
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        // Album Name & Artist
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = album.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = album.artist.ifEmpty { "Unknown Artist" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Play All & Shuffle Buttons
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
                Text("Play All")
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
                Text("Shuffle")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Song List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize() // Takes up the remaining space
        ) {
            items(songs) { thisSong ->
                val placeholderImage = ImageBitmap.imageResource(id = R.drawable.music_icon)

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


