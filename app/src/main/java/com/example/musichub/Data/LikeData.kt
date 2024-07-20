package com.example.musichub.Data

data class LikeData(
    var songUrl:String = "",
    var email:String = "",
    var email_songUrl:String = email + "_" + songUrl
)
