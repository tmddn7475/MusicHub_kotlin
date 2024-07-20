package com.example.musichub

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.musichub.Data.AlbumToSongData
import com.example.musichub.Data.LikeData
import com.example.musichub.RoomDB.PlaylistDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date

class Command {
     @SuppressLint("SimpleDateFormat")
     fun getTime(): String {
         val mFormat = SimpleDateFormat("yyyy/MM/dd")
         val mNow:Long = System.currentTimeMillis()
         val mDate = Date(mNow)

         return mFormat.format(mDate)
     }

    @SuppressLint("SimpleDateFormat")
    fun getTime2(): String {
        val mFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val mNow:Long = System.currentTimeMillis()
        val mDate = Date(mNow)

        return mFormat.format(mDate)
    }

    // 검색, 플레이리스트 기록 삭제
    fun deleteAll(context: Context){
        val db = PlaylistDatabase.getInstance(context)
        val r = Runnable {
            if (db != null) {
                db.musicDAO().deleteAll()
                db.musicDAO().deleteAll2()
            }
        }
        val thread = Thread(r)
        thread.start()

        val preference:SharedPreferences = context.getSharedPreferences("pref", Activity.MODE_PRIVATE)
        val editor = preference.edit()

        editor.putString("url", "")
        editor.apply()
    }

    // 좋아요
    fun checkLike(url: String){
        val email: String = FirebaseAuth.getInstance().currentUser?.email.toString()
        val data = LikeData(songUrl = url, email = email)
        FirebaseDatabase.getInstance().getReference("Like").push().setValue(data)
    }

    fun uncheckLike(key: String){
        FirebaseDatabase.getInstance().getReference("Like").child(key).removeValue()
    }

    fun putTrack(key: String, url: String){
        FirebaseDatabase.getInstance().getReference("PlayLists_song").orderByChild("key_songUrl")
            .equalTo(key + "_" + url).limitToFirst(1).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.children.iterator().hasNext()){
                        // exist
                        Log.i("album_exist", "already exist")
                    } else {
                        // not exist
                        val data = AlbumToSongData(key = key, songUrl = url, time = Command().getTime2())
                        FirebaseDatabase.getInstance().getReference("PlayLists_song").push().setValue(data)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}