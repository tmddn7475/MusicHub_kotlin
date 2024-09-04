package com.example.musichub.Adapter.Base

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.musichub.Data.AlbumData
import com.example.musichub.R

class GridViewAdapter(val context: Context, val items: MutableList<AlbumData>): BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return (convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.grid_item, parent, false)).apply {
            val gridImage:ImageView = findViewById(R.id.grid_image)
            val gridName:TextView = findViewById(R.id.grid_name)
            val gridArtist:TextView = findViewById(R.id.grid_artist)

            Glide.with(context).load(items[position].imageUrl).into(gridImage)
            gridName.text = items[position].listName
            gridArtist.text = items[position].nickname
        }
    }
}