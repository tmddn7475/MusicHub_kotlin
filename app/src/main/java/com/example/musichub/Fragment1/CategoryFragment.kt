package com.example.musichub.Fragment1

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
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

class CategoryFragment : Fragment(), MusicListListener {

    lateinit var category_list: ListView
    lateinit var category_text: TextView
    lateinit var musicListAdapter: MusicListAdapter
    lateinit var musicListener: MusicListener

    var list_item = mutableListOf<MusicData>()

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
        val v = inflater.inflate(R.layout.fragment_category, container, false)

        val category: String = arguments?.getString("category").toString()
        val category_name: TextView = v.findViewById(R.id.category_name)
        val category_image: ImageView = v.findViewById(R.id.category_image)
        val category_back_btn: ImageView = v.findViewById(R.id.category_back_btn)
        category_list = v.findViewById(R.id.category_list)
        category_text = v.findViewById(R.id.category_text)

        category_name.text = category
        category_image.setImageResource(getImage(category))

        musicListAdapter = MusicListAdapter(list_item, this)
        getSongs(category)

        category_list.setOnItemClickListener { parent, view, position, id ->
            val data = list_item[position]
            val url:String = data.songUrl
            musicListener.playMusic(url)
        }

        category_back_btn.setOnClickListener{
            back()
        }

        return v
    }

    private fun getImage(str: String): Int = when(str) {
        "Ambient" -> R.drawable.ambient
        "Classical" -> R.drawable.classical
        "Disco" -> R.drawable.disco
        "Dance & EDM" -> R.drawable.edm
        "Hip hop" -> R.drawable.hiphop
        "Jazz" -> R.drawable.jazz
        "R&B" -> R.drawable.rnb
        "Reggae" -> R.drawable.reggae
        "Rock" -> R.drawable.rock
        else -> R.drawable.ic_baseline_library_music_24
    }

    private fun getSongs(str: String){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songCategory").equalTo(str)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.children.iterator().hasNext()){
                        list_item.clear()
                        for(ds: DataSnapshot in snapshot.children) {
                            val data = ds.getValue<MusicData>()
                            if(data != null){
                                list_item.add(data)
                            }
                        }
                        category_list.adapter = musicListAdapter
                        category_text.visibility = View.GONE
                    } else {
                        category_list.visibility = View.GONE
                        category_text.visibility = View.VISIBLE
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun back(){
        val mainActivity = (activity as MainActivity)
        val fragmentManager = mainActivity.supportFragmentManager
        fragmentManager.beginTransaction().remove(this).commit()
        fragmentManager.popBackStack()
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