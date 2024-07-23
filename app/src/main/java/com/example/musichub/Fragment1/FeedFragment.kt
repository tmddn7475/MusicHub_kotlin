package com.example.musichub.Fragment1

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musichub.Activity.LoginActivity
import com.example.musichub.Activity.UploadActivity
import com.example.musichub.Adapter.Recycler.FeedAccountAdapter
import com.example.musichub.Data.AccountData
import com.example.musichub.Data.FollowData
import com.example.musichub.Data.MusicData
import com.example.musichub.Adapter.Base.FeedListAdapter
import com.example.musichub.Command
import com.example.musichub.Fragment1.Account.AccountFragment
import com.example.musichub.Interface.MusicListener
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import de.hdodenhof.circleimageview.CircleImageView

class FeedFragment : Fragment() {

    lateinit var feed_account_recycler:RecyclerView
    lateinit var feed_list: ListView
    lateinit var feed_text: TextView
    lateinit var dialog: Dialog
    lateinit var feedListAdapter: FeedListAdapter
    lateinit var feedAccountAdapter: FeedAccountAdapter
    lateinit var accountList:MutableList<AccountData>
    lateinit var songList:MutableList<MusicData>
    lateinit var feed_logout: ImageView
    lateinit var feed_upload: ImageView
    lateinit var feed_account: CircleImageView

    private lateinit var musicListener: MusicListener
    val email:String = FirebaseAuth.getInstance().currentUser?.email.toString()

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
        val v = inflater.inflate(R.layout.fragment_feed, container, false)

        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)
        dialog.show()

        feed_text = v.findViewById(R.id.feed_text)
        feed_list = v.findViewById(R.id.feed_list)
        feed_logout = v.findViewById(R.id.feed_logout)
        feed_upload = v.findViewById(R.id.feed_upload)
        feed_account = v.findViewById(R.id.feed_account)

        accountList = mutableListOf()
        songList = mutableListOf()
        feed_account_recycler = v.findViewById(R.id.feed_account_recycler)
        feedAccountAdapter = FeedAccountAdapter(accountList)
        feed_account_recycler.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)

        feedListAdapter = FeedListAdapter(songList, musicListener)
        addItem()

        feed_list.setOnItemClickListener{ parent, view, position, id ->
            val data = songList[position]
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val songInfoFragment = SongInfoFragment()

            val bundle = Bundle()
            bundle.putString("url", data.songUrl)
            songInfoFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, songInfoFragment).addToBackStack(null).commit()
        }

        click()

        return v
    }

    private fun click(){
        // 업로드
        feed_upload.setOnClickListener{
            val intent = Intent(requireContext(), UploadActivity::class.java)
            startActivity(intent)
        }
        // 내 계정
        feed_account.setOnClickListener{
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val accountFragment = AccountFragment()

            val bundle = Bundle()
            bundle.putString("email", FirebaseAuth.getInstance().currentUser?.email)
            accountFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit()
        }
        // 로그아웃
        feed_logout.setOnClickListener{
            val alert_ex: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            alert_ex.setMessage("로그아웃하시겠습니까?")
            alert_ex.setNegativeButton("네") { _, _ ->
                Command().deleteAll(requireContext())
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                ActivityCompat.finishAffinity(requireActivity())
            }
            alert_ex.setPositiveButton("아니요") { dialog, _ ->
                dialog.dismiss()
            }
            val alert = alert_ex.create()
            alert.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
            alert.show()
        }

        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email").equalTo(email).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val data = ds.getValue<AccountData>()
                        if(data != null){
                            if(isAdded && data.imageUrl != ""){
                                Glide.with(requireContext()).load(data.imageUrl).into(feed_account)
                            } else {
                                feed_account.setImageResource(R.drawable.baseline_account_circle_24)
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "오류가 발생했습니다! 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            })
    }

    // 팔로우 계정 가져오기
    private fun addItem(){
        FirebaseDatabase.getInstance().getReference("Follow").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.children.iterator().hasNext()){
                        for(ds: DataSnapshot in snapshot.children) {
                            val data = ds.getValue<FollowData>()
                            if(data != null){
                                addItem2(data.follow)
                            }
                            feed_text.visibility = View.GONE
                        }
                        dialog.dismiss()
                    } else {
                        feed_text.visibility = View.VISIBLE
                        dialog.dismiss()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
    // 팔로우 곡 가져오기
    private fun addItem2(email: String){
        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email").equalTo(email).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val data = ds.getValue<AccountData>()
                        if(data != null){
                            accountList.add(data)
                        }
                    }
                    feed_account_recycler.adapter = feedAccountAdapter
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val data = ds.getValue<MusicData>()
                        if(data != null){
                            songList.add(0, data)
                        }
                    }
                    feedListAdapter.sort()
                    feed_list.adapter = feedListAdapter
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}