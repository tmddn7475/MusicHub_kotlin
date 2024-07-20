package com.example.musichub.Fragment1.Library

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import com.example.musichub.Adapter.Base.MusicListAdapter
import com.example.musichub.Data.MusicData
import com.example.musichub.Interface.MusicListListener
import com.example.musichub.Interface.MusicListener
import com.example.musichub.Data.LikeData
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class LikeFragment : Fragment(), MusicListListener {

    lateinit var like_list: ListView
    lateinit var like_text: TextView
    lateinit var musicListAdapter: MusicListAdapter
    lateinit var musicListener: MusicListener

    var list = mutableListOf<MusicData>()

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
        val v = inflater.inflate(R.layout.fragment_like, container, false)

        like_list = v.findViewById(R.id.like_list)
        like_text = v.findViewById(R.id.like_text)
        musicListAdapter = MusicListAdapter(list, this)

        val email:String = FirebaseAuth.getInstance().currentUser?.email.toString()
        FirebaseDatabase.getInstance().getReference("Like").orderByChild("email").equalTo(email)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.children.iterator().hasNext()){
                        for(ds: DataSnapshot in snapshot.children) {
                            val data = ds.getValue<LikeData>()
                            if(data != null){
                                getMusic(data.songUrl)
                            }
                        }
                        like_text.visibility = View.GONE
                    } else {
                        like_text.visibility = View.VISIBLE
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        return v
    }

    private fun getMusic(url:String){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val data = ds.getValue<MusicData>()
                        if(data != null){
                            list.add(data)
                        }
                    }
                    like_list.adapter = musicListAdapter
                }
                override fun onCancelled(error: DatabaseError) {}
            })
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