package com.example.musichub.Adapter.Base

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.musichub.Data.MusicData
import com.example.musichub.Fragment1.SongInfoFragment
import com.example.musichub.Interface.MusicListListener
import com.example.musichub.MainActivity
import com.example.musichub.R

class MusicListAdapter(val list: MutableList<MusicData>, var musicListListener: MusicListListener) : BaseAdapter() {
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return (convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.songs_list_layout, parent, false)).apply {
            val context = parent?.context

            val data:MusicData = list[position]

            val songDuration:TextView = findViewById(R.id.song_duration)
            val songArtist:TextView = findViewById(R.id.song_artist)
            val songName:TextView = findViewById(R.id.song_name)
            val songEtc:ImageView = findViewById(R.id.song_etc)
            val songThumbnail:ImageView = findViewById(R.id.song_thumnail)

            songName.text = data.songName
            songArtist.text = data.songArtist
            songDuration.text = data.songDuration
            Glide.with(this).load(data.imageUrl).into(songThumbnail)

            songThumbnail.setOnClickListener{
                val mainActivity: MainActivity = context as MainActivity
                val fragmentManager = mainActivity.supportFragmentManager
                val songInfoFragment = SongInfoFragment()

                val bundle = Bundle()
                bundle.putString("url", data.songUrl)
                songInfoFragment.setArguments(bundle)
                fragmentManager.beginTransaction().replace(R.id.container, songInfoFragment).addToBackStack(null).commit()
            }

            songEtc.setOnClickListener {
                musicListListener.sendEtc(data.songUrl)
            }

            songName.setSingleLine(true)
            songName.ellipsize = TextUtils.TruncateAt.MARQUEE
            songName.isSelected = true
        }
    }

    fun sort(){
        val comparator: Comparator<MusicData> =
            Comparator { prod1: MusicData, prod2: MusicData ->
                prod1.time.compareTo(prod2.time)
            }
        list.sortWith(comparator.reversed())
    }
}