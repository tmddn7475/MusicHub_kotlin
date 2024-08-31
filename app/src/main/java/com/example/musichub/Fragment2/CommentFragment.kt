package com.example.musichub.Fragment2

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.musichub.Adapter.Recycler.CommentAdapter
import com.example.musichub.Object.Command
import com.example.musichub.Data.AccountData
import com.example.musichub.Data.CommentData
import com.example.musichub.Data.MusicData
import com.example.musichub.Fragment1.Account.AccountFragment
import com.example.musichub.Interface.CommentListener
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.databinding.FragmentCommentBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class CommentFragment : BottomSheetDialogFragment(), CommentListener {

    private var _binding: FragmentCommentBinding? = null
    private val binding get() = _binding!!
    lateinit var commentAdapter: CommentAdapter
    lateinit var nickname: String
    lateinit var imageUrl: String
    var list = mutableListOf<CommentData>()
    var keyList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentBinding.inflate(inflater, container, false)

        binding.commentSongName.setSingleLine(true)
        binding.commentSongName.ellipsize = TextUtils.TruncateAt.MARQUEE
        binding.commentSongName.isSelected = true

        val email: String = FirebaseAuth.getInstance().currentUser?.email.toString()
        val songUrl: String = arguments?.getString("url")!!

        commentAdapter = CommentAdapter(list, keyList, this)

        getAccounts(email)
        setComments(songUrl)

        // 댓글 쓰기
        binding.commentSend.setOnClickListener{
            val comment = binding.commentEdit.text.toString()
            if(comment.isEmpty()){
                Toast.makeText(context, getString(R.string.enter_all), Toast.LENGTH_SHORT).show()
            } else {
                uploadComment(email, nickname, imageUrl, comment, songUrl)
                binding.commentEdit.text.clear()
            }
        }

        return binding.root
    }

    // 댓글 등록
    @SuppressLint("NotifyDataSetChanged")
    private fun uploadComment(email:String, name:String, imageUrl:String, comment:String, songUrl: String){
        val data = CommentData(email = email, nickname = name, imageUrl = imageUrl, comment = comment, time = Command.getTime2(), songUrl = songUrl)

        val reference = FirebaseDatabase.getInstance().getReference("Comments").push()
        reference.setValue(data).addOnCompleteListener { p0 ->
            if (p0.isSuccessful) {
                list.add(data)
                keyList.add(reference.key.toString())
                Log.i("key", reference.key.toString())
                commentAdapter.notifyDataSetChanged()
            }
        }
    }

    // 내 계정 정보
    private fun getAccounts(str: String){
        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email").equalTo(str).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val data = ds.getValue<AccountData>()
                        if(data != null){
                            nickname = data.nickname
                            imageUrl = data.imageUrl
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setComments(url: String){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val data = ds.getValue<MusicData>()
                        if(data != null){
                            binding.commentSongName.text = data.songName
                            binding.commentSongArtist.text = data.songArtist
                            Glide.with(this@CommentFragment).load(data.imageUrl).into(binding.commentSongThumnail)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // 댓글들 가져오기
        FirebaseDatabase.getInstance().getReference("Comments").orderByChild("songUrl").equalTo(url)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()
                    keyList.clear()
                    for(ds: DataSnapshot in snapshot.children){
                        val data = ds.getValue<CommentData>()
                        if(data != null){
                            keyList.add(ds.key.toString())
                            list.add(data)
                        }
                    }
                    commentAdapter.notifyDataSetChanged()
                    binding.commentRecycler.adapter = commentAdapter
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetBehavior = BottomSheetBehavior.from<View>(view.parent as View)
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
        bottomSheetBehavior.peekHeight = view.measuredHeight
        bottomSheetBehavior.isDraggable = false

        view.findViewById<ImageView>(R.id.comment_down_btn).setOnClickListener{
            dismiss()
        }
    }

    override fun goProfile(email: String) {
        val mainActivity: MainActivity = context as MainActivity
        val fragmentManager = mainActivity.supportFragmentManager
        val accountFragment = AccountFragment()

        val bundle = Bundle()
        bundle.putString("email", email)
        accountFragment.setArguments(bundle)
        fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit()

        if(mainActivity.mediaFragment.isAdded){
            mainActivity.mediaFragment.dismiss()
        }
        if(mainActivity.playlistFragment.isAdded){
            mainActivity.playlistFragment.dismiss()
        }
        if(mainActivity.etcFragment.isAdded){
            mainActivity.etcFragment.dismiss()
        }
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}