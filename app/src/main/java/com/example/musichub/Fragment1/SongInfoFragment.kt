package com.example.musichub.Fragment1

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.musichub.Data.MusicData
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import java.util.Calendar

class SongInfoFragment : Fragment() {

    val ONE_DAY: Int = 24 * 60 * 60 * 1000
    lateinit var info_song_name: TextView
    lateinit var info_song_artist: TextView
    lateinit var info_song_play: TextView
    lateinit var info_song_like: TextView
    lateinit var info_song_duration: TextView
    lateinit var info_song_time: TextView
    lateinit var info_song_desc: TextView
    lateinit var info_song_thumbnail: ImageView
    lateinit var info_edit_btn: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_song_info, container, false)

        val info_back_btn: ImageView = v.findViewById(R.id.info_back_btn)
        info_song_name = v.findViewById(R.id.info_song_name)
        info_song_artist = v.findViewById(R.id.info_song_artist)
        info_song_play = v.findViewById(R.id.info_song_play)
        info_song_like = v.findViewById(R.id.info_song_like)
        info_song_duration = v.findViewById(R.id.info_song_duration)
        info_song_time = v.findViewById(R.id.info_song_time)
        info_song_desc = v.findViewById(R.id.info_song_desc)
        info_song_thumbnail = v.findViewById(R.id.info_song_thumnail)
        info_edit_btn = v.findViewById(R.id.info_edit_btn)

        info_song_name.setSingleLine(true)
        info_song_name.ellipsize = TextUtils.TruncateAt.MARQUEE
        info_song_name.isSelected = true

        getData(arguments?.getString("url").toString())

        info_back_btn.setOnClickListener{
            back()
        }

        return v
    }

    private fun getData(url: String){
        FirebaseDatabase.getInstance().getReference("History").orderByChild("songUrl").equalTo(url)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var num = 0
                    for(ds in snapshot.children){
                        num += 1
                    }
                    info_song_play.text = num.toString()
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference("Like").orderByChild("songUrl").equalTo(url)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var num = 0
                    for(ds in snapshot.children){
                        num += 1
                    }
                    info_song_like.text = num.toString()
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds:DataSnapshot in snapshot.children){
                        val data = ds.getValue<MusicData>()

                        if (data != null) {
                            info_song_name.text = data.songName
                            info_song_artist.text = data.songArtist
                            info_song_duration.text = " · " + data.songDuration
                            info_song_desc.text = data.songInfo
                            Glide.with(requireContext()).load(data.imageUrl).into(info_song_thumbnail)
                            getDday(data.time, data.songCategory)

                            if(data.email == FirebaseAuth.getInstance().currentUser?.email.toString()){
                                info_edit_btn.visibility = View.VISIBLE
                            } else {
                                info_edit_btn.visibility = View.GONE
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun back(){
        val mainActivity = (activity as MainActivity)
        val fragmentManager = mainActivity.supportFragmentManager
        fragmentManager.beginTransaction().remove(this).commit()
        fragmentManager.popBackStack()
    }

    private fun getDday(time: String, category:String){
        val times = time.split("/")

        val dDayCalendar = Calendar.getInstance()
        // 입력 받은 날짜로 설정한다
        dDayCalendar.set(times[0].toInt(), times[1].toInt() - 1, times[2].toInt())

        // millisecond 으로 환산한 뒤 입력한 날짜에서 현재 날짜의 차를 구한다
        val dDay: Long = dDayCalendar.timeInMillis / ONE_DAY
        val today: Long = System.currentTimeMillis() / ONE_DAY
        var result = today - dDay
        val goalDate: String

        if (result <= 1) {
            goalDate = "$category · $result day ago"
            info_song_time.text = goalDate
        } else if (result <= 30) {
            goalDate = "$category · $result days ago"
            info_song_time.text = goalDate
        } else if (result <= 365) {
            result /= 30
            goalDate = if (result <= 1) {
                "$category · $result month ago"
            } else {
                "$category · $result months ago"
            }
            info_song_time.text = goalDate
        } else {
            result /= 365
            goalDate = if (result <= 1) {
                "$category · $result year ago"
            } else {
                "$category · $result years ago"
            }
            info_song_time.text = goalDate
        }
    }
}