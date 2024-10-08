package com.example.musichub.Fragment1.Search

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musichub.Adapter.Base.MusicListAdapter
import com.example.musichub.Data.MusicData
import com.example.musichub.Interface.MusicListListener
import com.example.musichub.Interface.MusicListener
import com.example.musichub.MainActivity
import com.example.musichub.databinding.FragmentSearchTrackBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class SearchTrackFragment : Fragment(), MusicListListener {

    private var _binding: FragmentSearchTrackBinding? = null
    private val binding get() = _binding!!
    lateinit var musicListAdapter: MusicListAdapter
    var list = mutableListOf<MusicData>()

    private lateinit var musicListener: MusicListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            musicListener = context as MusicListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchTrackBinding.inflate(inflater, container, false)

        list.clear()
        musicListAdapter = MusicListAdapter(list, this)
        val query: String = arguments?.getString("search").toString().lowercase()

        FirebaseDatabase.getInstance().getReference("Songs").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val binding = getBind() ?: return
                for(ds: DataSnapshot in snapshot.children){
                    val data = ds.getValue<MusicData>()
                    if(data != null){
                        if(data.songName.lowercase().contains(query) || data.songArtist.lowercase().contains(query)){
                            list.add(data)
                            Log.v("test", data.songName)
                        }
                    }
                }
                binding.searchTrackList.adapter = musicListAdapter

                if(list.size == 0){
                    binding.none.visibility = View.VISIBLE
                } else {
                    binding.none.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        binding.searchTrackList.setOnItemClickListener{ _, _, position, _ ->
            val data = list[position]
            musicListener.playMusic(data.songUrl)
        }

        return binding.root
    }

    override fun sendEtc(message: String) {
        val mainActivity = (activity as MainActivity)
        val fragmentManager = mainActivity.supportFragmentManager
        val etcFragment = mainActivity.etcFragment

        if (!etcFragment.isAdded) {
            val bundle = Bundle()
            bundle.putString("url", message)
            etcFragment.setArguments(bundle)

            etcFragment.show(fragmentManager, etcFragment.getTag())
        }
    }

    private fun getBind(): FragmentSearchTrackBinding? {
        return _binding
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}