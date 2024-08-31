package com.example.musichub.Fragment2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.example.musichub.Adapter.Base.MyAlbumAdapter
import com.example.musichub.Object.Command
import com.example.musichub.Data.AlbumData
import com.example.musichub.R
import com.example.musichub.databinding.FragmentSongToAlbumBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class SongToAlbumFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSongToAlbumBinding? = null
    private val binding get() = _binding!!
    lateinit var albumAdapter: MyAlbumAdapter

    val list = mutableListOf<AlbumData>()
    val keyList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongToAlbumBinding.inflate(inflater, container, false)
        val url: String = arguments?.getString("url").toString()
        albumAdapter = MyAlbumAdapter(list, keyList)

        getList()

        binding.songToListView.setOnItemClickListener{ _, _, position, _ ->
            val key = keyList[position]
            Command.putTrack(key, url)
            Toast.makeText(requireContext(), getString(R.string.song_to_list), Toast.LENGTH_SHORT).show()
            dismiss()
        }

        return binding.root
    }

    private fun getList(){
        val email = FirebaseAuth.getInstance().currentUser?.email
        FirebaseDatabase.getInstance().getReference("PlayLists").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.children.iterator().hasNext()){
                        for(ds: DataSnapshot in snapshot.children){
                            val data = ds.getValue<AlbumData>()
                            if (data != null){
                                list.add(data)
                                keyList.add(ds.key.toString())
                            }
                        }
                        binding.songToListView.adapter = albumAdapter
                        binding.songToListText.visibility = View.GONE
                    } else {
                        binding.songToListText.visibility = View.VISIBLE
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetBehavior = BottomSheetBehavior.from<View>(view.parent as View)
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
        bottomSheetBehavior.peekHeight = view.measuredHeight
        bottomSheetBehavior.isDraggable = false

        view.findViewById<ImageView>(R.id.song_to_list_dismiss).setOnClickListener{
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}