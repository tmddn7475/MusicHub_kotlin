package com.example.musichub.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search")
data class SearchEntity(
    @PrimaryKey
    val searchText:String
)
