package com.example.musichub.Adapter.Recycler

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musichub.Data.MusicData
import com.example.musichub.Interface.PlayListListener
import com.example.musichub.Interface.ItemTouchHelperListener
import com.example.musichub.R
import com.example.musichub.RoomDB.PlaylistDatabase

class PlayListAdapter(val list: ArrayList<MusicData>, var playListListener: PlayListListener, val db: PlaylistDatabase?) :
    RecyclerView.Adapter<PlayListAdapter.ViewHolder>(), ItemTouchHelperListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_layout, parent, false)
        return ViewHolder(view)
    }

    private var selected: Int = -1

    @SuppressLint("NotifyDataSetChanged", "RecyclerView")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == selected) {
            holder.songName.setTextColor(Color.parseColor("#00B3EF"))
        } else {
            holder.songName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text))
        }

        holder.songName.text = list[position].songName
        holder.songArtist.text = list[position].songArtist
        Glide.with(holder.itemView).load(list[position].imageUrl).into(holder.songImage)

        holder.itemView.setOnClickListener {
            selected = position
            notifyDataSetChanged()
            playListListener.sendMusic(list[position].songUrl)
        }

        // EtcFragment
        holder.songEtc.setOnClickListener {
            playListListener.sendEtc(list[position].songUrl)
        }
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songImage: ImageView = itemView.findViewById(R.id.playlist_songThumbnail)
        val songName: TextView = itemView.findViewById(R.id.playlist_songName)
        val songArtist: TextView = itemView.findViewById(R.id.playlist_songArtist)
        val songEtc: ImageView = itemView.findViewById(R.id.playlist_etc)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getNumber(url:String) {
        for(i in list.indices){
            if(list[i].songUrl == url){
                selected = i
                break
            }
        }
        notifyDataSetChanged()
    }

    fun sort(){
        val comparator: Comparator<MusicData> =
            Comparator { prod1: MusicData, prod2: MusicData -> prod1.time.compareTo(prod2.time) }
        list.sortWith(comparator)
    }

    override fun onItemSwipe(position: Int) {
        removeItem(position)
        notifyItemRemoved(position)
    }

    private fun removeItem(position: Int){
        val data:String = list[position].songUrl

        val r = Runnable {
            db?.musicDAO()?.deleteSong(data)!!
            list.removeAt(position)
        }
        val thread = Thread(r)
        thread.start()
    }
}