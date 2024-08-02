package com.example.musichub.Fragment1.Search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.example.musichub.Adapter.ViewPagerAdapter
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SearchResultFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_search_result, container, false)

        val str: String = arguments?.getString("search").toString()

        val search_result_back_btn: ImageView = v.findViewById(R.id.search_result_back_btn)
        val tabLayout: TabLayout = v.findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = v.findViewById(R.id.viewPager)

        val searchTrackFragment = SearchTrackFragment()
        val searchAccountFragment = SearchAccountFragment()
        val searchAlbumFragment = SearchAlbumFragment()

        val bundle = Bundle()
        bundle.putString("search", str)
        searchTrackFragment.arguments = bundle
        searchAccountFragment.arguments = bundle
        searchAlbumFragment.arguments = bundle

        val viewPagerAdapter = ViewPagerAdapter(requireActivity())
        viewPagerAdapter.addFragment(searchTrackFragment, getString(R.string.track))
        viewPagerAdapter.addFragment(searchAccountFragment, getString(R.string.account))
        viewPagerAdapter.addFragment(searchAlbumFragment,  getString(R.string.album))

        viewPager.adapter = viewPagerAdapter
        val tm = TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.setText(viewPagerAdapter.getTitle(position))
        }
        tm.attach()

        search_result_back_btn.setOnClickListener{
            back()
        }

        return v
    }

    private fun back(){
        val mainActivity = (activity as MainActivity)
        val fragmentManager = mainActivity.supportFragmentManager
        fragmentManager.beginTransaction().remove(this).commit()
        fragmentManager.popBackStack()
    }
}