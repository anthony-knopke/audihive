package com.darkaquadigital.audihive.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val playlistId: Int = 0,  // This must match the column name in the cross-reference
    val name: String
)
