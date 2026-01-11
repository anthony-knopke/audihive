package com.darkaquadigital.audihive.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongIntoPlaylist(crossRef: PlaylistSongCrossRef)

    @Delete
    suspend fun deletePlaylist(playlistEntity: PlaylistEntity)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Int, songId: Int)

    @Transaction
    @Query("SELECT * FROM playlists")
    fun getPlaylists(): Flow<List<PlaylistWithSongs>>

    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists():  Flow<List<PlaylistEntity>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId")
    fun getPlaylistSongs(playlistId: Int): Flow<PlaylistWithSongs>

    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId LIMIT 1")
    suspend fun getPlaylistById(playlistId: Int): PlaylistEntity?

    @Transaction
    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId")
    fun getPlaylistWithSongs(playlistId: Int): Flow<PlaylistWithSongs?>

    @Transaction
    @Query("SELECT * FROM playlists")
    fun getAllPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>

    @Query("SELECT * FROM songs WHERE songId = :songId")
    suspend fun getSongById(songId: Int): SongEntity?

}