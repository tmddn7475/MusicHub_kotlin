package com.example.musichub.Fragment1.Library

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.musichub.Data.AlbumData
import com.example.musichub.Adapter.Base.MyAlbumAdapter
import com.example.musichub.Activity.AddAlbumActivity
import com.example.musichub.Fragment1.AlbumFragment
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class MyAlbumFragment : Fragment() {

    lateinit var add_my_list: TextView
    lateinit var my_list_view: ListView
    lateinit var my_list_text: TextView
    lateinit var myAlbumAdapter: MyAlbumAdapter

    private var list = mutableListOf<AlbumData>()
    private var keyList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_my_album, container, false)

        add_my_list = v.findViewById(R.id.add_my_list)
        my_list_view = v.findViewById(R.id.my_list_view)
        my_list_text = v.findViewById(R.id.my_list_text)
        myAlbumAdapter = MyAlbumAdapter(list = list, keyList = keyList)

        getList()

        // 앨범 추가
        add_my_list.setOnClickListener{
            val intent = Intent(requireContext(), AddAlbumActivity::class.java)
            startActivity(intent)
        }

        my_list_view.setOnItemClickListener{ _, _, position, _ ->
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val albumFragment = AlbumFragment()

            val bundle = Bundle()
            bundle.putString("key", keyList[position])
            albumFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, albumFragment).addToBackStack(null).commit()
        }

        return v
    }

    private fun getList(){
        val email = FirebaseAuth.getInstance().currentUser?.email
        FirebaseDatabase.getInstance().getReference("PlayLists").orderByChild("email").equalTo(email)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
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
                    my_list_view.adapter = myAlbumAdapter

                    if(list.isEmpty()){
                        my_list_text.visibility = View.VISIBLE
                    } else {
                        my_list_text.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "오류가 발생했습니다! 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                }
            })
    }
}