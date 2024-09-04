package com.example.musichub.Fragment1.Follow

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musichub.Adapter.Base.AccountListAdapter
import com.example.musichub.Data.AccountData
import com.example.musichub.Data.FollowData
import com.example.musichub.Fragment1.Account.AccountFragment
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.databinding.FragmentFollowersBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class FollowerFragment : Fragment() {

    private var _binding: FragmentFollowersBinding? = null
    private val binding get() = _binding!!
    lateinit var accountListAdapter: AccountListAdapter
    val list = mutableListOf<AccountData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFollowersBinding.inflate(inflater, container, false)

        val email = arguments?.getString("email").toString()
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
                        binding.textView1.visibility = View.GONE
                    } else {
                        binding.textView1.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        binding.followerList.setOnItemClickListener { _, _, position, _ ->
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val accountFragment = AccountFragment()

            val bundle = Bundle()
            bundle.putString("email", list[position].email)
            accountFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, accountFragment).addToBackStack(null).commit()
        }

        binding.followerBackBtn.setOnClickListener{
            back()
        }

        return binding.root
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
                    binding.followerList.adapter = accountListAdapter
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}