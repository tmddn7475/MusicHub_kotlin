package com.example.musichub.Fragment1.Account

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.musichub.Activity.AccountEditActivity
import com.example.musichub.Activity.UploadActivity
import com.example.musichub.Data.AccountData
import com.example.musichub.R
import com.example.musichub.Adapter.ViewPagerAdapter
import com.example.musichub.Object.Command
import com.example.musichub.Fragment1.Follow.FollowerFragment
import com.example.musichub.Fragment1.Follow.FollowingFragment
import com.example.musichub.MainActivity
import com.example.musichub.databinding.FragmentAccountBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class AccountFragment : Fragment() {

    lateinit var dialog: Dialog
    private var getEmail:String = ""
    var followCheck: Boolean = false
    var followKey: String = ""

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)

        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)
        dialog.show()

        // 계정정보
        getEmail = arguments?.getString("email").toString()
        getAccountData(getEmail)

        // viewPager
        val viewPagerAdapter = ViewPagerAdapter(requireActivity())
        val trackFragment = AccountTrackFragment()
        val albumFragment = AccountAlbumFragment()

        val bundle = Bundle()
        bundle.putString("email", getEmail)
        trackFragment.arguments = bundle
        albumFragment.arguments = bundle

        viewPagerAdapter.addFragment(trackFragment, getString(R.string.track))
        viewPagerAdapter.addFragment(albumFragment, getString(R.string.album))

        binding.accountViewPager.adapter = viewPagerAdapter
        val tm = TabLayoutMediator(binding.accountTabLayout, binding.accountViewPager) { tab: TabLayout.Tab, position: Int ->
            tab.setText(viewPagerAdapter.getTitle(position))
        }
        tm.attach()

        binding.accountFollowers.setOnClickListener{
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val followerFragment = FollowerFragment()

            val b = Bundle()
            b.putString("email", getEmail)
            followerFragment.arguments = b
            fragmentManager.beginTransaction().replace(R.id.container, followerFragment).addToBackStack(null).commit()
        }

        binding.accountFollowing.setOnClickListener{
            val mainActivity = (activity as MainActivity)
            val fragmentManager = mainActivity.supportFragmentManager
            val followingFragment = FollowingFragment()

            val b = Bundle()
            b.putString("email", getEmail)
            followingFragment.arguments = b
            fragmentManager.beginTransaction().replace(R.id.container, followingFragment).addToBackStack(null).commit()
        }

        // 계정 info
        binding.accountShowMore.setOnClickListener{
            showMore()
        }

        binding.accountBackBtn.setOnClickListener{
            back()
        }

        // 업로드
        binding.accountUpload.setOnClickListener{
            val intent = Intent(requireContext(), UploadActivity::class.java)
            startActivity(intent)
        }

        // 계정 정보 수정
        binding.accountEdit.setOnClickListener{
            val intent = Intent(requireContext(), AccountEditActivity::class.java)
            startActivity(intent)
        }

        // 팔로우
        binding.accountFollow.setOnClickListener{
            if(followCheck){
                Command.unfollow(followKey)
                binding.accountFollow.setImageResource(R.drawable.baseline_person_add_24)
                Toast.makeText(requireContext(), getString(R.string.unfollow), Toast.LENGTH_SHORT).show()
                followCheck = false
            } else {
                Command.follow(getEmail)
                binding.accountFollow.setImageResource(R.drawable.baseline_person_add_disabled_24)
                Toast.makeText(requireContext(), getString(R.string.follow), Toast.LENGTH_SHORT).show()
                followCheck = true
            }
        }

        return binding.root
    }

    private fun getAccountData(email: String){
        val myEmail = FirebaseAuth.getInstance().currentUser?.email.toString()

        if(myEmail == email){
            binding.accountUpload.visibility = View.VISIBLE
            binding.accountFollow.visibility = View.GONE
            binding.accountEdit.visibility = View.VISIBLE
        } else {
            binding.accountUpload.visibility = View.GONE
            binding.accountFollow.visibility = View.VISIBLE
            binding.accountEdit.visibility = View.GONE
            setFollow(myEmail, email)
        }

        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email").equalTo(email).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val binding = getBind() ?: return
                    for(ds: DataSnapshot in snapshot.children) {
                        val data = ds.getValue<AccountData>()
                        if (data != null) {
                            binding.accountName.text = data.nickname

                            if(data.imageUrl == ""){
                                binding.accountCircleImage.setImageResource(R.drawable.baseline_account_circle_24)
                            } else {
                                Glide.with(requireContext()).load(data.imageUrl).into(binding.accountCircleImage)
                            }

                            if(data.info == "") {
                                binding.accountInfo.visibility = View.GONE
                                binding.accountShowMore.visibility = View.GONE
                            } else {
                                binding.accountInfo.visibility = View.VISIBLE
                                binding.accountInfo.text = data.info
                                if(binding.accountInfo.lineCount < 2){
                                    binding.accountShowMore.visibility = View.GONE
                                } else {
                                    binding.accountShowMore.visibility = View.VISIBLE
                                    showMore()
                                }
                            }
                        }
                    }
                    dialog.dismiss()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // 팔로워
        FirebaseDatabase.getInstance().getReference("Follow").orderByChild("follow").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val binding = getBind() ?: return
                    var num = 0
                    for (ds: DataSnapshot in snapshot.children) num++

                    if(num < 2){
                        binding.accountFollowers.text = "$num follower · "
                    } else {
                        binding.accountFollowers.text = "$num followers · "
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference("Follow").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val binding = getBind() ?: return
                    var num = 0
                    for (ds: DataSnapshot in snapshot.children) num++

                    binding.accountFollowing.text = "$num following"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setFollow(myEmail: String, email: String){
        val str: String = myEmail + "_" + email

        FirebaseDatabase.getInstance().getReference("Follow").orderByChild("email_follow").equalTo(str).limitToFirst(1)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val binding = getBind() ?: return
                    if(snapshot.children.iterator().hasNext()){
                        for (ds: DataSnapshot in snapshot.children){
                            followKey = ds.key.toString()
                            binding.accountFollow.setImageResource(R.drawable.baseline_person_add_disabled_24)
                            followCheck = true
                        }
                    } else {
                        binding.accountFollow.setImageResource(R.drawable.baseline_person_add_24)
                        followCheck = false
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    @SuppressLint("SetTextI18n")
    private fun showMore(){
        if(binding.accountInfo.maxLines == 1){
            binding.accountInfo.maxLines = Int.MAX_VALUE
            binding.accountInfo.ellipsize = null
            binding.accountShowMore.text = getString(R.string.show_less)
        } else {
            binding.accountInfo.maxLines = 1
            binding.accountInfo.ellipsize = TextUtils.TruncateAt.END
            binding.accountShowMore.text = getString(R.string.show_more)
        }
    }

    private fun back(){
        val mainActivity = (activity as MainActivity)
        val fragmentManager = mainActivity.supportFragmentManager
        fragmentManager.beginTransaction().remove(this).commit()
        fragmentManager.popBackStack()
    }

    private fun getBind(): FragmentAccountBinding? {
        return _binding
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}