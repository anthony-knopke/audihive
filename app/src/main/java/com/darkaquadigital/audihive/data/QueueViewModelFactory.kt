package com.darkaquadigital.audihive.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.darkaquadigital.audihive.player.PlayerViewModel

class QueueViewModelFactory(
    private val playerViewModel: PlayerViewModel
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QueueViewModel::class.java)) {
            return QueueViewModel(playerViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
