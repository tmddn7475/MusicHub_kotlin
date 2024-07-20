package com.example.musichub.Adapter.Base

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musichub.Data.MusicData
import com.example.musichub.Interface.MusicListener
import com.example.musichub.R
import jp.wasabeef.glide.transformations.BlurTransformation
import java.util.Calendar

class FeedListAdapter(val list: MutableList<MusicData>, val musicListener: MusicListener): BaseAdapter() {
    val ONE_DAY:Int = 24 * 60 * 60 * 1000

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("CheckResult")
    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        return (view ?: LayoutInflater.from(parent?.context).inflate(R.layout.feed_list_layout, parent, false)).apply {
            val data:MusicData = list[position]

            val feed_post: TextView = findViewById(R.id.feed_post)
            val feed_song_duration: TextView = findViewById(R.id.feed_song_duration)
            val feed_song_name: TextView = findViewById(R.id.feed_song_name)
            val feed_play_btn:ImageView = findViewById(R.id.feed_play_btn)
            val feed_song_image: ImageView = findViewById(R.id.feed_song_image)
            val feed_blur_image: ImageView = findViewById(R.id.feed_blur_image)

            feed_song_name.text = data.songName
            feed_song_duration.text = data.songDuration
            feed_post.text = getDday(data.time, data.songArtist)

            Glide.with(this).load(data.imageUrl).into(feed_song_image)
            Glide.with(this).load(data.imageUrl) // 이미지 블러처리
                .apply(RequestOptions.bitmapTransform(BlurTransformation(10, 3))).into(feed_blur_image)

            feed_play_btn.setOnClickListener{
                musicListener.playMusic(data.songUrl)
            }

            feed_post.setSingleLine(true)
            feed_post.ellipsize = TextUtils.TruncateAt.MARQUEE
            feed_post.isSelected = true
            feed_song_name.setSingleLine(true)
            feed_song_name.ellipsize = TextUtils.TruncateAt.MARQUEE
            feed_song_name.isSelected = true
        }
    }

    fun sort() {
        val comparator: Comparator<MusicData> =
            Comparator { prod1: MusicData, prod2: MusicData -> prod1.time.compareTo(prod2.time) }
        list.sortWith(comparator.reversed())
    }

    private fun getDday(time:String, artist:String): String{
        val times = time.split("/")

        val dDayCalendar = Calendar.getInstance()

        // 입력 받은 날짜로 설정한다
        dDayCalendar.set(times[0].toInt(), times[1].toInt(), times[2].toInt())

        // millisecond 으로 환산한 뒤 입력한 날짜에서 현재 날짜의 차를 구한다
        val dDay: Long = dDayCalendar.timeInMillis / ONE_DAY
        val today: Long = Calendar.getInstance().timeInMillis / ONE_DAY
        var result = today - dDay
        val goalDate: String

        if (result <= 1) {
            goalDate = "$artist posted a track $result day ago"
        } else if (result <= 30) {
            goalDate = "$artist posted a track $result days ago"
        } else if (result <= 365) {
            result /= 30
            goalDate = if (result <= 1) {
                "$artist posted a track $result month ago"
            } else {
                "$artist posted a track $result months ago"
            }
        } else {
            result /= 365
            goalDate = if (result <= 1) {
                "$artist posted a track $result year ago"
            } else {
                "$artist posted a track $result years ago"
            }
        }

        return goalDate
    }
}