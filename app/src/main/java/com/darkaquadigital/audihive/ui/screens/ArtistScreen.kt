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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.darkaquadigital.audihive.data.Artist
import com.darkaquadigital.audihive.data.PlaylistViewModel
import com.darkaquadigital.audihive.data.Song
import com.darkaquadigital.audihive.data.SongViewModel
import com.darkaquadigital.audihive.player.PlayerViewModel
import com.darkaquadigital.audihive.ui.components.SongItem

@Composable
fun ArtistListScreen(artists: List<Artist>, navController: NavController) {
    LazyColumn {
        items(artists) { artist ->
            ArtistItem(artist = artist) {
                navController.navigate("artist/${artist.id}")
            }
        }
    }
}

@Composable
fun ArtistItem(artist: Artist, onArtistClick: (Artist) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onArtistClick(artist) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = artist.image.takeIf { it.isNotEmpty() }?.let {
                BitmapFactory.decodeFile(it)?.asImageBitmap()
            } ?: ImageBitmap.imageResource(R.drawable.music_icon),
            contentDescription = "Artist Image",
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(artist.name, fontSize = 18.sp)
            Text(artist.name, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ArtistDetailScreen(
    artist: Artist,
    albums: List<Album>,
    songs: List<Song>,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        // Artist Image
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val artistArt = artist.image.takeIf { it.isNotEmpty() }?.let {
                    BitmapFactory.decodeFile(it)?.asImageBitmap()
                } ?: ImageBitmap.imageResource(R.drawable.music_icon)

                Image(
                    bitmap = artistArt,
                    contentDescription = "Artist Art",
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }

        // Artist Name & Bio
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = artist.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }

        // Play All & Shuffle Buttons
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
        }

        // Albums Section
        if (albums.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Albums",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            items(albums) { album ->
                AlbumArtistItem(album = album, onAlbumClick = onAlbumClick)
            }
        }

        // Songs Section
        if (songs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Songs",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

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



@Composable
fun ArtistScreen(
    artist: Artist, // This would be your Artist model, containing details like name, bio, and albums
    albums: List<Album>, // List of albums by this artist
    onAlbumClick: (Album) -> Unit // Function to handle album click
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Allow scrolling for larger content
    ) {
        // Artist's Profile Picture & Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val artistImage = artist.image.takeIf { it.isNotEmpty() }?.let {
                BitmapFactory.decodeFile(it)?.asImageBitmap()
            } ?: ImageBitmap.imageResource(R.drawable.music_icon) // Default placeholder image

            Image(
                bitmap = artistImage,
                contentDescription = "Artist Profile",
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        // Artist's Name & Bio
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = artist.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = artist.name.ifEmpty { "No bio available" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List of Albums by the Artist
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(albums) { album ->
                AlbumArtistItem(
                    album = album,
                    onAlbumClick = { onAlbumClick(album) }
                )
            }
        }
    }
}

@Composable
fun AlbumArtistItem(
    album: Album,
    onAlbumClick: (Album) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAlbumClick(album) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val albumImage = album.coverImage.takeIf { it.isNotEmpty() }?.let {
            BitmapFactory.decodeFile(it)?.asImageBitmap()
        } ?: ImageBitmap.imageResource(R.drawable.music_icon) // Default placeholder image

        Image(
            bitmap = albumImage,
            contentDescription = "Album Cover",
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = album.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

