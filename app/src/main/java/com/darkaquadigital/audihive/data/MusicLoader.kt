package com.darkaquadigital.audihive.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class MusicLoader {
    private val isDebugging = false

    fun getAllSongs(context: Context): List<Song> {
        val songList = mutableListOf<Song>()

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA,  // File path (may be null on Android 10+)
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.SIZE
            ),
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getInt(0)
                val title = it.getString(1)
                val artist = it.getString(2)
                val album = it.getString(3)
                val dataIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)
                val duration = it.getLong(5)
                val lastModified = it.getLong(6)
                val fileSize = it.getLong(7)

                // Try to get the song file path (may be null on Android 10+)
                val data = if (dataIndex != -1) it.getString(dataIndex) else null
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toLong())

                // Get album artwork
                val albumImagePath = data?.let { path ->
                    getEmbeddedAlbumArt(path, context) ?: getAlbumImage(path)
                }

                if (isDebugging) Log.d("MusicLoader", "Loaded song: $title, URI: $uri, Path: $data, AlbumArt: $albumImagePath")

                songList.add(Song(
                    id, title, artist, album, data ?: uri.toString(), duration, albumImagePath, lastModified, fileSize
                ))
            }
        }

        return songList
    }

    private fun getEmbeddedAlbumArt(songFilePath: String, context: Context): String? {
        return try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(songFilePath)
            val data = mmr.embeddedPicture
            mmr.release()

            if (data != null) {
                // Save to cache and return the path
                val cacheFile = File(context.cacheDir, "${songFilePath.hashCode()}.jpg")
                FileOutputStream(cacheFile).use { it.write(data) }
                return cacheFile.absolutePath
            }
            null
        } catch (e: Exception) {
            if (isDebugging) Log.e("MusicLoader", "Failed to get embedded album art: ${e.message}")
            null
        }
    }

    // This function checks if there is a PNG or JPG image in the same directory as the song
    private fun getAlbumImage(songFilePath: String): String? {
        val songFile = File(songFilePath)
        val directory = songFile.parentFile

        directory?.let {
            val files = it.listFiles { _, name ->
                name.endsWith(".png", ignoreCase = true) || name.endsWith(".jpg", ignoreCase = true)
            }

            val imageFile = files?.firstOrNull()
            if (isDebugging) Log.d("MusicLoader", "Album art found in folder: ${imageFile?.absolutePath}")
            return imageFile?.absolutePath
        }

        if (isDebugging) Log.d("MusicLoader", "No album image found in: ${songFile.parent}")
        return null
    }

}