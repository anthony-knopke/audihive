package com.darkaquadigital.audihive.ui.screens

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.darkaquadigital.audihive.data.Song

@Composable
fun QueueScreen(
    queue: List<Song>
) {
    LazyColumn {
        items(queue) { song ->
            ListItem(
                headlineContent = { Text(song.title) },
                supportingContent = { Text(song.artist) }
            )
        }
        Log.i("QueueScreen", "Queue size: ${queue.size}")
    }
}