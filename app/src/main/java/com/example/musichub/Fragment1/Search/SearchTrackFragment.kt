package com.example.musichub.Fragment1.Search

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.musichub.Adapter.Base.MusicListAdapter
import com.example.musichub.Data.MusicData
import com.example.musichub.Interface.MusicListListener
import com.example.musichub.Interface.MusicListener
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class SearchTrackFragment : Fragment(), MusicListListener {

    lateinit var search_track_list: ListView
    lateinit var none: TextView
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
    ): View? {
        val v = inflater.inflate(R.layout.fragment_search_track, container, false)

        val query: String = arguments?.getString("search").toString().lowercase()
        list.clear()

        search_track_list = v.findViewById(R.id.search_track_list)
        none = v.findViewById(R.id.none)
        musicListAdapter = MusicListAdapter(list, this)

        FirebaseDatabase.getInstance().getReference("Songs").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(ds: DataSnapshot in snapshot.children){
                    val data = ds.getValue<MusicData>()
                    if(data != null){
                        if(data.songName.lowercase().contains(query) || data.songArtist.lowercase().contains(query)){
                            list.add(data)
                            Log.v("test", data.songName)
                        }
                    }
                }
                search_track_list.adapter = musicListAdapter

                if(list.size == 0){
                    none.visibility = View.VISIBLE
                } else {
                    none.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "오류가 발생했습니다! 다시 시도해주세요", Toast.LENGTH_SHORT).show()
            }
        })

        search_track_list.setOnItemClickListener{ _, _, position, _ ->
            val data = list[position]
            musicListener.playMusic(data.songUrl)
        }

        return v
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
}