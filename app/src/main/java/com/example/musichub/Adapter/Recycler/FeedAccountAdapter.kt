package com.example.musichub.Adapter.Recycler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musichub.Fragment1.Account.AccountFragment
import com.example.musichub.Data.AccountData
import com.example.musichub.MainActivity
import com.example.musichub.R
import de.hdodenhof.circleimageview.CircleImageView

class FeedAccountAdapter(val list: MutableList<AccountData>): RecyclerView.Adapter<FeedAccountAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_account_recycler, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        if(item.imageUrl == ""){
            holder.feed_account_image.setImageResource(R.drawable.baseline_account_circle_24)
        } else {
            Glide.with(holder.itemView).load(item.imageUrl).into(holder.feed_account_image)
        }
        holder.feed_account_name.text = item.nickname

        holder.itemView.setOnClickListener{
            val mainActivity = holder.itemView.context as MainActivity
            val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
            val accountFragment = AccountFragment()

            val bundle = Bundle()
            bundle.putString("email", item.email)
            accountFragment.setArguments(bundle)
            fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var feed_account_image: CircleImageView = itemView.findViewById(R.id.feed_account_image)
        var feed_account_name: TextView = itemView.findViewById(R.id.feed_account_name)
    }
}