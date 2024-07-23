package com.example.musichub.Adapter.Base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.musichub.Data.MusicData
import com.example.musichub.R

class AlbumEditAdapter(val list: MutableList<MusicData>): BaseAdapter() {
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return (convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.album_edit_list_layout, parent, false)).apply {
            val songThumbnail: ImageView = findViewById(R.id.songThumbnail)
            val songName: TextView = findViewById(R.id.songName)
            val artistName: TextView = findViewById(R.id.artistName)

            if(list[position].imageUrl != "") {
                Glide.with(context).load(list[position].imageUrl).into(songThumbnail)
            } else {
                songThumbnail.setImageResource(R.drawable.ic_baseline_library_music_24)
            }
            songName.text = list[position].songName
            artistName.text = list[position].songArtist
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