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
import androidx.recyclerview.widget.RecyclerView
import com.example.musichub.Adapter.ItemTouchHelperCallBack
import com.example.musichub.Adapter.Recycler.PlayListAdapter
import com.example.musichub.Data.MusicData
import com.example.musichub.Interface.MusicListener
import com.example.musichub.Interface.PlayListListener
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.RoomDB.PlaylistDatabase
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class PlaylistFragment : BottomSheetDialogFragment(), PlayListListener {

    var db:PlaylistDatabase? = null

    lateinit var playlistRecyclerView: RecyclerView
    lateinit var mediaController: MediaController
    lateinit var playListAdapter: PlayListAdapter
    lateinit var playlist_progress: SeekBar
    lateinit var playlist_media_btn: ImageView
    lateinit var playlist_play_btn: ImageView
    lateinit var playlist_skip_previous_btn: ImageView
    lateinit var playlist_skip_next_btn: ImageView

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
    ): View? {
        val v = inflater.inflate(R.layout.fragment_playlist, container, false)

        playlist_play_btn = v.findViewById(R.id.playlist_play_btn)
        playlist_skip_next_btn = v.findViewById(R.id.playlist_skip_next_btn)
        playlist_skip_previous_btn = v.findViewById(R.id.playlist_skip_previous_btn)
        playlist_media_btn = v.findViewById(R.id.playlist_media_btn)
        playlist_progress = v.findViewById(R.id.playlist_progress)
        playlistRecyclerView = v.findViewById(R.id.playlist_recycler)

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
        helper.attachToRecyclerView(playlistRecyclerView)

        // seekbar
        playlist_progress.progress = mediaController.currentPosition.toInt()
        playlist_progress.max = mediaController.duration.toInt()

        playlist_progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaController.seekTo(seekBar?.progress!!.toLong())
            }

        })

        // 재생 버튼
        if(mediaController.isPlaying){
            playlist_play_btn.setImageResource(R.drawable.pause)
        } else {
            playlist_play_btn.setImageResource(R.drawable.play_arrow)
        }

        playlist_play_btn.setOnClickListener{
            if(mediaController.isPlaying){
                mediaController.pause()
                playlist_play_btn.setImageResource(R.drawable.play_arrow)
            } else {
                mediaController.play()
                playlist_play_btn.setImageResource(R.drawable.pause)
            }
        }

        // 다음 곡
        playlist_skip_next_btn.setOnClickListener{
            musicListener.nextMusic()
            mediaController.play()
            if(isAdded){
                playlist_play_btn.setImageResource(R.drawable.pause)
            }
        }

        // 전 곡
        playlist_skip_previous_btn.setOnClickListener{
            musicListener.prevMusic()
            mediaController.play()
            if(isAdded){
                playlist_play_btn.setImageResource(R.drawable.pause)
            }
        }

        playlist_media_btn.setOnClickListener{
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val mediaFragment: MediaFragment = mainActivity.mediaFragment

            mediaFragment.setController(mediaController)
            mediaFragment.show(fragmentManager, mediaFragment.tag)
            dismiss()
        }

        return v
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
                    playlistRecyclerView.adapter = playListAdapter
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun setUpCurrent(url: String){
        playListAdapter.getNumber(url)
    }

    override fun sendMusic(message: String) {
        musicListener.playMusic(message)
        if(isAdded){
            playlist_play_btn.setImageResource(R.drawable.pause)
        }
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
}