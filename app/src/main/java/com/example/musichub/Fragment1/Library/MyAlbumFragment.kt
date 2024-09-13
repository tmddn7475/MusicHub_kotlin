package com.example.musichub.Fragment1.Library

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musichub.Data.AlbumData
import com.example.musichub.Adapter.Base.MyAlbumAdapter
import com.example.musichub.Activity.AddAlbumActivity
import com.example.musichub.Fragment1.AlbumFragment
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.databinding.FragmentMyAlbumBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class MyAlbumFragment : Fragment() {

    private var _binding: FragmentMyAlbumBinding? = null
    private val binding get() = _binding!!
    private var list = mutableListOf<AlbumData>()
    private var keyList = mutableListOf<String>()
    lateinit var myAlbumAdapter: MyAlbumAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAlbumBinding.inflate(inflater, container, false)

        myAlbumAdapter = MyAlbumAdapter(list = list, keyList = keyList)
        getList()

        // 앨범 추가
        binding.addMyList.setOnClickListener{
            val intent = Intent(requireContext(), AddAlbumActivity::class.java)
            startActivity(intent)
        }

        binding.myListView.setOnItemClickListener{ _, _, position, _ ->
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val albumFragment = AlbumFragment()

            val bundle = Bundle()
            bundle.putString("key", keyList[position])
            albumFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, albumFragment).addToBackStack(null).commit()
        }

        return binding.root
    }

    private fun getList(){
        val email = FirebaseAuth.getInstance().currentUser?.email
        FirebaseDatabase.getInstance().getReference("PlayLists").orderByChild("email").equalTo(email)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val binding = getBind() ?: return
                    list.clear()
                    keyList.clear()
                    for(ds: DataSnapshot in snapshot.children) {
                        val data = ds.getValue<AlbumData>()
                        val listKey = ds.key
                        if (data != null) {
                            list.add(data)
                            keyList.add(listKey!!)
                        }
                    }
                    binding.myListView.adapter = myAlbumAdapter

                    if(list.isEmpty()){
                        binding.myListText.visibility = View.VISIBLE
                    } else {
                        binding.myListText.visibility = View.GONE
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getBind(): FragmentMyAlbumBinding? {
        return _binding
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}