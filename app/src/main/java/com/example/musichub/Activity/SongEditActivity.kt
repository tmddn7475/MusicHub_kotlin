package com.example.musichub.Activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musichub.Data.MusicData
import com.example.musichub.R
import com.example.musichub.databinding.ActivitySongEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage

class SongEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySongEditBinding
    lateinit var progressDialog: Dialog

    var songKey: String = ""
    var imageUrl: String = ""
    private val categoryArr:Array<String> = arrayOf("None", "Ambient", "Classical", "Dance & EDM",
        "Disco", "Hip hop", "Jazz", "R&B", "Reggae", "Rock")

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = Dialog(this)
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog.setContentView(R.layout.progress_layout2)
        progressDialog.setCancelable(false)

        val songUrl: String = intent.getStringExtra("url").toString()

        getData(songUrl)

        // 곡 설명 터치시 editText가 스크롤 되도록 설정
        binding.songEditDescription.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (binding.songEditDescription.hasFocus()) {
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
        binding.songEditDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length: Int = binding.songEditDescription.text.length
                binding.songEditLength.text = "$length / 2000"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 카테고리
        binding.songEditCategory.setOnClickListener{
            AlertDialog.Builder(this@SongEditActivity)
                .setItems(categoryArr) { _, which ->
                    binding.songEditCategory.setText(categoryArr[which])
                }.show()
        }

        binding.editSaveBtn.setOnClickListener{
            progressDialog.show()
            saveEdit()
        }

        binding.songDelete.setOnClickListener{
            val alert_ex:AlertDialog.Builder = AlertDialog.Builder(this@SongEditActivity)
            alert_ex.setMessage(getString(R.string.track_delete_alert))
            alert_ex.setNegativeButton(getString(R.string.yes)) { _, _ ->
                progressDialog.show()
                deleteSong(songKey, songUrl, imageUrl)
            }
            alert_ex.setPositiveButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            val alert = alert_ex.create()
            alert.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
            alert.show()
        }

        binding.songEditBackBtn.setOnClickListener{
            finish()
        }
    }

    private fun saveEdit(){
        val hashMap = HashMap<String, Any>()
        hashMap["songName"] = binding.songEditName.text.toString()
        hashMap["songCategory"] = binding.songEditCategory.text.toString()
        hashMap["songInfo"] = binding.songEditDescription.text.toString()

        FirebaseDatabase.getInstance().getReference("Songs").child(songKey).updateChildren(hashMap)
        Toast.makeText(this, getString(R.string.update_track), Toast.LENGTH_SHORT).show()
        progressDialog.dismiss()
        finish()
    }

    private fun deleteSong(songKey: String, songUrl: String, imageUrl: String){
        FirebaseDatabase.getInstance().getReference("Songs").child(songKey).removeValue()
        FirebaseStorage.getInstance().getReferenceFromUrl(songUrl).delete()
        FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete()

        FirebaseDatabase.getInstance().getReference("History").orderByChild("songUrl").equalTo(songUrl)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val key = ds.key.toString()
                        FirebaseDatabase.getInstance().getReference("History").child(key).removeValue()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SongEditActivity, "error", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            })

        FirebaseDatabase.getInstance().getReference("Like").orderByChild("songUrl").equalTo(songUrl)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val key = ds.key.toString()
                        FirebaseDatabase.getInstance().getReference("Like").child(key).removeValue()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SongEditActivity, "error", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            })

        FirebaseDatabase.getInstance().getReference("PlayLists_song").orderByChild("songUrl").equalTo(songUrl)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val key = ds.key.toString()
                        FirebaseDatabase.getInstance().getReference("PlayLists_song").child(key).removeValue()
                    }
                    Toast.makeText(this@SongEditActivity, getString(R.string.delete_track), Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                    finish()
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SongEditActivity, getString(R.string.try_again), Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            })
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
                            binding.songEditName.setText(data.songName)
                            binding.songEditCategory.setText(data.songCategory)
                            binding.songEditDescription.setText(data.songInfo)
                            binding.songEditLength.text = data.songInfo.length.toString() + " / 2000"
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}