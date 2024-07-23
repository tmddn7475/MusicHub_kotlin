package com.example.musichub.RoomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlaylistEntity::class, SearchEntity::class], version = 1, exportSchema = false)
abstract class PlaylistDatabase : RoomDatabase(){
    abstract fun musicDAO(): MusicDAO

    companion object {
        private var INSTANCE: PlaylistDatabase? = null

        fun getInstance(context: Context): PlaylistDatabase? {
            if (INSTANCE == null) {
                synchronized(PlaylistDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        PlaylistDatabase::class.java, "Playlist.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}