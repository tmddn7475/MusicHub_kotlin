package com.example.musichub.RoomDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist")
data class PlaylistEntity(
    @PrimaryKey
    val songUrl: String,
    @ColumnInfo(name = "time")
    val time: String
)
