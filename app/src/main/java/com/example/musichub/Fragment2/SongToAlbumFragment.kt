package com.example.musichub.Fragment2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.musichub.Adapter.Base.MyAlbumAdapter
import com.example.musichub.Object.Command
import com.example.musichub.Data.AlbumData
import com.example.musichub.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class SongToAlbumFragment : BottomSheetDialogFragment() {

    lateinit var song_to_list_view: ListView
    lateinit var song_to_list_text: TextView
    lateinit var albumAdapter: MyAlbumAdapter

    val list = mutableListOf<AlbumData>()
    val keyList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_song_to_album, container, false)

        val url: String = arguments?.getString("url").toString()

        song_to_list_view = v.findViewById(R.id.song_to_list_view)
        song_to_list_text = v.findViewById(R.id.song_to_list_text)
        albumAdapter = MyAlbumAdapter(list, keyList)

        getList()

        song_to_list_view.setOnItemClickListener{ _, _, position, _ ->
            val key = keyList[position]
            Command.putTrack(key, url)
            Toast.makeText(requireContext(), "곡이 앨범에 추가되었습니다\n" + "중복이 있을 경우 추가되지 않습니다", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        return v
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
                        song_to_list_view.adapter = albumAdapter
                        song_to_list_text.visibility = View.GONE
                    } else {
                        song_to_list_text.visibility = View.VISIBLE
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
}