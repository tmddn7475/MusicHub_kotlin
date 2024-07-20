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
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.musichub.Activity.UploadActivity
import com.example.musichub.Data.AccountData
import com.example.musichub.R
import com.example.musichub.Adapter.ViewPagerAdapter
import com.example.musichub.MainActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text

class AccountFragment : Fragment() {

    lateinit var account_circleImage:CircleImageView
    lateinit var account_upload: ImageView
    lateinit var account_follow: ImageView
    lateinit var account_edit: ImageView
    lateinit var account_name:TextView
    lateinit var account_followers:TextView
    lateinit var account_following:TextView
    lateinit var account_info:TextView
    lateinit var account_show_more:TextView
    lateinit var dialog: Dialog

    var getEmail:String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_account, container, false)

        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)
        dialog.show()

        account_upload = v.findViewById(R.id.account_upload)
        account_follow = v.findViewById(R.id.account_follow)
        account_edit = v.findViewById(R.id.account_edit)
        account_circleImage = v.findViewById(R.id.account_circleImage)
        account_name = v.findViewById(R.id.account_name)
        account_followers = v.findViewById(R.id.account_followers)
        account_following = v.findViewById(R.id.account_following)
        account_info = v.findViewById(R.id.account_info)
        account_show_more = v.findViewById(R.id.account_show_more)

        // 계정정보
        getEmail = arguments?.getString("email").toString()
        getAccountData(getEmail)

        // viewPager
        val tabLayout: TabLayout = v.findViewById(R.id.account_tabLayout)
        val viewPager: ViewPager2 = v.findViewById(R.id.account_viewPager)
        val viewPagerAdapter = ViewPagerAdapter(requireActivity())

        val trackFragment = AccountTrackFragment()
        val albumFragment = AccountAlbumFragment()

        val bundle = Bundle()
        bundle.putString("email", getEmail)
        trackFragment.arguments = bundle
        albumFragment.arguments = bundle

        viewPagerAdapter.addFragment(trackFragment, "곡")
        viewPagerAdapter.addFragment(albumFragment, "앨범")

        viewPager.adapter = viewPagerAdapter
        val tm = TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.setText(viewPagerAdapter.getTitle(position))
        }
        tm.attach()

        account_show_more.setOnClickListener{
            showMore()
        }

        val account_back_btn:ImageView = v.findViewById(R.id.account_back_btn)
        account_back_btn.setOnClickListener{
            back()
        }

        account_upload.setOnClickListener{
            val intent = Intent(requireContext(), UploadActivity::class.java)
            startActivity(intent)
        }

        return v
    }

    private fun getAccountData(email: String){
        val account = FirebaseAuth.getInstance().currentUser?.email.toString()

        if(account == email){
            account_upload.visibility = View.VISIBLE
            account_follow.visibility = View.GONE
            account_edit.visibility = View.VISIBLE
        } else {
            account_upload.visibility = View.GONE
            account_follow.visibility = View.VISIBLE
            account_edit.visibility = View.GONE
        }

        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email").equalTo(email).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val data = ds.getValue<AccountData>()
                        if (data != null) {
                            account_name.text = data.nickname

                            if(data.imageUrl == ""){
                                account_circleImage.setImageResource(R.drawable.baseline_account_circle_24)
                            } else {
                                Glide.with(requireContext()).load(data.imageUrl).into(account_circleImage)
                            }

                            if(data.info == "") {
                                account_info.visibility = View.GONE
                                account_show_more.visibility = View.GONE
                            } else {
                                account_info.visibility = View.VISIBLE
                                account_info.text = data.info
                                if(account_info.lineCount < 2){
                                    account_show_more.visibility = View.GONE
                                } else {
                                    account_show_more.visibility = View.VISIBLE
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
                    var num = 0
                    for (ds: DataSnapshot in snapshot.children) num++

                    if(num < 2){
                        account_followers.text = "$num follower · "
                    } else {
                        account_followers.text = "$num followers · "
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference("Follow").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    var num = 0
                    for (ds: DataSnapshot in snapshot.children) num++

                    account_following.text = "$num following"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showMore(){
        if(account_info.maxLines == 1){
            account_info.maxLines = Int.MAX_VALUE
            account_info.ellipsize = null
            account_show_more.text = "닫기"
        } else {
            account_info.maxLines = 1
            account_info.ellipsize = TextUtils.TruncateAt.END
            account_show_more.text = "더 보기"
        }
    }

    private fun back(){
        val mainActivity = (activity as MainActivity)
        val fragmentManager = mainActivity.supportFragmentManager
        fragmentManager.beginTransaction().remove(this).commit()
        fragmentManager.popBackStack()
    }
}