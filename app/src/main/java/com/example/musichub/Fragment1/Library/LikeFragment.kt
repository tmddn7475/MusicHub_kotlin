package com.example.musichub.Fragment1.Library

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musichub.Adapter.Base.MusicListAdapter
import com.example.musichub.Data.MusicData
import com.example.musichub.Interface.MusicListListener
import com.example.musichub.Interface.MusicListener
import com.example.musichub.Data.LikeData
import com.example.musichub.MainActivity
import com.example.musichub.databinding.FragmentLikeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class LikeFragment : Fragment(), MusicListListener {

    private var _binding: FragmentLikeBinding? = null
    private val binding get() = _binding!!
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
    ): View {
        _binding = FragmentLikeBinding.inflate(inflater, container, false)

        musicListAdapter = MusicListAdapter(list, this)

        val email:String = FirebaseAuth.getInstance().currentUser?.email.toString()
        FirebaseDatabase.getInstance().getReference("Like").orderByChild("email").equalTo(email)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val binding = getBind() ?: return
                    if(snapshot.children.iterator().hasNext()){
                        for(ds: DataSnapshot in snapshot.children) {
                            val data = ds.getValue<LikeData>()
                            if(data != null){
                                getMusic(data.songUrl)
                            }
                        }
                        binding.likeText.visibility = View.GONE
                    } else {
                        binding.likeText.visibility = View.VISIBLE
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        return binding.root
    }

    private fun getMusic(url:String){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val binding = getBind() ?: return
                    for(ds: DataSnapshot in snapshot.children) {
                        val data = ds.getValue<MusicData>()
                        if(data != null){
                            list.add(data)
                        }
                    }
                    binding.likeList.adapter = musicListAdapter
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

    private fun getBind(): FragmentLikeBinding? {
        return _binding
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}