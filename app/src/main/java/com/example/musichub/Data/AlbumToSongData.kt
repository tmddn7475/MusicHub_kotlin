package com.example.musichub.Data

data class AlbumToSongData(
    var songUrl:String = "",
    var key:String = "",
    var key_songUrl:String = key + "_" + songUrl,
    var time:String = ""
)
