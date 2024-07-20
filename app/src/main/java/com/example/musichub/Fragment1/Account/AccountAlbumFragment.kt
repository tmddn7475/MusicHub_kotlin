package com.example.musichub.Fragment1.Account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.musichub.Data.AlbumData
import com.example.musichub.Adapter.Base.GridViewAdapter
import com.example.musichub.Fragment1.AlbumFragment
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class AccountAlbumFragment : Fragment() {

    lateinit var gridViewAdapter: GridViewAdapter
    lateinit var keyList: MutableList<String>
    lateinit var items: MutableList<AlbumData>
    lateinit var albumGridView: GridView
    lateinit var album_none: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_account_album, container, false)
        val email: String = arguments?.getString("email").toString()
        albumGridView = v.findViewById(R.id.list_gridview)
        album_none = v.findViewById(R.id.list_none)

        keyList = mutableListOf()
        items = mutableListOf()
        gridViewAdapter = GridViewAdapter(requireContext(), items)

        getList(email)
        albumGridView.setOnItemClickListener{ parent, view, position, id ->
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

    private fun getList(str: String){
        FirebaseDatabase.getInstance().getReference("PlayLists").orderByChild("email").equalTo(str)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.children.iterator().hasNext()){
                        for(ds: DataSnapshot in snapshot.children) {
                            val data = ds.getValue<AlbumData>()
                            if (data != null) {
                                if(data.list_mode == "public" || FirebaseAuth.getInstance().currentUser?.email.toString() == data.email){
                                    keyList.add(ds.key.toString())
                                    items.add(data)
                                }
                            }
                        }
                        album_none.visibility = View.GONE
                        albumGridView.adapter = gridViewAdapter
                    } else {
                        album_none.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}