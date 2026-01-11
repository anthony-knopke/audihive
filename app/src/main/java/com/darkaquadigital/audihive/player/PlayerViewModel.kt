package com.darkaquadigital.audihive.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.media3.exoplayer.ExoPlayer
import com.darkaquadigital.audihive.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

enum class RepeatMode { OFF, REPEAT_ALL, REPEAT_ONE }

@HiltViewModel
class PlayerViewModel @Inject constructor(
    context: Application
    ) : ViewModel() {

    private val _exoPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                _isPlaying.value = isPlaying

                if (state == Player.STATE_ENDED) {
                    nextSong()  // Auto-advance
                }
            }
        })
    }

    private val _currentQueue = mutableStateOf<List<Song>>(emptyList())
    val currentQueue: State<List<Song>> = _currentQueue

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    fun playSong(song: Song) {
        _currentSong.value = song
        _exoPlayer.setMediaItem(MediaItem.fromUri(song.data))
        _exoPlayer.prepare()
        _exoPlayer.play()
    }

    fun playAll(songs: List<Song>) {
        Log.d("PlayerViewModel", "playAll called with ${songs.size} songs")
        if (songs.isNotEmpty()) {
            _currentQueue.value = songs
            playSong(songs.first()) // Start playing the first song in the list
        }
    }

    fun shufflePlay(songs: List<Song>) {
        Log.d("PlayerViewModel", "shufflePlay called with ${songs.size} songs")
        if (songs.isNotEmpty()) {
            val shuffledSongs = songs.shuffled()
            _currentQueue.value = shuffledSongs
            playSong(shuffledSongs.first()) // Start playing the first shuffled song
        }
    }

    fun togglePlayPause() {
        if (_exoPlayer.isPlaying) {
            _exoPlayer.pause()
        } else {
            _exoPlayer.play()
        }
        _isPlaying.value = _exoPlayer.isPlaying
    }

    fun nextSong() {
        val queue = _currentQueue.value
        val currentIndex = queue.indexOf(_currentSong.value)
        if (currentIndex != -1 && currentIndex < queue.size - 1) {
            playSong(queue[currentIndex + 1])
        }
    }

    fun seekTo(positionMs: Long) {
        _exoPlayer.seekTo(positionMs)
    }

    fun previousSong() {
        val queue = _currentQueue.value
        val currentIndex = queue.indexOf(_currentSong.value)
        if (currentIndex > 0) {
            playSong(queue[currentIndex - 1])
        }
    }

    fun getSongProgress(): Float {
        return _exoPlayer.currentPosition.toFloat() / _exoPlayer.duration.toFloat()
    }

    override fun onCleared() {
        super.onCleared()
        _exoPlayer.release()
    }

    private val _repeatMode = mutableStateOf(RepeatMode.OFF)
    val repeatMode: State<RepeatMode> = _repeatMode

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.REPEAT_ALL
            RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
            RepeatMode.REPEAT_ONE -> RepeatMode.OFF
        }
    }

    fun getQueueCurrentIndex(): Int {
        return _currentQueue.value.indexOf(_currentSong.value)
    }

    fun playFromQueue(index: Int) {
        if (index in _currentQueue.value.indices) {
            playSong(_currentQueue.value[index])
        }
    }

    fun removeFromQueue(index: Int) {
        if (index in _currentQueue.value.indices) {
            val updatedQueue = _currentQueue.value.toMutableList()
            updatedQueue.removeAt(index)
        }
    }

}