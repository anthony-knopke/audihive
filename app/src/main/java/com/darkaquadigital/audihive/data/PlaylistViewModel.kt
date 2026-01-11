package com.darkaquadigital.audihive.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkaquadigital.audihive.database.PlaylistDao
import com.darkaquadigital.audihive.database.PlaylistRepository
import com.darkaquadigital.audihive.database.PlaylistWithSongs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistViewModel(private val repository: PlaylistRepository, private val playlistDao: PlaylistDao) : ViewModel() {

    val allPlaylists: StateFlow<List<PlaylistWithSongs>> = repository.getAllPlaylistsWithSongs()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    init {
        viewModelScope.launch {
            repository.getAllPlaylistsWithSongs().collect { result ->
                Log.d("PlaylistViewModel", "Playlist updated: $result")
            }
        }
    }

    private val _playlistWithSongs = MutableStateFlow<PlaylistWithSongs?>(null)
    val playlistWithSongs: StateFlow<PlaylistWithSongs?> = _playlistWithSongs

    private val _selectedPlaylistSongs = MutableStateFlow<PlaylistWithSongs?>(null)
    val selectedPlaylistSongs: StateFlow<PlaylistWithSongs?> = _selectedPlaylistSongs

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun addSongToPlaylist(songId: Int, playlistId: Int) {
        Log.d("DatabaseDebug", "Query - Playlist id: $playlistId, Song id: $songId")

        viewModelScope.launch {
            val playlistExists = playlistDao.getPlaylistById(playlistId) != null
            val songExists = playlistDao.getSongById(songId) != null

            Log.d("DatabaseDebug", "Playlist exists: $playlistExists, Song exists: $songExists")

            if (playlistExists && songExists) {
                Log.d("DatabaseDebug", "Inserting song $songId into playlist $playlistId")
                repository.addSongToPlaylist(songId, playlistId)
            } else {
                Log.e("DatabaseDebug", "Cannot add song to playlist: Invalid ID(s)")
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: Int, songId: Int) {
        viewModelScope.launch {
            playlistDao.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun getPlaylistWithSongs(playlistId: Int, onResult: (PlaylistWithSongs?) -> Unit) {
        viewModelScope.launch {
            repository.getPlaylistWithSongs(playlistId).collect { result ->
                Log.d("PlaylistViewModel", "Fetched playlist result: $result")
                Log.d("PlaylistViewModel", "Fetching playlist: $playlistId")
                Log.d("PlaylistViewModel", "Playlist songs: ${result?.songs?.size ?: 0}")
                onResult(result)
            }
        }
    }

}