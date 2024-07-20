package com.example.musichub.Fragment1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.musichub.R
import com.example.musichub.RoomDB.PlaylistDatabase
import com.example.musichub.RoomDB.SearchEntity
import com.example.musichub.Adapter.Base.SearchListAdapter
import com.example.musichub.MainActivity
import com.example.musichub.Fragment1.Search.SearchResultFragment

class SearchFragment : Fragment() {

    private var db: PlaylistDatabase? = null

    private lateinit var searchView: SearchView
    private lateinit var searchList: ListView
    private lateinit var none: TextView
    private lateinit var searchListAdapter: SearchListAdapter

    lateinit var list:MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_search, container, false)

        db = PlaylistDatabase.getInstance(requireContext())
        list = mutableListOf()
        searchView = v.findViewById(R.id.search_view)
        searchList = v.findViewById(R.id.search_recent)
        none = v.findViewById(R.id.none)
        searchListAdapter = SearchListAdapter(list, db)

        val run = Runnable {
            val playlistData = db?.musicDAO()?.getSearch()!!
            if(playlistData.isEmpty()){
                none.visibility = View.VISIBLE
            } else {
                none.visibility = View.GONE
                for(i:Int in playlistData.indices){
                    list.add(0, playlistData[i].searchText)
                }
                searchList.adapter = searchListAdapter
            }
        }
        val thread = Thread(run)
        thread.start()

        searchList.setOnItemClickListener{ parent, view, position, id ->
            val data:String = list[position]
            searchView.setQuery("", false)
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val searchResultFragment = SearchResultFragment()

            val bundle = Bundle()
            bundle.putString("search", data)
            searchResultFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, searchResultFragment).addToBackStack(null).commit()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchView.setQuery("", false)
                    save(query)

                    val mainActivity = (activity as MainActivity)
                    val fragmentManager = mainActivity.supportFragmentManager
                    val searchResultFragment = SearchResultFragment()

                    val bundle = Bundle()
                    bundle.putString("search", query)
                    searchResultFragment.arguments = bundle
                    fragmentManager.beginTransaction().replace(R.id.container, searchResultFragment).addToBackStack(null).commit()
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {return false}
        })

        return v
    }

    fun save(str: String){
        val r = Runnable {
            val entity = SearchEntity(str)
            db?.musicDAO()?.saveSearch(entity)!!
        }
        val thread = Thread(r)
        thread.start()
    }
}