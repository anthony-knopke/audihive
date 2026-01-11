package com.darkaquadigital.audihive.database

import android.util.Log
import kotlinx.coroutines.flow.Flow

class SongRepository(private val songDao: SongDao) {

    private val isDebugging = false

    suspend fun insertSong(song: SongEntity) {
        if (isDebugging) Log.d("SongRepository", "Inserting into DB: ${song.title} (ID: ${song.songId})")
        songDao.insertSong(song)

        // Verify insertion
        val insertedSong = songDao.getSongById(song.songId)
        if (isDebugging) Log.d("DatabaseSuccess", "Song successfully inserted: ${insertedSong.title} (ID: ${insertedSong.songId})")
    }

    suspend fun insertSongs(songs: List<SongEntity>) {
        songDao.insertSongs(songs)
    }

    suspend fun updateSong(song: SongEntity) {
        songDao.updateSong(song)
    }

    suspend fun deleteSong(song: SongEntity) {
        songDao.deleteSong(song)
    }

    suspend fun getSongByPath(filePath: String): SongEntity? {
        return songDao.getSongByPath(filePath)
    }

    fun getAllSongs(): Flow<List<SongEntity>> {  // ✅ Use LiveData
        return songDao.getAllSongs()
    }

    fun getAllArtists(): Flow<List<String>> {  // ✅ Use LiveData
        return songDao.getAllArtists()
    }

    fun getSongsByArtist(artist: String): Flow<List<SongEntity>> {  // ✅ Use LiveData
        return songDao.getSongsByArtist(artist)
    }

    suspend fun getAllStoredPaths(): List<String> {
        return songDao.getAllStoredPaths()
    }

    suspend fun clearAllSongs() {
        //songDao.clearAllSongs()
    }

}