package com.darkaquadigital.audihive.database

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["playlistId"], // Must match PlaylistEntity's ID column
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["songId"], // Must match SongEntity's ID column
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index("playlistId"),
        androidx.room.Index("songId")
    ]
)
data class PlaylistSongCrossRef(
    val playlistId: Int,  // Must match column name in PlaylistEntity
    val songId: Int       // Must match column name in SongEntity
)