package com.example.musichub.Fragment2

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.media3.session.MediaController
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.musichub.Adapter.ItemTouchHelperCallBack
import com.example.musichub.Adapter.Recycler.PlayListAdapter
import com.example.musichub.Data.MusicData
import com.example.musichub.Interface.MusicListener
import com.example.musichub.Interface.PlayListListener
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.RoomDB.PlaylistDatabase
import com.example.musichub.databinding.FragmentPlaylistBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class PlaylistFragment : BottomSheetDialogFragment(), PlayListListener {

    private var _binding: FragmentPlaylistBinding? = null
    val binding get() = _binding!!
    var db:PlaylistDatabase? = null
    lateinit var mediaController: MediaController
    lateinit var playListAdapter: PlayListAdapter

    var list = ArrayList<MusicData>()
    private lateinit var musicListener: MusicListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            musicListener = context as MusicListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString())
        }
    }

    fun setController(controller: MediaController?){
        if (controller != null) {
            mediaController = controller
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)

        // room db
        db = PlaylistDatabase.getInstance(requireContext())
        list.clear()
        val run = Runnable {
            try {
                val playlistData = db?.musicDAO()?.getPlaylist()!!
                for(i:Int in playlistData.indices){
                    getTrack(playlistData[i].songUrl, playlistData[i].time)
                    Log.i("time", playlistData[i].time)
                }
            } catch (e: Exception) {
                Log.d("tag", "Error - $e")
            }
        }
        val thread = Thread(run)
        thread.start()

        playListAdapter = PlayListAdapter(list, this, db)
        val helper = ItemTouchHelper(ItemTouchHelperCallBack(playListAdapter))
        helper.attachToRecyclerView(binding.playlistRecycler)

        // seekbar
        binding.playlistProgress.progress = mediaController.currentPosition.toInt()
        binding.playlistProgress.max = mediaController.duration.toInt()

        binding.playlistProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaController.seekTo(seekBar?.progress!!.toLong())
            }

        })

        // 재생 버튼
        if(mediaController.isPlaying){
            binding.playlistPlayBtn.setImageResource(R.drawable.baseline_pause_24)
        } else {
            binding.playlistPlayBtn.setImageResource(R.drawable.baseline_play_arrow_24)
        }

        binding.playlistPlayBtn.setOnClickListener{
            if(mediaController.isPlaying){
                mediaController.pause()
                binding.playlistPlayBtn.setImageResource(R.drawable.baseline_play_arrow_24)
            } else {
                mediaController.play()
                binding.playlistPlayBtn.setImageResource(R.drawable.baseline_pause_24)
            }
        }

        // 다음 곡
        binding.playlistSkipNextBtn.setOnClickListener{
            musicListener.nextMusic()
            mediaController.play()
            if(isAdded){
                binding.playlistPlayBtn.setImageResource(R.drawable.baseline_pause_24)
            }
        }

        // 전 곡
        binding.playlistSkipPreviousBtn.setOnClickListener{
            musicListener.prevMusic()
            mediaController.play()
            if(isAdded){
                binding.playlistPlayBtn.setImageResource(R.drawable.baseline_pause_24)
            }
        }

        // mediaFragment
        binding.playlistMediaBtn.setOnClickListener{
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val mediaFragment: MediaFragment = mainActivity.mediaFragment

            mediaFragment.setController(mediaController)
            mediaFragment.show(fragmentManager, mediaFragment.tag)
            dismiss()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetBehavior = BottomSheetBehavior.from<View>(view.parent as View)
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
        bottomSheetBehavior.peekHeight = view.measuredHeight
        bottomSheetBehavior.isDraggable = false

        view.findViewById<ImageView>(R.id.play_list_down_btn).setOnClickListener{
            dismiss()
        }
    }

    // 플레이리스트의 곡 가져오기
    private fun getTrack(url:String, time:String) {
        val mainActivity = (activity as MainActivity)

        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val mld = ds.getValue<MusicData>()
                        if (mld != null) {
                            mld.time = time
                            list.add(mld)
                        }
                    }
                    playListAdapter.sort()
                    playListAdapter.getNumber(mainActivity.currentUrl)
                    binding.playlistRecycler.adapter = playListAdapter
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun setUpCurrent(url: String){
        playListAdapter.getNumber(url)
    }

    // 리스트에서 클릭한 음악을 인터페이스를 통해 보냄
    override fun sendMusic(message: String) {
        musicListener.playMusic(message)
        if(isAdded){
            binding.playlistPlayBtn.setImageResource(R.drawable.baseline_pause_24)
        }
    }

    // EtcFragment
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}