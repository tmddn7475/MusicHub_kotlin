package com.example.musichub.Fragment2

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.media3.session.MediaController
import com.bumptech.glide.Glide
import com.example.musichub.Object.Command
import com.example.musichub.Data.AccountData
import com.example.musichub.Data.MusicData
import com.example.musichub.Fragment1.Account.AccountFragment
import com.example.musichub.Interface.MusicListener
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import de.hdodenhof.circleimageview.CircleImageView
import java.util.concurrent.TimeUnit

class MediaFragment(_musicListener:MusicListener) : BottomSheetDialogFragment() {

    lateinit var mediaController: MediaController
    lateinit var media_seekbar:SeekBar
    lateinit var media_thumbnail: ImageView
    lateinit var media_playlist_btn: ImageView
    lateinit var media_comment: ImageView
    lateinit var media_account: CircleImageView
    lateinit var media_like_btn: ImageView
    lateinit var media_song_name: TextView
    lateinit var media_song_artist: TextView
    lateinit var media_song_duration: TextView
    lateinit var media_song_current: TextView
    lateinit var data: MusicData

    var media_email: String = ""
    var like_check: Boolean = false
    var like_key: String = ""

    val musicListener = _musicListener

    fun setController(controller: MediaController?){
        if (controller != null) {
            mediaController = controller
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_media, container, false)

        val mainActivity = (activity as MainActivity)
        val media_etc_btn:ImageButton = v.findViewById(R.id.media_etc_btn)
        val media_play_btn:ImageView = v.findViewById(R.id.media_play_btn)
        val media_next_btn:ImageView = v.findViewById(R.id.media_next_btn)
        val media_previous_btn:ImageView = v.findViewById(R.id.media_previous_btn)

        media_seekbar = v.findViewById(R.id.media_seekbar)
        media_account = v.findViewById(R.id.media_account)
        media_comment = v.findViewById(R.id.media_comment)
        media_thumbnail = v.findViewById(R.id.media_song_thumnail)
        media_like_btn = v.findViewById(R.id.media_like_btn)
        media_song_name = v.findViewById(R.id.media_song_name)
        media_song_artist = v.findViewById(R.id.media_song_artist)
        media_song_duration = v.findViewById(R.id.media_song_duration)
        media_playlist_btn = v.findViewById(R.id.media_playlist_btn)
        media_song_current = v.findViewById(R.id.media_song_current)

        media_song_name.isSingleLine = true // 한줄로 표시하기
        media_song_name.ellipsize = TextUtils.TruncateAt.MARQUEE // 흐르게 만들기
        media_song_name.isSelected = true

        setUpCurrent(mainActivity.current_url)

        media_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaController.seekTo(seekBar?.progress!!.toLong())
            }
        })

        // Etc
        media_etc_btn.setOnClickListener{
            val fragmentManager = mainActivity.supportFragmentManager
            val etcFragment = mainActivity.etcFragment

            if (!etcFragment.isAdded) {
                val bundle = Bundle()
                bundle.putString("url", mainActivity.current_url)
                etcFragment.setArguments(bundle)
                etcFragment.show(fragmentManager, etcFragment.getTag())
            }
        }

        // 댓글
        media_comment.setOnClickListener{
            val fragmentManager = mainActivity.supportFragmentManager
            val commentFragment = CommentFragment()

            if (!commentFragment.isAdded) {
                val bundle = Bundle()
                bundle.putString("url", mainActivity.current_url)
                commentFragment.setArguments(bundle)
                commentFragment.show(fragmentManager, commentFragment.getTag())
            }
        }

        media_account.setOnClickListener{
            val fragmentManager = mainActivity.supportFragmentManager
            val accountFragment = AccountFragment()

            val bundle = Bundle()
            bundle.putString("email", media_email)
            accountFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit()
            dismiss()
        }

        // 좋아요
        media_like_btn.setOnClickListener{
            if(like_check){
                Command.uncheckLike(like_key)
                media_like_btn.setImageResource(R.drawable.baseline_favorite_border_24)
                like_check = false
            } else {
                Command.checkLike(mainActivity.current_url)
                media_like_btn.setImageResource(R.drawable.baseline_favorite_24)
                like_check = true
            }
        }

        // play 버튼
        if(mediaController.isPlaying){
            media_play_btn.setImageResource(R.drawable.pause)
        } else {
            media_play_btn.setImageResource(R.drawable.play_arrow)
        }

        media_play_btn.setOnClickListener{
            if(mediaController.isPlaying){
                mediaController.pause()
                media_play_btn.setImageResource(R.drawable.play_arrow)
            } else {
                mediaController.play()
                media_play_btn.setImageResource(R.drawable.pause)
            }
        }

        // 다음 곡
        media_next_btn.setOnClickListener{
            musicListener.nextMusic()
            media_seekbar.progress = 0
            mediaController.play()
            if(isAdded){
                media_play_btn.setImageResource(R.drawable.pause)
            }
        }

        // 전 곡
        media_previous_btn.setOnClickListener{
            musicListener.prevMusic()
            media_seekbar.progress = 0
            mediaController.play()
            if(isAdded){
                media_play_btn.setImageResource(R.drawable.pause)
            }
        }

        // playlist
        media_playlist_btn.setOnClickListener{
            val fragmentManager = mainActivity.supportFragmentManager
            val playlistFragment = mainActivity.playlistFragment

            playlistFragment.setController(mediaController)
            playlistFragment.show(fragmentManager, playlistFragment.tag)
            dismiss()
        }

        return v
    }

    @SuppressLint("SetTextI18n")
    fun setUpCurrent(url:String){
        val email = FirebaseAuth.getInstance().currentUser?.email

        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds in snapshot.children) {
                        val mld = ds.getValue<MusicData>()
                        if (mld != null) {
                            Glide.with(requireContext()).load(mld.imageUrl).into(media_thumbnail)
                            media_song_name.text = mld.songName
                            media_song_artist.text = mld.songArtist
                            media_song_duration.text = mld.songDuration
                            setAccount(mld.email)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference("Like").orderByChild("email_songUrl").equalTo(email + "_" + url).limitToFirst(1)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.children.iterator().hasNext()) {
                        //exist
                        for (ds in snapshot.children) {
                            like_key = ds.key.toString()
                            media_like_btn.setImageResource(R.drawable.baseline_favorite_24)
                            like_check = true
                        }
                    } else {
                        //not exist
                        media_like_btn.setImageResource(R.drawable.baseline_favorite_border_24)
                        like_check = false
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // 음악 재생시간 설정
        media_seekbar.progress = mediaController.currentPosition.toInt()
        media_seekbar.max = mediaController.duration.toInt()

        val millis = mediaController.currentPosition
        val totalSecs = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS)
        val minute = TimeUnit.MINUTES.convert(totalSecs, TimeUnit.SECONDS)
        val secs = totalSecs - (minute * 60)

        if(secs < 10){
            media_song_current.text = "$minute:0$secs"
        } else {
            media_song_current.text = "$minute:$secs"
        }
    }

    fun setAccount(email:String){
        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email").equalTo(email).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val data = ds.getValue<AccountData>()
                        if (data != null) {
                            media_email = data.email
                            if(data.imageUrl != "" && isAdded){
                                Glide.with(requireContext()).load(data.imageUrl).into(media_account)
                            } else {
                                media_account.setImageResource(R.drawable.baseline_account_circle_24)
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetBehavior = BottomSheetBehavior.from<View>(view.parent as View)
        bottomSheetBehavior.maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)

        view.findViewById<ImageButton>(R.id.back_btn).setOnClickListener{
            dismiss()
        }
    }
}