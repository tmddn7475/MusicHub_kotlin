package com.example.musichub.Activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseBooleanArray
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musichub.Adapter.Base.AlbumEditAdapter
import com.example.musichub.Data.AlbumData
import com.example.musichub.Data.AlbumToSongData
import com.example.musichub.Data.MusicData
import com.example.musichub.R
import com.example.musichub.databinding.ActivityAlbumEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

@SuppressLint("UseSwitchCompatOrMaterialCode", "ClickableViewAccessibility")
class AlbumEditActivity : AppCompatActivity() {

    lateinit var binding: ActivityAlbumEditBinding
    lateinit var albumEditAdapter: AlbumEditAdapter
    lateinit var dialog: Dialog
    private var albumKey: String = ""
    val list: MutableList<MusicData> = mutableListOf<MusicData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // dialog
        dialog = Dialog(this@AlbumEditActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)

        albumKey = intent.getStringExtra("key").toString()
        albumEditAdapter = AlbumEditAdapter(list)
        getData(albumKey)

        // 설명 터치시 editText가 스크롤 되도록 설정
        binding.editListDescription.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (binding.editListDescription.hasFocus()) {
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

        binding.editListDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length: Int = binding.editListDescription.text.length
                binding.descriptionLength.text = "$length / 2000"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 리스트에서 체크 된 걸 제거
        binding.albumEditDelete.setOnClickListener{
            val alertEx: AlertDialog.Builder = AlertDialog.Builder(this@AlbumEditActivity)
            alertEx.setMessage(getString(R.string.delete_selected_track))
            alertEx.setNegativeButton(getString(R.string.yes)){ _, _ ->
                val checkItems: SparseBooleanArray = binding.albumEditList.checkedItemPositions
                for(i in list.indices){
                    if(checkItems.get(i)){
                        list.removeAt(i)
                    }
                }
                binding.albumEditList.clearChoices()
                albumEditAdapter.notifyDataSetChanged()
            }
            alertEx.setPositiveButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            val alert = alertEx.create()
            alert.show()
        }

        // 저장
        binding.albumEditSave.setOnClickListener{
            albumEdit()
        }

        binding.backBtn.setOnClickListener{
            finish()
        }
    }

    private fun albumEdit(){
        dialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["listName"] = binding.editListName.text.toString()
        hashMap["description"] = binding.editListDescription.text.toString()
        if(binding.editSet.isChecked){
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
                    Toast.makeText(this@AlbumEditActivity, getString(R.string.update_album), Toast.LENGTH_SHORT).show()
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
                            binding.editListName.setText(data.listName)
                            binding.editListDescription.setText(data.description)

                            if(data.list_mode == "public"){
                                binding.editSet.isChecked = true
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
                        binding.albumEditList.adapter = albumEditAdapter
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