package com.example.musichub.Fragment1

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
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.databinding.FragmentCategoryBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class CategoryFragment : Fragment(), MusicListListener {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    lateinit var musicListAdapter: MusicListAdapter
    lateinit var musicListener: MusicListener
    var listItem = mutableListOf<MusicData>()

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
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)

        val category: String = arguments?.getString("category").toString()

        binding.categoryName.text = category
        binding.categoryImage.setImageResource(getImage(category))

        musicListAdapter = MusicListAdapter(listItem, this)
        getSongs(category)

        binding.categoryList.setOnItemClickListener { _, _, position, _ ->
            val data = listItem[position]
            val url:String = data.songUrl
            musicListener.playMusic(url)
        }

        binding.categoryBackBtn.setOnClickListener{
            back()
        }

        return binding.root
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
                        listItem.clear()
                        for(ds: DataSnapshot in snapshot.children) {
                            val data = ds.getValue<MusicData>()
                            if(data != null){
                                listItem.add(data)
                            }
                        }
                        binding.categoryList.adapter = musicListAdapter
                        binding.categoryText.visibility = View.GONE
                    } else {
                        binding.categoryList.visibility = View.GONE
                        binding.categoryText.visibility = View.VISIBLE
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}