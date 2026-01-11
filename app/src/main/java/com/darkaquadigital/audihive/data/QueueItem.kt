package com.darkaquadigital.audihive.data

import android.net.Uri

data class QueueItem(
    val mediaId: String,
    val title: String,
    val artist: String,
    val albumArt: Uri?
)

