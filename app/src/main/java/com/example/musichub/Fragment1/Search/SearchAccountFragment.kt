package com.example.musichub.Fragment1.Search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import com.example.musichub.Adapter.Base.AccountListAdapter
import com.example.musichub.Data.AccountData
import com.example.musichub.Fragment1.Account.AccountFragment
import com.example.musichub.Fragment1.SongInfoFragment
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class SearchAccountFragment : Fragment() {

    lateinit var account_list: ListView
    lateinit var accountListAdapter: AccountListAdapter
    lateinit var none: TextView
    var list = mutableListOf<AccountData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_search_account, container, false)

        val query: String = arguments?.getString("search").toString().lowercase()

        list.clear()
        account_list = v.findViewById(R.id.search_account_list)
        none = v.findViewById(R.id.none)
        accountListAdapter = AccountListAdapter(list)

        FirebaseDatabase.getInstance().getReference("accounts").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(ds: DataSnapshot in snapshot.children) {
                    val data = ds.getValue<AccountData>()
                    if(data != null){
                        if(data.nickname.lowercase().contains(query)){
                            list.add(data)
                        }
                    }
                }
                account_list.adapter = accountListAdapter

                if(list.size == 0){
                    none.visibility = View.VISIBLE
                } else {
                    none.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        account_list.setOnItemClickListener{parent, view, position, id ->
            val mainActivity: MainActivity = context as MainActivity
            val fragmentManager = mainActivity.supportFragmentManager
            val accountFragment = AccountFragment()

            val bundle = Bundle()
            bundle.putString("email", list[position].email)
            accountFragment.setArguments(bundle)
            fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit()
        }

        return v
    }

}