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
import com.example.musichub.databinding.FragmentAlbumBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class AlbumFragment : Fragment(), MusicListListener {

    private var _binding: FragmentAlbumBinding? = null
    private val binding get() = _binding!!
    lateinit var musicListAdapter: MusicListAdapter
    lateinit var musicListener: MusicListener

    var trackNum: Int = 0
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
    ): View {
        _binding = FragmentAlbumBinding.inflate(inflater, container, false)

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)
        dialog.show()

        val key: String = arguments?.getString("key").toString()
        db = PlaylistDatabase.getInstance(requireContext())

        musicListAdapter = MusicListAdapter(list, this)

        binding.albumBackBtn.setOnClickListener{
            back()
        }

        // 앨범 수정
        binding.albumEditBtn.setOnClickListener{
            val intent = Intent(requireContext(), AlbumEditActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
        }

        binding.albumShowMore.setOnClickListener{
            showMore()
        }

        // 앨범 재생
        binding.albumPlayBtn.setOnClickListener{
            playAlbum()
            musicListener.playMusic(list[0].songUrl)
            Toast.makeText(requireContext(), getString(R.string.song_to_list), Toast.LENGTH_SHORT).show()
        }

        // 앨범 정보
        FirebaseDatabase.getInstance().getReference("PlayLists").orderByKey().equalTo(key).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val binding = getBind() ?: return
                        val data = ds.getValue<AlbumData>()
                        if(data != null){
                            binding.albumName.text = data.listName
                            binding.albumArtist.text = data.nickname
                            binding.albumDescription.text = data.description
                            Glide.with(requireContext()).load(data.imageUrl).into(binding.albumThumbnail)

                            if(data.description == ""){
                                binding.albumDescription.visibility = View.GONE
                            }
                            if(data.email == myEmail){
                                binding.albumEditBtn.visibility = View.VISIBLE
                            }
                        }
                    }

                    if(binding.albumDescription.lineCount < 2) {
                        binding.albumShowMore.visibility = View.GONE
                    } else {
                        binding.albumShowMore.visibility = View.VISIBLE
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
                    val binding = getBind() ?: return
                    list.clear()
                    trackNum = 0
                    if(snapshot.children.iterator().hasNext()){
                        for(ds: DataSnapshot in snapshot.children){
                            val data = ds.getValue<AlbumToSongData>()
                            if(data != null){
                                getTracks(data.songUrl, data.time)
                            }
                        }
                        dialog.dismiss()
                    } else {
                        binding.albumTrackNum.text = "0 track"
                        dialog.dismiss()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        return binding.root
    }

    private fun getTracks(url: String, time: String){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val binding = getBind() ?: return
                    for(ds: DataSnapshot in snapshot.children){
                        val data = ds.getValue<MusicData>()
                        if(data != null){
                            data.time = time
                            list.add(data)
                            trackNum++
                        }
                    }
                    if(trackNum < 2) {
                        binding.albumTrackNum.text = "${trackNum} track"
                    } else {
                        binding.albumTrackNum.text = "${trackNum} tracks"
                    }
                    musicListAdapter.sort()
                    binding.albumTrack.adapter = musicListAdapter
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    @SuppressLint("SetTextI18n")
    private fun showMore(){
        if(binding.albumDescription.maxLines == 1){
            binding.albumDescription.maxLines = Int.MAX_VALUE
            binding.albumDescription.ellipsize = null
            binding.albumShowMore.text = getString(R.string.show_less)
        } else {
            binding.albumDescription.maxLines = 1
            binding.albumDescription.ellipsize = TextUtils.TruncateAt.END
            binding.albumShowMore.text = getString(R.string.show_more)
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

    private fun getBind(): FragmentAlbumBinding? {
        return _binding
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}