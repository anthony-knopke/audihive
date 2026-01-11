package com.darkaquadigital.audihive.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "playlistId",    // Matches PlaylistEntity's primary key
        entityColumn = "songId",    // Matches SongEntity's primary key
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val songs: List<SongEntity>
)