package com.example.musichub.Adapter.Base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.musichub.R
import com.example.musichub.RoomDB.PlaylistDatabase

class SearchListAdapter(val list: MutableList<String>, val db: PlaylistDatabase?): BaseAdapter() {
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
        return (convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.search_recent_layout, parent, false)).apply {
            val search_text: TextView = findViewById(R.id.recent_text)
            val search_delete: ImageView = findViewById(R.id.recent_delete)

            search_text.text = list[position]

            search_delete.setOnClickListener{
                delete(list[position])
                list.removeAt(position)
                notifyDataSetChanged()
            }
        }
    }

    private fun delete(str: String){
        val r = Runnable {
            db?.musicDAO()?.deleteSearch(str)!!
        }
        val thread = Thread(r)
        thread.start()
    }
}