package com.example.musichub.Fragment1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.musichub.R
import com.example.musichub.RoomDB.PlaylistDatabase
import com.example.musichub.RoomDB.SearchEntity
import com.example.musichub.Adapter.Base.SearchListAdapter
import com.example.musichub.MainActivity
import com.example.musichub.Fragment1.Search.SearchResultFragment
import com.example.musichub.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var db: PlaylistDatabase? = null
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchListAdapter: SearchListAdapter
    private lateinit var list:MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        db = PlaylistDatabase.getInstance(requireContext())
        list = mutableListOf()
        searchListAdapter = SearchListAdapter(list, db)

        val run = Runnable {
            val playlistData = db?.musicDAO()?.getSearch()!!
            if(playlistData.isEmpty()){
                binding.none.visibility = View.VISIBLE
            } else {
                binding.none.visibility = View.GONE
                for(i:Int in playlistData.indices){
                    list.add(0, playlistData[i].searchText)
                }
                binding.searchRecent.adapter = searchListAdapter
            }
        }
        val thread = Thread(run)
        thread.start()

        binding.searchRecent.setOnItemClickListener{ parent, view, position, id ->
            val data:String = list[position]
            binding.searchView.setQuery("", false)
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val searchResultFragment = SearchResultFragment()

            val bundle = Bundle()
            bundle.putString("search", data)
            searchResultFragment.arguments = bundle
            fragmentManager.beginTransaction().replace(R.id.container, searchResultFragment).addToBackStack(null).commit()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    binding.searchView.setQuery("", false)
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

        return binding.root
    }

    fun save(str: String){
        val r = Runnable {
            val entity = SearchEntity(str)
            db?.musicDAO()?.saveSearch(entity)!!
        }
        val thread = Thread(r)
        thread.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}