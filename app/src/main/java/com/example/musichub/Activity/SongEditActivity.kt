package com.example.musichub.Activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.musichub.Command
import com.example.musichub.Data.MusicData
import com.example.musichub.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class SongEditActivity : AppCompatActivity() {

    lateinit var song_edit_back_btn: ImageView
    lateinit var edit_save_btn: ImageView
    lateinit var song_edit_name: EditText
    lateinit var song_edit_category: EditText
    lateinit var song_edit_description: EditText
    lateinit var song_edit_length: TextView
    lateinit var song_delete: TextView

    var songKey: String = ""
    var imageUrl: String = ""
    val category_arr:Array<String> = arrayOf("None", "Ambient", "Classical", "Dance & EDM",
        "Disco", "Hip hop", "Jazz", "R&B", "Reggae", "Rock")

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_edit)

        val songUrl: String = intent.getStringExtra("url").toString()

        song_edit_back_btn = findViewById(R.id.song_edit_back_btn)
        edit_save_btn = findViewById(R.id.edit_save_btn)
        song_edit_name = findViewById(R.id.song_edit_name)
        song_edit_category = findViewById(R.id.song_edit_category)
        song_edit_description = findViewById(R.id.song_edit_description)
        song_edit_length = findViewById(R.id.song_edit_length)
        song_delete = findViewById(R.id.song_delete)

        getData(songUrl)

        // 곡 설명 터치시 editText가 스크롤 되도록 설정
        song_edit_description.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (song_edit_description.hasFocus()) {
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

        // 글자 수 제한
        song_edit_description.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length: Int = song_edit_description.text.length
                song_edit_length.text = "$length / 2000"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 카테고리
        song_edit_category.setOnClickListener{
            AlertDialog.Builder(this@SongEditActivity)
                .setItems(category_arr) { dialog, which ->
                    song_edit_category.setText(category_arr[which])
                }.show()
        }

        edit_save_btn.setOnClickListener{
            val alert_ex:AlertDialog.Builder = AlertDialog.Builder(this@SongEditActivity)
            alert_ex.setMessage("저장하시겠습니까?")
            alert_ex.setNegativeButton("네") { dialog, which ->
                saveEdit()
            }
            alert_ex.setPositiveButton("아니요") { dialog, which ->
                dialog.dismiss()
            }
            val alert = alert_ex.create()
            alert.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
            alert.show()
        }

        song_delete.setOnClickListener{
            val alert_ex:AlertDialog.Builder = AlertDialog.Builder(this@SongEditActivity)
            alert_ex.setMessage("해당 곡을 삭제하시겠습니까?")
            alert_ex.setNegativeButton("네") { dialog, which ->

            }
            alert_ex.setPositiveButton("아니요") { dialog, which ->
                dialog.dismiss()
            }
            val alert = alert_ex.create()
            alert.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
            alert.show()
        }

        song_edit_back_btn.setOnClickListener{
            finish()
        }
    }

    private fun saveEdit(){
        val hashMap = HashMap<String, Any>()
        hashMap.put("songName", song_edit_name.text.toString())
        hashMap.put("songCategory", song_edit_category.text.toString())
        hashMap.put("songInfo", song_edit_description.text.toString())

        FirebaseDatabase.getInstance().getReference("Songs").child(songKey).updateChildren(hashMap)
        Toast.makeText(this, "저장되었습니다", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun getData(url: String){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val data = ds.getValue<MusicData>()
                        if(data != null){
                            songKey = ds.key.toString()
                            imageUrl = data.imageUrl
                            song_edit_name.setText(data.songName)
                            song_edit_category.setText(data.songCategory)
                            song_edit_description.setText(data.songInfo)
                            song_edit_length.text = data.songInfo.length.toString() + " / 2000"
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}