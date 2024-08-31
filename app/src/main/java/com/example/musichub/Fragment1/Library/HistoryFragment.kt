package com.example.musichub.Fragment1.Library

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.musichub.Adapter.Base.MusicListAdapter
import com.example.musichub.Data.HistoryData
import com.example.musichub.Data.MusicData
import com.example.musichub.Interface.MusicListListener
import com.example.musichub.Interface.MusicListener
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class HistoryFragment : Fragment(), MusicListListener {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    var arr1 = mutableListOf<String>()
    var items = mutableListOf<MusicData>()
    lateinit var musicListAdapter: MusicListAdapter

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
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val email: String = FirebaseAuth.getInstance().currentUser?.email.toString()
        musicListAdapter = MusicListAdapter(items,this)

        FirebaseDatabase.getInstance().getReference("History").orderByChild("email").equalTo(email)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    arr1.clear()
                    items.clear()
                    for(ds: DataSnapshot in snapshot.children) {
                        val data = ds.getValue<HistoryData>()
                        if(data != null){
                            if(!arr1.contains(data.songUrl)){
                                arr1.add(data.songUrl)
                                getSongs(data)
                            }
                        }
                    }
                    if(arr1.size == 0){
                        binding.historyText.visibility = View.VISIBLE
                    } else {
                        binding.historyText.visibility = View.GONE
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        binding.historyList.setOnItemClickListener{ parent, view, position, id ->
            val data = items[position]
            val url:String = data.songUrl
            musicListener.playMusic(url)
        }

        return binding.root
    }

    private fun getSongs(data: HistoryData){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(data.songUrl).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val mld = ds.getValue<MusicData>()
                        if(mld != null){
                            mld.time = data.time
                            items.add(mld)
                        }
                    }
                    musicListAdapter.sort()
                    binding.historyList.adapter = musicListAdapter
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show()
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}