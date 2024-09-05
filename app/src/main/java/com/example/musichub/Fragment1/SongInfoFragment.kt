package com.example.musichub.Fragment1

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.musichub.Activity.SongEditActivity
import com.example.musichub.Data.MusicData
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.databinding.FragmentSongInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import java.util.Calendar

class SongInfoFragment : Fragment() {

    private var _binding: FragmentSongInfoBinding? = null
    private val binding get() = _binding!!
    private val ONE_DAY: Int = 24 * 60 * 60 * 1000
    lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongInfoBinding.inflate(inflater, container, false)

        val getUrl: String = arguments?.getString("url").toString()

        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)
        dialog.show()

        binding.infoSongName.setSingleLine(true)
        binding.infoSongName.ellipsize = TextUtils.TruncateAt.MARQUEE
        binding.infoSongName.isSelected = true

        getData(getUrl)

        binding.infoBackBtn.setOnClickListener{
            back()
        }

        // 곡 정보 수정
        binding.infoEditBtn.setOnClickListener{
            val intent = Intent(requireContext(), SongEditActivity::class.java)
            intent.putExtra("url", getUrl)
            startActivity(intent)
        }

        return binding.root
    }

    private fun getData(url: String){
        // 스트리밍 수
        FirebaseDatabase.getInstance().getReference("History").orderByChild("songUrl").equalTo(url)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var num = 0
                    for(ds in snapshot.children){
                        num += 1
                    }
                    binding.infoSongPlay.text = num.toString()
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            })
        // 좋아요
        FirebaseDatabase.getInstance().getReference("Like").orderByChild("songUrl").equalTo(url)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var num = 0
                    for(ds in snapshot.children){
                        num += 1
                    }
                    binding.infoSongLike.text = num.toString()
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            })
        // 음악 데이터
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds:DataSnapshot in snapshot.children){
                        val data = ds.getValue<MusicData>()

                        if (data != null) {
                            binding.infoSongName.text = data.songName
                            binding.infoSongArtist.text = data.songArtist
                            binding.infoSongDuration.text = " · " + data.songDuration
                            binding.infoSongDesc.text = data.songInfo
                            Glide.with(requireContext()).load(data.imageUrl).into(binding.infoSongThumnail)
                            getDay(data.time, data.songCategory)

                            if(data.email == FirebaseAuth.getInstance().currentUser?.email.toString()){
                                binding.infoEditBtn.visibility = View.VISIBLE
                            } else {
                                binding.infoEditBtn.visibility = View.GONE
                            }
                        }
                    }
                    dialog.dismiss()
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            })
    }

    private fun back(){
        val mainActivity = (activity as MainActivity)
        val fragmentManager = mainActivity.supportFragmentManager
        fragmentManager.beginTransaction().remove(this).commit()
        fragmentManager.popBackStack()
    }

    private fun getDay(time: String, category:String){
        val times = time.split("/")

        val dDayCalendar = Calendar.getInstance()
        // 입력 받은 날짜로 설정한다
        dDayCalendar.set(times[0].toInt(), times[1].toInt() - 1, times[2].toInt())

        // millisecond 으로 환산한 뒤 입력한 날짜에서 현재 날짜의 차를 구한다
        val dDay: Long = dDayCalendar.timeInMillis / ONE_DAY
        val today: Long = System.currentTimeMillis() / ONE_DAY
        var result = today - dDay
        val goalDate: String

        if (result <= 1) {
            goalDate = "$category · $result day ago"
            binding.infoSongTime.text = goalDate
        } else if (result <= 30) {
            goalDate = "$category · $result days ago"
            binding.infoSongTime.text = goalDate
        } else if (result <= 365) {
            result /= 30
            goalDate = if (result <= 1) {
                "$category · $result month ago"
            } else {
                "$category · $result months ago"
            }
            binding.infoSongTime.text = goalDate
        } else {
            result /= 365
            goalDate = if (result <= 1) {
                "$category · $result year ago"
            } else {
                "$category · $result years ago"
            }
            binding.infoSongTime.text = goalDate
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}