package com.example.musichub.Fragment1

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.musichub.Adapter.Base.MusicListAdapter
import com.example.musichub.Activity.AlbumEditActivity
import com.example.musichub.Object.Command
import com.example.musichub.Data.AlbumData
import com.example.musichub.Data.AlbumToSongData
import com.example.musichub.Data.MusicData
import com.example.musichub.Interface.MusicListListener
import com.example.musichub.Interface.MusicListener
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.RoomDB.PlaylistDatabase
import com.example.musichub.RoomDB.PlaylistEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class AlbumFragment : Fragment(), MusicListListener {

    lateinit var list_thumbnail: ImageView
    lateinit var list_back_btn: ImageView
    lateinit var list_edit_btn: ImageView
    lateinit var list_name: TextView
    lateinit var list_artist: TextView
    lateinit var list_track_num: TextView
    lateinit var list_play_btn: ImageView
    lateinit var list_description: TextView
    lateinit var list_show_more: TextView
    lateinit var list_track: ListView
    lateinit var musicListAdapter: MusicListAdapter
    lateinit var musicListener: MusicListener

    var track_num: Int = 0
    var list = mutableListOf<MusicData>()
    private var db: PlaylistDatabase? = null
    private val myEmail: String = FirebaseAuth.getInstance().currentUser?.email.toString()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            musicListener = context as MusicListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_album, container, false)

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)
        dialog.show()

        val key: String = arguments?.getString("key").toString()
        db = PlaylistDatabase.getInstance(requireContext())

        list_thumbnail = v.findViewById(R.id.list_thumbnail)
        list_back_btn = v.findViewById(R.id.list_back_btn)
        list_edit_btn = v.findViewById(R.id.list_edit_btn)
        list_name = v.findViewById(R.id.list_name)
        list_artist = v.findViewById(R.id.list_artist)
        list_play_btn = v.findViewById(R.id.list_play_btn)
        list_track_num = v.findViewById(R.id.list_track_num)
        list_description = v.findViewById(R.id.list_description)
        list_show_more = v.findViewById(R.id.list_show_more)
        list_track = v.findViewById(R.id.list_track)

        musicListAdapter = MusicListAdapter(list, this)

        list_back_btn.setOnClickListener{
            back()
        }

        // 앨범 수정
        list_edit_btn.setOnClickListener{
            val intent = Intent(requireContext(), AlbumEditActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
        }

        list_show_more.setOnClickListener{
            showMore()
        }

        // 앨범 재생
        list_play_btn.setOnClickListener{
            playAlbum()
            musicListener.playMusic(list[0].songUrl)
            Toast.makeText(requireContext(), "곡이 리스트에 추가되었습니다\n" + "중복이 있을 경우 추가되지 않습니다", Toast.LENGTH_SHORT).show()
        }

        // 앨범 정보
        FirebaseDatabase.getInstance().getReference("PlayLists").orderByKey().equalTo(key).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val data = ds.getValue<AlbumData>()
                        if(data != null){
                            list_name.text = data.listName
                            list_artist.text = data.nickname
                            list_description.text = data.description
                            Glide.with(requireContext()).load(data.imageUrl).into(list_thumbnail)

                            if(data.description == ""){
                                list_description.visibility = View.GONE
                            }
                            if(data.email == myEmail){
                                list_edit_btn.visibility = View.VISIBLE
                            }
                        }
                    }

                    if(list_description.lineCount < 2) {
                        list_show_more.visibility = View.GONE
                    } else {
                        list_show_more.visibility = View.VISIBLE
                        showMore()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // 앨범 수록곡
        FirebaseDatabase.getInstance().getReference("PlayLists_song").orderByChild("key").equalTo(key)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()
                    track_num = 0
                    if(snapshot.children.iterator().hasNext()){
                        for(ds: DataSnapshot in snapshot.children){
                            val data = ds.getValue<AlbumToSongData>()
                            if(data != null){
                                getTracks(data.songUrl, data.time)
                            }
                        }
                        dialog.dismiss()
                    } else {
                        list_track_num.text = "0곡"
                        dialog.dismiss()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        return v
    }

    private fun getTracks(url: String, time: String){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val data = ds.getValue<MusicData>()
                        if(data != null){
                            data.time = time
                            list.add(data)
                            track_num++
                        }
                    }
                    list_track_num.text = "${track_num}곡"
                    musicListAdapter.sort()
                    list_track.adapter = musicListAdapter
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    @SuppressLint("SetTextI18n")
    private fun showMore(){
        if(list_description.maxLines == 1){
            list_description.maxLines = Int.MAX_VALUE
            list_description.ellipsize = null
            list_show_more.text = "닫기"
        } else {
            list_description.maxLines = 1
            list_description.ellipsize = TextUtils.TruncateAt.END
            list_show_more.text = "더 보기"
        }
    }

    private fun playAlbum(){
        val r = Runnable {
            for(i in list.indices){
                val data = PlaylistEntity(songUrl = list[i].songUrl, time = Command.getTime2())
                db?.musicDAO()?.saveSong(data)!!
                Thread.sleep(1000)
            }
        }
        val thread = Thread(r)
        thread.start()
    }

    override fun sendEtc(message: String) {
        val mainActivity = (activity as MainActivity)
        val fragmentManager = mainActivity.supportFragmentManager
        val etcFragment = mainActivity.etcFragment

        if (!etcFragment.isAdded) {
            val bundle = Bundle()
            bundle.putString("url", message)
            etcFragment.setArguments(bundle)
            etcFragment.show(fragmentManager, etcFragment.getTag())
        }
    }

    private fun back(){
        val mainActivity = (activity as MainActivity)
        val fragmentManager = mainActivity.supportFragmentManager
        fragmentManager.beginTransaction().remove(this).commit()
        fragmentManager.popBackStack()
    }
}