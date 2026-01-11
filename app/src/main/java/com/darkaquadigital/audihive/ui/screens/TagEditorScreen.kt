package com.darkaquadigital.audihive.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.darkaquadigital.audihive.data.Song

@Composable
fun TagEditorScreen(
    song: Song,
    onSave: (Song) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.album) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Edit Tags", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
        OutlinedTextField(value = artist, onValueChange = { artist = it }, label = { Text("Artist") })
        OutlinedTextField(value = album, onValueChange = { album = it }, label = { Text("Album") })

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onCancel() }) {
                Text("Cancel")
            }
            Button(onClick = {
                val updatedSong = song.copy(title = title, artist = artist, album = album)
                onSave(updatedSong)
            }) {
                Text("Save")
            }
        }
    }
}
