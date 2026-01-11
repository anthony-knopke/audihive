package com.darkaquadigital.audihive.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.darkaquadigital.audihive.database.PlaylistDao
import com.darkaquadigital.audihive.database.PlaylistRepository

class PlaylistViewModelFactory(private val repository: PlaylistRepository, private val playlistDao: PlaylistDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaylistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaylistViewModel(repository, playlistDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}