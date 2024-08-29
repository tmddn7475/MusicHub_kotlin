package com.example.musichub.Fragment1

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musichub.Fragment1.Account.AccountFragment
import com.example.musichub.Activity.LoginActivity
import com.example.musichub.Adapter.Recycler.CategoryAdapter
import com.example.musichub.Adapter.Base.MusicListAdapter
import com.example.musichub.Data.CategoryData
import com.example.musichub.Data.MusicData
import com.example.musichub.Interface.MusicListListener
import com.example.musichub.Interface.MusicListener
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.Activity.UploadActivity
import com.example.musichub.Object.Command
import com.example.musichub.Data.AccountData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import de.hdodenhof.circleimageview.CircleImageView

class HomeFragment(_musicListener:MusicListener) : Fragment(), MusicListListener {

    val musicListener:MusicListener = _musicListener

    lateinit var songsList: ListView
    lateinit var upload: ImageView
    lateinit var account: CircleImageView
    lateinit var logout: ImageView

    val email: String = FirebaseAuth.getInstance().currentUser?.email.toString()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: View = inflater.inflate(R.layout.fragment_home, container, false)

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)
        dialog.show()

        songsList = v.findViewById(R.id.songsList)
        upload = v.findViewById(R.id.home_upload)
        account = v.findViewById(R.id.home_account)
        logout = v.findViewById(R.id.home_logout)

        homeIntent()
        
        // 카테고리
        val categoryRecycler: RecyclerView = v.findViewById(R.id.category_recycler)
        val list = mutableListOf<CategoryData>()
        list.add(CategoryData(text = "Ambient", image = R.drawable.ambient))
        list.add(CategoryData(text = "Classical", image = R.drawable.classical))
        list.add(CategoryData(text = "Disco", image = R.drawable.disco))
        list.add(CategoryData(text = "Dance & EDM", image = R.drawable.edm))
        list.add(CategoryData(text = "Hip hop", image = R.drawable.hiphop))
        list.add(CategoryData(text = "Jazz", image = R.drawable.jazz))
        list.add(CategoryData(text = "R&B", image = R.drawable.rnb))
        list.add(CategoryData(text = "Reggae", image = R.drawable.reggae))
        list.add(CategoryData(text = "Rock", image = R.drawable.rock))

        val categoryAdapter = CategoryAdapter(list)
        categoryRecycler.adapter = categoryAdapter
        categoryRecycler.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)

        // 최신 곡
        val list_item = mutableListOf<MusicData>()
        val musicListAdapter = MusicListAdapter(list_item, this)
        FirebaseDatabase.getInstance().getReference("Songs").limitToLast(8)
            .addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(ds: DataSnapshot in snapshot.children) {
                    val mld = ds.getValue<MusicData>()
                    if (mld != null) {
                        list_item.add(0, mld)
                    }
                }
                dialog.dismiss()
                songsList.adapter = musicListAdapter

            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        })

        songsList.setOnItemClickListener { _, _, position, _ ->
            val data = list_item[position]
            val url:String = data.songUrl
            musicListener.playMusic(url)
        }

        return v
    }

    private fun homeIntent(){
        // 업로드
        upload.setOnClickListener{
            val intent = Intent(requireContext(), UploadActivity::class.java)
            startActivity(intent)
        }
        // 내 계정
        account.setOnClickListener{
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val accountFragment = AccountFragment()

            val bundle = Bundle()
            bundle.putString("email", FirebaseAuth.getInstance().currentUser?.email)
            accountFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit()
        }
        // 로그아웃
        logout.setOnClickListener{
            val alert_ex:AlertDialog.Builder = AlertDialog.Builder(requireContext())
            alert_ex.setMessage(getString(R.string.sign_out_alert))
            alert_ex.setNegativeButton(getString(R.string.yes)) { _, _ ->
                Command.deleteAll(requireContext())
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                ActivityCompat.finishAffinity(requireActivity())
            }
            alert_ex.setPositiveButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            val alert = alert_ex.create()
            alert.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
            alert.show()
        }
        // 내 계정 사진 가져오기
        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email").equalTo(email).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val data = ds.getValue<AccountData>()
                        if(data != null){
                            if(isAdded && data.imageUrl != ""){
                                Glide.with(requireContext()).load(data.imageUrl).into(account)
                            } else {
                                account.setImageResource(R.drawable.baseline_account_circle_24)
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show()
                }
            })
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