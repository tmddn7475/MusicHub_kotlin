package com.example.musichub.Fragment1.Library

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.musichub.Activity.Setting.SettingActivity
import com.example.musichub.Adapter.ViewPagerAdapter
import com.example.musichub.Data.AccountData
import com.example.musichub.Fragment1.Account.AccountFragment
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import de.hdodenhof.circleimageview.CircleImageView

class LibraryFragment : Fragment() {

    val email: String = FirebaseAuth.getInstance().currentUser?.email.toString()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_library, container, false)

        val tabLayout: TabLayout = v.findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = v.findViewById(R.id.viewPager)
        val viewPagerAdapter = ViewPagerAdapter(requireActivity())

        val myAlbumFragment = MyAlbumFragment()
        val likeFragment = LikeFragment()
        val historyFragment = HistoryFragment()

        viewPagerAdapter.addFragment(myAlbumFragment, getString(R.string.album))
        viewPagerAdapter.addFragment(likeFragment, getString(R.string.like))
        viewPagerAdapter.addFragment(historyFragment,  getString(R.string.history))

        viewPager.adapter = viewPagerAdapter
        val tm = TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.setText(viewPagerAdapter.getTitle(position))
        }
        tm.attach()

        val setting: ImageView = v.findViewById(R.id.setting)
        setting.setOnClickListener{
            val intent = Intent(requireContext(), SettingActivity::class.java)
            startActivity(intent)
        }

        val libraryAccount: CircleImageView = v.findViewById(R.id.libraryAccount)
        libraryAccount.setOnClickListener{
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val accountFragment = AccountFragment()

            val bundle = Bundle()
            bundle.putString("email", email)
            accountFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit()
        }

        // 내 계정 사진 가져오기
        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email").equalTo(email).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val data = ds.getValue<AccountData>()
                        if(data != null){
                            if(isAdded && data.imageUrl != ""){
                                Glide.with(requireContext()).load(data.imageUrl).into(libraryAccount)
                            } else {
                                libraryAccount.setImageResource(R.drawable.baseline_account_circle_24)
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show()
                }
            })

        return v
    }
}