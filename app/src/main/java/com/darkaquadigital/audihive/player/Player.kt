package com.darkaquadigital.audihive.player

import android.graphics.BitmapFactory
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.darkaquadigital.audihive.R
import com.darkaquadigital.audihive.data.Song
import com.darkaquadigital.audihive.ui.components.CustomLinearProgressIndicator
import kotlinx.coroutines.delay

@Composable
fun MusicPlayer(
    song: Song?,
    isPlaying: Boolean,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    getSongProgress: () -> Float,
    onShowQueue: () -> Unit,
    playerViewModel: PlayerViewModel
) {
    if (song == null) return  // Don't show if no song is playing

    var currentProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying, song) {
        while (isPlaying) {
            currentProgress = getSongProgress()
            delay(500)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onExpandToggle() }
            .animateContentSize()
    ) {
        if (!isExpanded) {
            // Mini Player
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    bitmap = song.albumImagePath?.let {
                        BitmapFactory.decodeFile(it)?.asImageBitmap()
                    } ?: ImageBitmap.imageResource(R.drawable.music_icon),
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(onClick = onPlayPauseClick) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = onShowQueue) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            CustomLinearProgressIndicator(
                progress = currentProgress, // 0f to 1f
                duration = song.duration,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                onSeek = { newPosition -> playerViewModel.seekTo(newPosition) }
            )

        } else {
            // Full Player
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onExpandToggle) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Large Album Art
                Image(
                    bitmap = song.albumImagePath?.let {
                        BitmapFactory.decodeFile(it)?.asImageBitmap()
                    } ?: ImageBitmap.imageResource(R.drawable.music_icon),
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Song Title & Artist
                Column(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = song.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Seek Bar
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    CustomLinearProgressIndicator(
                        progress = currentProgress, // 0f to 1f
                        duration = song.duration, // <- You'll need this
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        onSeek = { newPosition -> playerViewModel.seekTo(newPosition) }
                    )

                }

                Spacer(modifier = Modifier.height(16.dp))

                // Playback Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onShuffleClick) {
                        Icon(Icons.Filled.Shuffle, contentDescription = "Shuffle")
                    }
                    IconButton(onClick = onPreviousClick) {
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous")
                    }
                    IconButton(onClick = onPlayPauseClick, modifier = Modifier.size(64.dp)) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    IconButton(onClick = onNextClick) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Next")
                    }
                    IconButton(onClick = onRepeatClick) {
                        Icon(Icons.Filled.Repeat, contentDescription = "Repeat")
                    }
                }
            }
        }
    }
}
