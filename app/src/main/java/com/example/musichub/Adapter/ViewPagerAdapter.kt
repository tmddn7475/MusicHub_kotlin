package com.example.musichub.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
    private val fragments: MutableList<Fragment> = mutableListOf()
    private val titles: MutableList<String> = mutableListOf()

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun addFragment(fragment: Fragment, title: String){
        fragments.add(fragment)
        titles.add(title)
    }

    fun getTitle(pos:Int): String{
        return titles[pos]
    }
}

