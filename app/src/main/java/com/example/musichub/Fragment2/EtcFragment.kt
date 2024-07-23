package com.example.musichub.Fragment2

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.musichub.Activity.SongEditActivity
import com.example.musichub.Fragment1.Account.AccountFragment
import com.example.musichub.Command
import com.example.musichub.Data.MusicData
import com.example.musichub.Fragment1.SongInfoFragment
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.RoomDB.PlaylistDatabase
import com.example.musichub.RoomDB.PlaylistEntity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue


class EtcFragment : BottomSheetDialogFragment() {

    lateinit var etc_song_thumnail: ImageView
    lateinit var etc_like_img: ImageView
    lateinit var etc_song_info: Button
    lateinit var etc_artist_info: Button
    lateinit var etc_song_name: TextView
    lateinit var etc_song_artist: TextView
    lateinit var etc_add_my_list: TextView
    lateinit var etc_add_playlist: TextView
    lateinit var etc_like: TextView
    lateinit var etc_comment: TextView
    lateinit var etc_song_edit: TextView

    private var like_key = ""
    private var email: String = ""
    private var like_check: Boolean = false
    private var db: PlaylistDatabase? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_etc, container, false)

        val getUrl: String = arguments?.getString("url").toString()
        db = PlaylistDatabase.getInstance(requireContext())

        etc_song_thumnail = v.findViewById(R.id.etc_song_thumnail)
        etc_song_name = v.findViewById(R.id.etc_song_name)
        etc_song_artist = v.findViewById(R.id.etc_song_artist)

        etc_song_info = v.findViewById(R.id.etc_song_info)
        etc_artist_info = v.findViewById(R.id.etc_artist_info)
        etc_add_my_list = v.findViewById(R.id.etc_add_my_list)
        etc_add_playlist = v.findViewById(R.id.etc_add_playlist)
        etc_like_img = v.findViewById(R.id.etc_like_img)
        etc_like = v.findViewById(R.id.etc_like)
        etc_comment = v.findViewById(R.id.etc_comment)
        etc_song_edit = v.findViewById(R.id.etc_song_edit)

        etc_song_name.setSingleLine(true)
        etc_song_name.ellipsize = TextUtils.TruncateAt.MARQUEE // 흐르게 만들기
        etc_song_name.isSelected = true

        getData(getUrl)
        val mainActivity = activity as MainActivity
        val fragmentManager = mainActivity.supportFragmentManager

        // 곡 정보
        etc_song_info.setOnClickListener{
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
        etc_artist_info.setOnClickListener{
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
        etc_add_my_list.setOnClickListener{
            val songToAlbumFragment = SongToAlbumFragment()
            val bundle = Bundle()
            bundle.putString("url", getUrl)
            songToAlbumFragment.arguments = bundle
            songToAlbumFragment.show(fragmentManager, songToAlbumFragment.getTag())
        }

        // 재셍목록에 담기
        etc_add_playlist.setOnClickListener{
            val r = Runnable {
                if(db?.musicDAO()?.getCount(getUrl)!! < 1){
                    val data = PlaylistEntity(songUrl = getUrl, time = Command().getTime2())
                    db?.musicDAO()?.saveSong(data)!!
                }
            }
            val thread = Thread(r)
            thread.start()
            Toast.makeText(requireContext(), "재생목록에 추가되었습니다\n" + "중복이 있을 경우 추가되지 않습니다", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        // 좋아요
        etc_like.setOnClickListener{
            if(like_check){
                Command().uncheckLike(like_key)
                etc_like_img.setImageResource(R.drawable.baseline_favorite_border_24)
                like_check = false
                Toast.makeText(requireContext(), "해당 곡이 좋아요에 삭제되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                Command().checkLike(getUrl)
                etc_like_img.setImageResource(R.drawable.baseline_favorite_24)
                like_check = true
                Toast.makeText(requireContext(), "해당 곡이 좋아요에 추가되었습니다", Toast.LENGTH_SHORT).show()
            }
        }

        // 댓글 보기
        etc_comment.setOnClickListener{
            val commentFragment = CommentFragment()
            val bundle = Bundle()
            bundle.putString("url", getUrl)
            commentFragment.arguments = bundle
            commentFragment.show(fragmentManager, commentFragment.tag)
        }

        // 곡 정보 수정
        etc_song_edit.setOnClickListener {
            val intent = Intent(requireContext(), SongEditActivity::class.java)
            intent.putExtra("url", getUrl)
            startActivity(intent)
        }

        return v
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
                                etc_song_edit.visibility = View.GONE
                            } else {
                                etc_song_edit.visibility = View.VISIBLE
                            }

                            email = mld.email
                            etc_song_name.text = mld.songName
                            etc_song_artist.text = mld.songArtist
                            Glide.with(requireContext()).load(mld.imageUrl).into(etc_song_thumnail)
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
                            like_key = ds.key.toString()
                            like_check = true
                            etc_like_img.setImageResource(R.drawable.baseline_favorite_24)
                        }
                    } else {
                        like_check = false
                        etc_like_img.setImageResource(R.drawable.baseline_favorite_border_24)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "오류가 발생했습니다! 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                }
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
}