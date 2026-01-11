package com.darkaquadigital.audihive.services

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerNotificationManager
import com.darkaquadigital.audihive.data.QueueItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@UnstableApi
class AudioPlaybackService : MediaBrowserServiceCompat() {

    private lateinit var player: ExoPlayer
    private lateinit var notificationManager: PlayerNotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaController: MediaControllerCompat
    private val _queue =
        MutableStateFlow<List<QueueItem>>(emptyList())
    val queue: StateFlow<List<QueueItem>> = _queue


    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // Initialize the ExoPlayer
        player = ExoPlayer.Builder(this@AudioPlaybackService).build()

        // Create a MediaSession for external controls (e.g. lock screen, Bluetooth)
        mediaSession = MediaSessionCompat(this, "AudioPlaybackService").apply {
            isActive = true
        }

        mediaController = MediaControllerCompat(this, mediaSession.sessionToken)

        // Build the notification manager
        notificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
            .setMediaDescriptionAdapter(DescriptionAdapter(this))
            .setNotificationListener(NotificationListener(this))
            .build()
            .apply {
                setPlayer(player)
                setMediaSessionToken(mediaSession.sessionToken.token as android.media.session.MediaSession.Token)
            }

        player.addListener(object : Player.Listener {
            fun onMediaItemTransition(
                mediaItem: MediaBrowserCompat.MediaItem?,
                reason: Int
            ) {
                updateQueue()
            }

            override fun onTimelineChanged(
                timeline: Timeline,
                reason: Int
            ) {
                updateQueue()
            }
        })

    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        TODO("Not yet implemented")
    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        notificationManager.setPlayer(null)
        player.release()
        mediaSession.release()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "audio_playback_channel"
    }

    // Handles notification content
    @UnstableApi
    private class DescriptionAdapter(private val context: Context) :
        PlayerNotificationManager.MediaDescriptionAdapter {

        override fun getCurrentContentTitle(player: Player): String {
            return player.mediaMetadata.title?.toString() ?: "Playing audio"
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { intent ->
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            }
        }

        override fun getCurrentContentText(player: Player): String? {
            return player.mediaMetadata.artist?.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? = null
    }

    // Handles lifecycle of foreground notification
    @UnstableApi
    private class NotificationListener(private val service: Service) :
        PlayerNotificationManager.NotificationListener {

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            service.stopForeground(true)
            service.stopSelf()
        }

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing) {
                service.startForeground(notificationId, notification)
            } else {
                service.stopForeground(false)
            }
        }
    }

    // Updates the queue when the player changes
    private fun updateQueue() {
        val items = (0 until player.mediaItemCount).map { index ->
            val mediaItem = player.getMediaItemAt(index)
            QueueItem(
                mediaId = mediaItem.mediaId,
                title = mediaItem.mediaMetadata.title?.toString() ?: "",
                artist = mediaItem.mediaMetadata.artist?.toString() ?: "",
                albumArt = mediaItem.mediaMetadata.artworkUri
            )
        }
        _queue.value = items
    }

}
