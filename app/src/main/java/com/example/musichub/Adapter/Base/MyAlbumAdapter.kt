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
            val album_thumbnail:ImageView = findViewById(R.id.my_list_thumbnail)
            val album_lock:ImageView = findViewById(R.id.my_list_lock)
            val album_name:TextView = findViewById(R.id.my_list_name)
            val album_delete:ImageView = findViewById(R.id.my_list_delete)

            Glide.with(context).load(list[position].imageUrl).into(album_thumbnail)
            album_name.text = list[position].listName

            if(list[position].list_mode == "private"){
                album_lock.visibility = View.VISIBLE
            } else {
                album_lock.visibility = View.GONE
            }

            album_delete.setOnClickListener {
                val alert_ex: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
                alert_ex.setMessage("해당 앨범을 삭제하시겠습니까?")
                alert_ex.setNegativeButton("네") { dialog, _ ->
                    deleteAlbum(list[position].imageUrl, keyList[position])
                    list.removeAt(position)
                    keyList.removeAt(position)
                    notifyDataSetChanged()
                }
                alert_ex.setPositiveButton("아니요") { dialog, which ->
                    dialog.dismiss()
                }
                val alert = alert_ex.create()
                alert.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
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