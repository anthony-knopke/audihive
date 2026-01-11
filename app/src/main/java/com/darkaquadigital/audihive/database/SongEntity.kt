package com.darkaquadigital.audihive.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val songId: Int = 0,
    val title: String,
    val artist: String,
    val album: String,
    val filePath: String,
    val duration: Long,
    val albumImagePath: String?,
    val lastModified: Long,
    val fileSize: Long
)