package com.example.musichub.Fragment2

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.musichub.Activity.SongEditActivity
import com.example.musichub.Fragment1.Account.AccountFragment
import com.example.musichub.Object.Command
import com.example.musichub.Data.MusicData
import com.example.musichub.Fragment1.SongInfoFragment
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.RoomDB.PlaylistDatabase
import com.example.musichub.RoomDB.PlaylistEntity
import com.example.musichub.databinding.FragmentEtcBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue


class EtcFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentEtcBinding? = null
    private val binding get() = _binding!!

    private var likeKey = ""
    private var email: String = ""
    private var likeCheck: Boolean = false
    private var db: PlaylistDatabase? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEtcBinding.inflate(inflater, container, false)

        val getUrl: String = arguments?.getString("url").toString()
        db = PlaylistDatabase.getInstance(requireContext())

        binding.etcSongName.setSingleLine(true)
        binding.etcSongName.ellipsize = TextUtils.TruncateAt.MARQUEE // 흐르게 만들기
        binding.etcSongName.isSelected = true

        getData(getUrl)
        val mainActivity = activity as MainActivity
        val fragmentManager = mainActivity.supportFragmentManager

        // 곡 정보
        binding.etcSongInfo.setOnClickListener{
            val songInfoFragment = SongInfoFragment()
            val bundle = Bundle()
            bundle.putString("url", getUrl)
            songInfoFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, songInfoFragment).addToBackStack(null).commit()

            if(mainActivity.mediaFragment.isAdded){
                mainActivity.mediaFragment.dismiss()
            }
            if(mainActivity.playlistFragment.isAdded){
                mainActivity.playlistFragment.dismiss()
            }
            dismiss()
        }
        // 아티스트 정보
        binding.etcArtistInfo.setOnClickListener{
            val accountFragment = AccountFragment()
            val bundle = Bundle()
            bundle.putString("email", email)
            accountFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit()

            if(mainActivity.mediaFragment.isAdded){
                mainActivity.mediaFragment.dismiss()
            }
            if(mainActivity.playlistFragment.isAdded){
                mainActivity.playlistFragment.dismiss()
            }
            dismiss()
        }

        // 내 앨범에 담기
        binding.etcAddMyList.setOnClickListener{
            val songToAlbumFragment = SongToAlbumFragment()
            val bundle = Bundle()
            bundle.putString("url", getUrl)
            songToAlbumFragment.arguments = bundle
            songToAlbumFragment.show(fragmentManager, songToAlbumFragment.getTag())
        }

        // 재셍목록에 담기
        binding.etcAddPlaylist.setOnClickListener{
            val r = Runnable {
                if(db?.musicDAO()?.getCount(getUrl)!! < 1){
                    val data = PlaylistEntity(songUrl = getUrl, time = Command.getTime2())
                    db?.musicDAO()?.saveSong(data)!!
                }
            }
            val thread = Thread(r)
            thread.start()
            Toast.makeText(requireContext(), getString(R.string.song_to_playlist), Toast.LENGTH_SHORT).show()
            dismiss()
        }

        // 좋아요
        binding.etcLike.setOnClickListener{
            if(likeCheck){
                Command.uncheckLike(likeKey)
                binding.etcLikeImg.setImageResource(R.drawable.baseline_favorite_border_24)
                likeCheck = false
            } else {
                Command.checkLike(getUrl)
                binding.etcLikeImg.setImageResource(R.drawable.baseline_favorite_24)
                likeCheck = true
            }
        }

        // 댓글 보기
        binding.etcComment.setOnClickListener{
            val commentFragment = CommentFragment()
            val bundle = Bundle()
            bundle.putString("url", getUrl)
            commentFragment.arguments = bundle
            commentFragment.show(fragmentManager, commentFragment.tag)
        }

        // 곡 정보 수정
        binding.etcSongEdit.setOnClickListener {
            val intent = Intent(requireContext(), SongEditActivity::class.java)
            intent.putExtra("url", getUrl)
            startActivity(intent)
        }

        return binding.root
    }

    private fun getData(url: String){
        val myEmail: String = FirebaseAuth.getInstance().currentUser?.email.toString()

        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url)
            .limitToFirst(1).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val mld = ds.getValue<MusicData>()
                        if (mld != null) {
                            if(mld.email != myEmail){
                                binding.etcSongEdit.visibility = View.GONE
                            } else {
                                binding.etcSongEdit.visibility = View.VISIBLE
                            }

                            email = mld.email
                            binding.etcSongName.text = mld.songName
                            binding.etcSongArtist.text = mld.songArtist
                            Glide.with(requireContext()).load(mld.imageUrl).into(binding.etcSongThumnail)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference("Like").orderByChild("email_songUrl").equalTo(myEmail+"_"+url)
            .limitToFirst(1).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.children.iterator().hasNext()){
                        for(ds: DataSnapshot in snapshot.children) {
                            likeKey = ds.key.toString()
                            likeCheck = true
                            binding.etcLikeImg.setImageResource(R.drawable.baseline_favorite_24)
                        }
                    } else {
                        likeCheck = false
                        binding.etcLikeImg.setImageResource(R.drawable.baseline_favorite_border_24)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        view.findViewById<ImageButton>(R.id.etc_dismiss_btn).setOnClickListener{
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}