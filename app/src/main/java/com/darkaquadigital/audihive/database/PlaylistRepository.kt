package com.darkaquadigital.audihive.database

import android.util.Log
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(private val songDao: SongDao, private val playlistDao: PlaylistDao) {

    fun getAllPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>> {
        return playlistDao.getAllPlaylistsWithSongs()
    }

    suspend fun createPlaylist(name: String) {
        val playlist = PlaylistEntity(name = name)
        playlistDao.insertPlaylist(playlist)  // This inserts into the "playlists" table
        Log.d("DatabaseDebug", "Playlist inserted: $playlist")
    }

    suspend fun addSongToPlaylist(songId: Int, playlistId: Int) {
        val crossRef = PlaylistSongCrossRef(playlistId, songId)
        playlistDao.insertSongIntoPlaylist(crossRef)  // <- Ensure this actually runs!
    }

    fun getPlaylistWithSongs(playlistId: Int): Flow<PlaylistWithSongs?> {
        return playlistDao.getPlaylistWithSongs(playlistId)
    }

}