package com.darkaquadigital.audihive.utilities

import android.annotation.SuppressLint
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.IntentSender
import android.provider.MediaStore
import android.util.Log
import com.darkaquadigital.audihive.data.Song
import com.darkaquadigital.audihive.database.SongEntity
import com.mpatric.mp3agic.ID3v1
import com.mpatric.mp3agic.ID3v2
import com.mpatric.mp3agic.Mp3File
import java.io.File
import android.net.Uri
import android.os.Build
import com.mpatric.mp3agic.ID3v24Tag
import java.io.OutputStream


fun getAllSongs(context: Context): List<Song> {
    val songList = mutableListOf<Song>()
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.DURATION
    )

    val cursor = context.contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection, null, null, null
    )

    cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val lastModifiedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
        val filesizeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

        while (it.moveToNext()) {
            songList.add(
                Song(
                    id = it.getInt(idColumn),
                    title = it.getString(titleColumn),
                    artist = it.getString(artistColumn),
                    album = it.getString(albumColumn),
                    data = it.getString(dataColumn),
                    duration = it.getLong(durationColumn),
                    albumImagePath = null,
                    lastModified = it.getLong(lastModifiedColumn),
                    fileSize = it.getLong(filesizeColumn)
                )
            )
        }
    }

    return songList
}


fun getSongEntity(song: Song): SongEntity {
    val songEntity = SongEntity(
        songId = song.id,
        title = song.title,
        artist = song.artist,
        album = song.album,
        filePath = song.data,
        duration = song.duration,
        albumImagePath = song.albumImagePath,
        lastModified = song.lastModified,
        fileSize = song.fileSize
    )
    return songEntity
}

fun List<SongEntity>.toSongs(): List<Song> {
    return this.map { it.toSong() }
}

fun SongEntity.toSong(): Song {
    return Song(
        id = this.songId,
        title = this.title,
        artist = this.artist,
        album = this.album,
        albumImagePath = this.albumImagePath,
        duration = this.duration,
        data = this.filePath,
        lastModified = this.lastModified,
        fileSize = this.fileSize
    )
}

fun readTags(filePath: String): Map<String, String> {
    val mp3File = Mp3File(filePath)
    val tags = mutableMapOf<String, String>()

    if (mp3File.hasId3v2Tag()) {
        val tag: ID3v2 = mp3File.id3v2Tag
        tags["Title"] = tag.title ?: ""
        tags["Artist"] = tag.artist ?: ""
        tags["Album"] = tag.album ?: ""
        tags["Year"] = tag.year ?: ""
    } else if (mp3File.hasId3v1Tag()) {
        val tag: ID3v1 = mp3File.id3v1Tag
        tags["Title"] = tag.title ?: ""
        tags["Artist"] = tag.artist ?: ""
        tags["Album"] = tag.album ?: ""
        tags["Year"] = tag.year ?: ""
    }

    return tags
}

fun updateTags(
    context: Context,
    filePath: String,
    song: Song,
    onRecoverableSecurityException: (IntentSender) -> Unit
) {
    val mp3File = Mp3File(filePath)

    val tag = if (mp3File.hasId3v2Tag()) {
        mp3File.id3v2Tag
    } else {
        val newTag = com.mpatric.mp3agic.ID3v24Tag()
        mp3File.id3v2Tag = newTag
        newTag
    }

    tag.title = song.title
    tag.artist = song.artist
    tag.album = song.album

    val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.mp3")
    mp3File.save(tempFile.absolutePath)

    val uri = getAudioFileUri(context, filePath)
    if (uri == null) {
        Log.e("TagUpdate", "Failed to get URI for $filePath")
        return
    }

    try {
        context.contentResolver.openOutputStream(uri, "rwt")?.use { out ->
            tempFile.inputStream().copyTo(out)
            Log.d("TagUpdate", "Updated song for $filePath")
        }
        tempFile.delete()
    } catch (e: SecurityException) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
            onRecoverableSecurityException(e.userAction.actionIntent.intentSender)
        } else {
            throw e
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getAudioFileUri(context: Context, filePath: String): Uri? {
    val projection = arrayOf(MediaStore.Audio.Media._ID)
    val selection = "${MediaStore.Audio.Media.DATA} = ?"
    val selectionArgs = arrayOf(filePath)

    context.contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val id = cursor.getLong(idColumn)
            return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        }
    }
    return null
}

fun updateAudioFileTags(context: Context, filePath: String, song: Song) {
    val tempFile = writeUpdatedTagsToTempFile(context, filePath, song)

    deleteOriginalAudioFile(context, filePath)

    insertFileIntoMediaStore(context, tempFile)?.let {
        Log.i("TagUpdate", "Updated file reinserted: $it")
    } ?: Log.e("TagUpdate", "Failed to reinsert updated file")
}


fun writeUpdatedTagsToTempFile(context: Context, originalFilePath: String, song: Song): File {
    val mp3File = Mp3File(originalFilePath)

    val tag = if (mp3File.hasId3v2Tag()) {
        mp3File.id3v2Tag
    } else {
        val newTag = ID3v24Tag()
        mp3File.id3v2Tag = newTag
        newTag
    }

    tag.title = song.title
    tag.artist = song.artist
    tag.album = song.album

    val tempFile = File(context.cacheDir, "updated_${System.currentTimeMillis()}.mp3")
    mp3File.save(tempFile.absolutePath)
    return tempFile
}

fun deleteOriginalAudioFile(context: Context, filePath: String) {
    val uri = getAudioFileUri(context, filePath)
    if (uri != null) {
        context.contentResolver.delete(uri, null, null)
    }
}

fun insertFileIntoMediaStore(context: Context, tempFile: File): Uri? {
    val values = ContentValues().apply {
        put(MediaStore.Audio.Media.DISPLAY_NAME, tempFile.name)
        put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
        put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/") // optional: subfolder
        put(MediaStore.Audio.Media.IS_MUSIC, 1)
    }

    val uri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
    if (uri != null) {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            tempFile.inputStream().use { input ->
                input.copyTo(output)
            }
        }
        tempFile.delete()
    }

    return uri
}

