package com.darkaquadigital.audihive.data

import androidx.lifecycle.ViewModel
import com.darkaquadigital.audihive.player.PlayerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class QueueViewModel(private val playerViewModel: PlayerViewModel) : ViewModel() {

    private val _queue = MutableStateFlow(playerViewModel.currentQueue.value)
    val queue: StateFlow<List<Song>> = _queue

    val currentIndex = playerViewModel.getQueueCurrentIndex()

    fun playAt(index: Int) {
        playerViewModel.playFromQueue(index)
    }

    fun removeAt(index: Int) {
        playerViewModel.removeFromQueue(index)
    }
}
