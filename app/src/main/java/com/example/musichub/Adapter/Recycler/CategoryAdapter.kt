package com.example.musichub.Adapter.Recycler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musichub.Fragment1.CategoryFragment
import com.example.musichub.Data.CategoryData
import com.example.musichub.MainActivity
import com.example.musichub.R

class CategoryAdapter(val list: MutableList<CategoryData>) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.category_text.text = list[position].text
        holder.category_image.setImageResource(list[position].image)

        holder.itemView.setOnClickListener{
            val mainActivity: MainActivity = holder.itemView.context as MainActivity
            val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
            val categoryFragment = CategoryFragment()

            val bundle = Bundle()
            bundle.putString("category", list[position].text)
            categoryFragment.setArguments(bundle)
            fragmentManager.beginTransaction().replace(R.id.container, categoryFragment).addToBackStack(null).commit()
        }
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category_image: ImageView = itemView.findViewById(R.id.category_image)
        val category_text: TextView = itemView.findViewById(R.id.category_text)
    }
}