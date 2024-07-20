package com.example.musichub.Fragment1.Account

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
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

class AccountTrackFragment : Fragment(), MusicListListener {

    private lateinit var musicListener:MusicListener

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
        val v = inflater.inflate(R.layout.fragment_account_track, container, false)

        val email = arguments?.getString("email")
        val track_list: ListView = v.findViewById(R.id.track_list)
        val track_none: TextView = v.findViewById(R.id.track_none)
        val list = mutableListOf<MusicData>()
        val musicListAdapter = MusicListAdapter(list, this)

        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("email")
            .equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.children.iterator().hasNext()){
                    for(ds: DataSnapshot in snapshot.children) {
                        val mld = ds.getValue<MusicData>()
                        if (mld != null) {
                            list.add(mld)
                        }
                    }
                    track_none.visibility = View.GONE
                    track_list.adapter = musicListAdapter
                } else {
                    track_none.visibility = View.VISIBLE
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        track_list.setOnItemClickListener { parent, view, position, id ->
            val data = list[position]
            val url:String = data.songUrl
            musicListener.playMusic(url)
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

            etcFragment.show(fragmentManager, etcFragment.tag)
        }
    }
}