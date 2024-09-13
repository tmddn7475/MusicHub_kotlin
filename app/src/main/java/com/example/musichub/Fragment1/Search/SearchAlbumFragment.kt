package com.example.musichub.Fragment1.Search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musichub.Adapter.Base.GridViewAdapter
import com.example.musichub.Data.AlbumData
import com.example.musichub.Fragment1.AlbumFragment
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.databinding.FragmentSearchAlbumBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class SearchAlbumFragment : Fragment() {

    private var _binding: FragmentSearchAlbumBinding? = null
    private val binding get() = _binding!!
    lateinit var gridViewAdapter: GridViewAdapter
    var items = mutableListOf<AlbumData>()
    var keyList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchAlbumBinding.inflate(inflater, container, false)

        keyList.clear()
        items.clear()
        gridViewAdapter = GridViewAdapter(requireContext(), items)
        val query: String = arguments?.getString("search").toString().lowercase()

        binding.searchGridview.setOnItemClickListener{ _, _, position, _ ->
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
                val binding = getBind() ?: return
                for(ds: DataSnapshot in snapshot.children){
                    val data = ds.getValue<AlbumData>()
                    if(data != null){
                        if(data.list_mode == "public" && data.listName.lowercase().contains(query)){
                            keyList.add(ds.key.toString())
                            items.add(data)
                        }
                    }
                }
                binding.searchGridview.adapter = gridViewAdapter

                if(items.size == 0){
                    binding.none.visibility = View.VISIBLE
                } else {
                    binding.none.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        return binding.root
    }
    
    private fun getBind(): FragmentSearchAlbumBinding? {
        return _binding
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}