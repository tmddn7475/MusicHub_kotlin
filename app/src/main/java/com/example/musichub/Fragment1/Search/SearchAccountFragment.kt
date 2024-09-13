package com.example.musichub.Fragment1.Search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musichub.Adapter.Base.AccountListAdapter
import com.example.musichub.Data.AccountData
import com.example.musichub.Fragment1.Account.AccountFragment
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.databinding.FragmentSearchAccountBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class SearchAccountFragment : Fragment() {

    private var _binding: FragmentSearchAccountBinding? = null
    private val binding get() = _binding!!
    lateinit var accountListAdapter: AccountListAdapter
    var list = mutableListOf<AccountData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchAccountBinding.inflate(inflater, container, false)

        list.clear()
        accountListAdapter = AccountListAdapter(list)
        val query: String = arguments?.getString("search").toString().lowercase()

        FirebaseDatabase.getInstance().getReference("accounts").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val binding = getBind() ?: return
                for(ds: DataSnapshot in snapshot.children) {
                    val data = ds.getValue<AccountData>()
                    if(data != null){
                        if(data.nickname.lowercase().contains(query)){
                            list.add(data)
                        }
                    }
                }
                binding.searchAccountList.adapter = accountListAdapter

                if(list.size == 0){
                    binding.none.visibility = View.VISIBLE
                } else {
                    binding.none.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        binding.searchAccountList.setOnItemClickListener{ _, _, position, _ ->
            val mainActivity: MainActivity = context as MainActivity
            val fragmentManager = mainActivity.supportFragmentManager
            val accountFragment = AccountFragment()

            val bundle = Bundle()
            bundle.putString("email", list[position].email)
            accountFragment.setArguments(bundle)
            fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit()
        }

        return binding.root
    }

    private fun getBind(): FragmentSearchAccountBinding? {
        return _binding
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}