package com.example.musichub.Activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseBooleanArray
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musichub.Adapter.Base.AlbumEditAdapter
import com.example.musichub.Data.AlbumData
import com.example.musichub.Data.AlbumToSongData
import com.example.musichub.Data.MusicData
import com.example.musichub.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

@SuppressLint("UseSwitchCompatOrMaterialCode", "ClickableViewAccessibility")
class AlbumEditActivity : AppCompatActivity() {

    lateinit var back_btn: ImageView
    lateinit var album_edit_save: TextView
    lateinit var edit_list_name: EditText
    lateinit var edit_list_description: EditText
    lateinit var description_length: TextView
    lateinit var album_edit_set: Switch
    lateinit var album_edit_list: ListView
    lateinit var album_edit_delete: TextView
    lateinit var albumEditAdapter: AlbumEditAdapter
    lateinit var dialog: Dialog

    var albumKey: String = ""
    val list: MutableList<MusicData> = mutableListOf<MusicData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_edit)

        albumKey = intent.getStringExtra("key").toString()

        dialog = Dialog(this@AlbumEditActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)

        back_btn = findViewById(R.id.back_btn)
        album_edit_save = findViewById(R.id.album_edit_save)
        edit_list_name = findViewById(R.id.edit_list_name)
        edit_list_description = findViewById(R.id.edit_list_description)
        description_length = findViewById(R.id.description_length)
        album_edit_set = findViewById(R.id.edit_set)
        album_edit_list = findViewById(R.id.album_edit_list)
        album_edit_delete = findViewById(R.id.album_edit_delete)

        albumEditAdapter = AlbumEditAdapter(list)

        getData(albumKey)

        // 설명 터치시 editText가 스크롤 되도록 설정
        edit_list_description.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (edit_list_description.hasFocus()) {
                    v!!.parent.requestDisallowInterceptTouchEvent(true)
                    when (event!!.action and MotionEvent.ACTION_MASK) {
                        MotionEvent.ACTION_SCROLL -> {
                            v.parent.requestDisallowInterceptTouchEvent(false)
                            return true
                        }
                    }
                }
                return false
            }
        })

        edit_list_description.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length: Int = edit_list_description.text.length
                description_length.text = "$length / 2000"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 리스트에서 체크 된 걸 제거
        album_edit_delete.setOnClickListener{
            val alertEx: AlertDialog.Builder = AlertDialog.Builder(this@AlbumEditActivity)
            alertEx.setMessage("선택된 곡들이 삭제됩니다")
            alertEx.setNegativeButton("확인"){ _, _ ->
                val checkItems: SparseBooleanArray = album_edit_list.checkedItemPositions
                for(i in list.indices){
                    if(checkItems.get(i)){
                        list.removeAt(i)
                    }
                }
                album_edit_list.clearChoices()
                albumEditAdapter.notifyDataSetChanged()
            }
            alertEx.setPositiveButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            val alert = alertEx.create()
            alert.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
            alert.show()
        }

        // 저장
        album_edit_save.setOnClickListener{
            val alertEx2: AlertDialog.Builder = AlertDialog.Builder(this@AlbumEditActivity)
            alertEx2.setMessage("저장하시겠습니까?")
            alertEx2.setNegativeButton("네") { _, _ ->
                albumEdit()
            }
            alertEx2.setPositiveButton("아니요") { dialog, _ ->
                dialog.dismiss()
            }
            val alert = alertEx2.create()
            alert.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
            alert.show()
        }

        back_btn.setOnClickListener{
            finish()
        }
    }

    private fun albumEdit(){
        dialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["listName"] = edit_list_name.text.toString()
        hashMap["description"] = edit_list_description.text.toString()
        if(album_edit_set.isChecked){
            hashMap["list_mode"] = "public"
        } else {
            hashMap["list_mode"] = "private"
        }
        FirebaseDatabase.getInstance().getReference("PlayLists").child(albumKey).updateChildren(hashMap)

        FirebaseDatabase.getInstance().getReference("PlayLists_song").orderByChild("key").equalTo(albumKey)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val data = ds.getValue<AlbumToSongData>()
                        val key = ds.key.toString()
                        if (data != null) {
                            if(!getExist().contains(data.songUrl)){
                                FirebaseDatabase.getInstance().getReference("PlayLists_song").child(key).removeValue()
                            }
                        }
                    }
                    Toast.makeText(this@AlbumEditActivity, "정보가 수정되었습니다", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    finish()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getData(key: String){
        FirebaseDatabase.getInstance().getReference("PlayLists").orderByKey().equalTo(key).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val data = ds.getValue<AlbumData>()
                        if (data != null) {
                            edit_list_name.setText(data.listName)
                            edit_list_description.setText(data.description)

                            if(data.list_mode == "public"){
                                album_edit_set.isChecked = true
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference("PlayLists_song").orderByChild("key").equalTo(key)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val data = ds.getValue<AlbumToSongData>()
                        if (data != null) {
                            getTracks(data.songUrl, data.time)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getTracks(url: String, time: String){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val data = ds.getValue<MusicData>()
                        if (data != null) {
                            data.time = time
                            list.add(data)
                        }
                        albumEditAdapter.sort()
                        album_edit_list.adapter = albumEditAdapter
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getExist():String {
        var str = ""
        for(i in list.indices){
            str += list[i].songUrl + "/"
        }
        return str
    }
}