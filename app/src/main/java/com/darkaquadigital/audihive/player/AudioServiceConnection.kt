package com.darkaquadigital.audihive.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.SessionToken
import com.darkaquadigital.audihive.services.AudioPlaybackService

@UnstableApi
class AudioServiceConnection(context: Context) {

    private val sessionToken = SessionToken(context, ComponentName(context, AudioPlaybackService::class.java))

    //val mediaBrowser: MediaBrowser = MediaBrowser.Builder(context, sessionToken).build()
}