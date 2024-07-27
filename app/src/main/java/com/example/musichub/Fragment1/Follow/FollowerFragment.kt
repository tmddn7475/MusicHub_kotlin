package com.example.musichub.Fragment1.Follow

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.example.musichub.Adapter.Base.AccountListAdapter
import com.example.musichub.Data.AccountData
import com.example.musichub.Data.FollowData
import com.example.musichub.Fragment1.Account.AccountFragment
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class FollowerFragment : Fragment() {

    lateinit var follower_back_btn: ImageView
    lateinit var follower_list: ListView
    lateinit var follower_none: TextView
    lateinit var accountListAdapter: AccountListAdapter

    val list = mutableListOf<AccountData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_followers, container, false)

        val email = arguments?.getString("email").toString()

        follower_back_btn = v.findViewById(R.id.follower_back_btn)
        follower_list = v.findViewById(R.id.follower_list)
        follower_none = v.findViewById(R.id.textView1)
        accountListAdapter = AccountListAdapter(list)

        FirebaseDatabase.getInstance().getReference("Follow").orderByChild("follow").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.children.iterator().hasNext()){
                        list.clear()
                        for(ds: DataSnapshot in snapshot.children){
                            val data = ds.getValue<FollowData>()
                            if(data != null){
                                getAccount(data.email)
                            }
                        }
                        follower_none.visibility = View.GONE
                    } else {
                        follower_none.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        follower_list.setOnItemClickListener { parent, view, position, id ->
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val accountFragment = AccountFragment()

            val bundle = Bundle()
            bundle.putString("email", list[position].email)
            accountFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit()
        }

        follower_back_btn.setOnClickListener{
            back()
        }

        return v
    }

    private fun getAccount(email: String){
        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email").equalTo(email).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val data = ds.getValue<AccountData>()
                        if(data != null){
                            list.add(data)
                        }
                    }
                    follower_list.adapter = accountListAdapter
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun back(){
        val mainActivity = (activity as MainActivity)
        val fragmentManager = mainActivity.supportFragmentManager
        fragmentManager.beginTransaction().remove(this).commit()
        fragmentManager.popBackStack()
    }
}