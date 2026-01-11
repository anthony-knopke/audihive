package com.darkaquadigital.audihive.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkaquadigital.audihive.database.SongEntity
import com.darkaquadigital.audihive.database.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class SongViewModel(private val repository: SongRepository) : ViewModel() {

    private val isDebugging = false

    private val _songs = MutableStateFlow<List<SongEntity>>(emptyList())
    val songs: StateFlow<List<SongEntity>> = _songs

    init {
        viewModelScope.launch {
            repository.getAllSongs().collect { songsList ->
                Log.d("SongViewModel", "Current songs in DB: ${songsList.size}")
                _songs.value = songsList
            }
        }
    }

    fun insertSong(song: SongEntity) {
        viewModelScope.launch {
            if (isDebugging) Log.d("SongViewModel", "Inserting song: ${song.title}")
            repository.insertSong(song)
            _songs.value = repository.getAllSongs().first()
            if (isDebugging) Log.d("SongViewModel", "Songs after insert: ${_songs.value.size}")
        }
    }


    fun insertSongs(songs: List<SongEntity>) {
        viewModelScope.launch {
            repository.insertSongs(songs)
        }
    }

    fun fetchAllSongs() {
        viewModelScope.launch {
            val songs = repository.getAllSongs()
            //onResult(songs)
        }
    }

    fun fetchAllArtists(onResult: (Flow<List<String>>) -> Unit) {
        viewModelScope.launch {
            val artists = repository.getAllArtists()
            onResult(artists)
        }
    }

    fun fetchSongsByArtist(artist: String, onResult: (Flow<List<SongEntity>>) -> Unit) {
        viewModelScope.launch {
            val songs = repository.getSongsByArtist(artist)
            onResult(songs)
        }
    }

    fun clearSongs() {
        viewModelScope.launch {
            repository.clearAllSongs()
        }
    }

}