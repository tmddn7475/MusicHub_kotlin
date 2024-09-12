package com.example.musichub.Fragment2

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.media3.session.MediaController
import com.bumptech.glide.Glide
import com.example.musichub.Object.Command
import com.example.musichub.Data.AccountData
import com.example.musichub.Data.MusicData
import com.example.musichub.Fragment1.Account.AccountFragment
import com.example.musichub.Interface.MusicListener
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.databinding.FragmentMediaBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import java.util.concurrent.TimeUnit

class MediaFragment() : BottomSheetDialogFragment() {

    private var _binding: FragmentMediaBinding? = null
    val binding get() = _binding!!
    var mediaEmail: String = ""
    var likeCheck: Boolean = false
    var likeKey: String = ""

    lateinit var mediaController: MediaController
    lateinit var data: MusicData
    lateinit var musicListener: MusicListener

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
        _binding = FragmentMediaBinding.inflate(inflater, container, false)
        val mainActivity = (activity as MainActivity)

        binding.mediaSongName.isSingleLine = true // 한줄로 표시하기
        binding.mediaSongName.ellipsize = TextUtils.TruncateAt.MARQUEE // 흐르게 만들기
        binding.mediaSongName.isSelected = true

        setUpCurrent(mainActivity.currentUrl)

        binding.mediaSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaController.seekTo(seekBar?.progress!!.toLong())
            }
        })

        // Etc
        binding.mediaEtcBtn.setOnClickListener{
            val fragmentManager = mainActivity.supportFragmentManager
            val etcFragment = mainActivity.etcFragment

            if (!etcFragment.isAdded) {
                val bundle = Bundle()
                bundle.putString("url", mainActivity.currentUrl)
                etcFragment.setArguments(bundle)
                etcFragment.show(fragmentManager, etcFragment.getTag())
            }
        }

        // 댓글
        binding.mediaComment.setOnClickListener{
            val fragmentManager = mainActivity.supportFragmentManager
            val commentFragment = CommentFragment()

            if (!commentFragment.isAdded) {
                val bundle = Bundle()
                bundle.putString("url", mainActivity.currentUrl)
                commentFragment.setArguments(bundle)
                commentFragment.show(fragmentManager, commentFragment.getTag())
            }
        }

        // 계정
        binding.mediaAccount.setOnClickListener{
            val fragmentManager = mainActivity.supportFragmentManager
            val accountFragment = AccountFragment()

            val bundle = Bundle()
            bundle.putString("email", mediaEmail)
            accountFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit()
            dismiss()
        }

        // 좋아요
        binding.mediaLikeBtn.setOnClickListener{
            if(likeCheck){
                Command.uncheckLike(likeKey)
                binding.mediaLikeBtn.setImageResource(R.drawable.baseline_favorite_border_24)
                likeCheck = false
            } else {
                Command.checkLike(mainActivity.currentUrl)
                binding.mediaLikeBtn.setImageResource(R.drawable.baseline_favorite_24)
                likeCheck = true
            }
        }

        // play 버튼
        if(mediaController.isPlaying){
            binding.mediaPlayBtn.setImageResource(R.drawable.baseline_pause_24)
        } else {
            binding.mediaPlayBtn.setImageResource(R.drawable.baseline_play_arrow_24)
        }

        binding.mediaPlayBtn.setOnClickListener{
            if(mediaController.isPlaying){
                mediaController.pause()
                binding.mediaPlayBtn.setImageResource(R.drawable.baseline_play_arrow_24)
            } else {
                mediaController.play()
                binding.mediaPlayBtn.setImageResource(R.drawable.baseline_pause_24)
            }
        }

        // 다음 곡
        binding.mediaNextBtn.setOnClickListener{
            musicListener.nextMusic()
            binding.mediaSeekbar.progress = 0
            mediaController.play()
            if(isAdded){
                binding.mediaPlayBtn.setImageResource(R.drawable.baseline_pause_24)
            }
        }

        // 전 곡
        binding.mediaPreviousBtn.setOnClickListener{
            musicListener.prevMusic()
            binding.mediaSeekbar.progress = 0
            mediaController.play()
            if(isAdded){
                binding.mediaPlayBtn.setImageResource(R.drawable.baseline_pause_24)
            }
        }

        // playlist
        binding.mediaPlaylistBtn.setOnClickListener{
            val fragmentManager = mainActivity.supportFragmentManager
            val playlistFragment = mainActivity.playlistFragment

            playlistFragment.setController(mediaController)
            playlistFragment.show(fragmentManager, playlistFragment.tag)
            dismiss()
        }

        return binding.root
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
                            Glide.with(requireContext()).load(mld.imageUrl).into(binding.mediaSongThumnail)
                            binding.mediaSongName.text = mld.songName
                            binding.mediaSongArtist.text = mld.songArtist
                            binding.mediaSongDuration.text = mld.songDuration
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
                            likeKey = ds.key.toString()
                            binding.mediaLikeBtn.setImageResource(R.drawable.baseline_favorite_24)
                            likeCheck = true
                        }
                    } else {
                        //not exist
                        binding.mediaLikeBtn.setImageResource(R.drawable.baseline_favorite_border_24)
                        likeCheck = false
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // 음악 재생시간 설정
        binding.mediaSeekbar.progress = mediaController.currentPosition.toInt()
        binding.mediaSeekbar.max = mediaController.duration.toInt()

        val millis = mediaController.currentPosition
        val totalSecs = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS)
        val minute = TimeUnit.MINUTES.convert(totalSecs, TimeUnit.SECONDS)
        val secs = totalSecs - (minute * 60)

        if(secs < 10){
            binding.mediaSongCurrent.text = "$minute:0$secs"
        } else {
            binding.mediaSongCurrent.text = "$minute:$secs"
        }
    }

    fun setAccount(email:String){
        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email").equalTo(email).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val data = ds.getValue<AccountData>()
                        if (data != null) {
                            mediaEmail = data.email
                            if(data.imageUrl != "" && isAdded){
                                Glide.with(requireContext()).load(data.imageUrl).into(binding.mediaAccount)
                            } else {
                                binding.mediaAccount.setImageResource(R.drawable.baseline_account_circle_24)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}