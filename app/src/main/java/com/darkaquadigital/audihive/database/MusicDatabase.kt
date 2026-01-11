package com.darkaquadigital.audihive.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "music_database"
                )
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL("PRAGMA foreign_keys = ON;")
                        }
                    })
                    .addMigrations(object : Migration(1, 2) {
                        override fun migrate(db: SupportSQLiteDatabase) { /*No changes, just to prevent wiping data*/}
                    })
                    .addMigrations(object : Migration(2, 3) {
                        override fun migrate(db: SupportSQLiteDatabase) { /*No changes, just to prevent wiping data*/}
                    })
                    .addMigrations(object : Migration(1, 4) {
                        override fun migrate(db: SupportSQLiteDatabase) {
                            db.execSQL("ALTER TABLE songs ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
                            db.execSQL("ALTER TABLE songs ADD COLUMN fileSize INTEGER NOT NULL DEFAULT 0")
                        }
                    })
                    .addMigrations(object : Migration(3, 4) {
                        override fun migrate(db: SupportSQLiteDatabase) {
                            db.execSQL("ALTER TABLE songs ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
                            db.execSQL("ALTER TABLE songs ADD COLUMN fileSize INTEGER NOT NULL DEFAULT 0")
                        }
                    })
                    .setJournalMode(JournalMode.TRUNCATE) // Forces immediate changes
                    .build()

                // Run FK check outside of callback
                instance.openHelper.writableDatabase.execSQL("PRAGMA foreign_keys = ON;")

                INSTANCE = instance
                instance
            }
        }


    }
}