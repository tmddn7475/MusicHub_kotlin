package com.example.musichub.Fragment1.Search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musichub.Adapter.ViewPagerAdapter
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.databinding.FragmentSearchResultBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SearchResultFragment : Fragment() {

    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)

        val searchTrackFragment = SearchTrackFragment()
        val searchAccountFragment = SearchAccountFragment()
        val searchAlbumFragment = SearchAlbumFragment()
        val str: String = arguments?.getString("search").toString()

        val bundle = Bundle()
        bundle.putString("search", str)
        searchTrackFragment.arguments = bundle
        searchAccountFragment.arguments = bundle
        searchAlbumFragment.arguments = bundle

        val viewPagerAdapter = ViewPagerAdapter(requireActivity())
        viewPagerAdapter.addFragment(searchTrackFragment, getString(R.string.track))
        viewPagerAdapter.addFragment(searchAccountFragment, getString(R.string.account))
        viewPagerAdapter.addFragment(searchAlbumFragment,  getString(R.string.album))

        binding.viewPager.adapter = viewPagerAdapter
        val tm = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.setText(viewPagerAdapter.getTitle(position))
        }
        tm.attach()

        binding.searchResultBackBtn.setOnClickListener{
            back()
        }

        return binding.root
    }

    private fun back(){
        val mainActivity = (activity as MainActivity)
        val fragmentManager = mainActivity.supportFragmentManager
        fragmentManager.beginTransaction().remove(this).commit()
        fragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}