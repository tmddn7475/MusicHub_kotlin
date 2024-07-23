package com.example.musichub.Adapter.Base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.musichub.Data.AccountData
import com.example.musichub.R
import de.hdodenhof.circleimageview.CircleImageView

class AccountListAdapter(val list: MutableList<AccountData>): BaseAdapter() {
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return (convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.account_list, parent, false)).apply {
            val circleImageView: CircleImageView = findViewById(R.id.circleImageView)
            val accountText: TextView = findViewById(R.id.account_text)

            if(list[position].imageUrl != ""){
                Glide.with(context).load(list[position].imageUrl).into(circleImageView)
            } else {
                circleImageView.setImageResource(R.drawable.baseline_account_circle_24)
            }
            accountText.text = list[position].nickname
        }
    }
}