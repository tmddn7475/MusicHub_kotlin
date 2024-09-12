package com.example.musichub.Adapter.Base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.musichub.Data.AlbumData
import com.example.musichub.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class MyAlbumAdapter(val list: MutableList<AlbumData>, val keyList: MutableList<String>): BaseAdapter() {
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
        return (convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.album_layout, parent, false)).apply {
            val albumThumbnail:ImageView = findViewById(R.id.my_list_thumbnail)
            val albumLock:ImageView = findViewById(R.id.my_list_lock)
            val albumName:TextView = findViewById(R.id.my_list_name)
            val albumDelete:ImageView = findViewById(R.id.my_list_delete)

            Glide.with(context).load(list[position].imageUrl).into(albumThumbnail)
            albumName.text = list[position].listName

            if(list[position].list_mode == "private"){
                albumLock.visibility = View.VISIBLE
            } else {
                albumLock.visibility = View.GONE
            }

            albumDelete.setOnClickListener {
                val alert_ex: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
                alert_ex.setMessage(context.getString(R.string.album_delete_alert))
                alert_ex.setNegativeButton(context.getString(R.string.yes)) { _, _ ->
                    deleteAlbum(list[position].imageUrl, keyList[position])
                    list.removeAt(position)
                    keyList.removeAt(position)
                    notifyDataSetChanged()
                }
                alert_ex.setPositiveButton(context.getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
                val alert = alert_ex.create()
                alert.show()
            }
        }
    }

    private fun deleteAlbum(imageUrl: String, key:String){
        FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete()
        FirebaseDatabase.getInstance().getReference("PlayLists").child(key).removeValue()
        FirebaseDatabase.getInstance().getReference("PlayLists_song").orderByChild("key").equalTo(key)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val songKey = ds.key
                        if (songKey != null) {
                            FirebaseDatabase.getInstance().getReference("PlayLists_song").child(key).removeValue()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}