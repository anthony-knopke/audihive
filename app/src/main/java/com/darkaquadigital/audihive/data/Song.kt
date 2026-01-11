package com.darkaquadigital.audihive.data
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song (
    val id: Int,
    val title: String,
    val artist: String,
    val album: String,
    val data: String,
    val duration: Long,
    val albumImagePath: String?,
    val lastModified: Long,
    val fileSize: Long
) : Parcelable
