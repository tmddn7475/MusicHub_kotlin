package com.example.musichub.Fragment1.Search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import com.example.musichub.Adapter.Base.GridViewAdapter
import com.example.musichub.Data.AlbumData
import com.example.musichub.Fragment1.AlbumFragment
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class SearchAlbumFragment : Fragment() {

    lateinit var gridViewAdapter: GridViewAdapter
    lateinit var search_gridview: GridView
    lateinit var none: TextView

    var items = mutableListOf<AlbumData>()
    var keyList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_search_album, container, false)

        val query: String = arguments?.getString("search").toString().lowercase()

        keyList.clear()
        items.clear()

        search_gridview = v.findViewById(R.id.search_gridview)
        none = v.findViewById(R.id.none)
        gridViewAdapter = GridViewAdapter(requireContext(), items)

        search_gridview.setOnItemClickListener{ parent, view, position, id ->
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val albumFragment = AlbumFragment()

            val bundle = Bundle()
            bundle.putString("key", keyList[position])
            albumFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, albumFragment).addToBackStack(null).commit()
        }

        FirebaseDatabase.getInstance().getReference("PlayLists").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(ds: DataSnapshot in snapshot.children){
                    val data = ds.getValue<AlbumData>()
                    if(data != null){
                        if(data.list_mode == "public" && data.listName.lowercase().contains(query)){
                            keyList.add(ds.key.toString())
                            items.add(data)
                        }
                    }
                }
                search_gridview.adapter = gridViewAdapter

                if(items.size == 0){
                    none.visibility = View.VISIBLE
                } else {
                    none.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        //search_gridview.setOnItemClickListener()

        return v
    }

}