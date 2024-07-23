package com.example.musichub.Fragment1.Library

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.example.musichub.Activity.Setting.SettingActivity
import com.example.musichub.Adapter.ViewPagerAdapter
import com.example.musichub.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LibraryFragment : Fragment() {
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

        viewPagerAdapter.addFragment(myAlbumFragment, "내 앨범")
        viewPagerAdapter.addFragment(likeFragment, "좋아요")
        viewPagerAdapter.addFragment(historyFragment, "음악 기록")

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

        return v
    }
}