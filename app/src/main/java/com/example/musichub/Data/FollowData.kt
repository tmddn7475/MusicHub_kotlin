package com.example.musichub.Data

data class FollowData(
    var follow:String = "",
    var email:String = "",
    var email_follow:String = email + "_" + follow
)
