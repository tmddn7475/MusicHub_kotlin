package com.example.musichub.RoomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MusicDAO {
    @Query("select * from playlist")
    fun getPlaylist(): List<PlaylistEntity>

    @Query("select songUrl from playlist where songUrl = :url")
    fun getUrl(url:String) : String

    @Query("select count(*) from playlist where songUrl = :url")
    fun getCount(url:String) : Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSong(playlistEntity: PlaylistEntity)

    @Query("delete from playlist where songUrl = :url")
    fun deleteSong(url: String)

    @Query("select * from search")
    fun getSearch():List<SearchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSearch(searchText: SearchEntity)

    @Query("delete from search where searchText = :text")
    fun deleteSearch(text: String)

    @Query("delete from playlist")
    fun deleteAll()

    @Query("delete from search")
    fun deleteAll2()
}