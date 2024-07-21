package com.example.musichub.Data

data class FollowData(
    var email:String = "",
    var follow:String = "",
    var email_follow:String = email + "_" + follow
)
