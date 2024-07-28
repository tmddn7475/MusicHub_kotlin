package com.example.musichub

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.musichub.Data.HistoryData
import com.example.musichub.Data.MusicData
import com.example.musichub.Fragment1.FeedFragment
import com.example.musichub.Fragment1.HomeFragment
import com.example.musichub.Fragment1.Library.LibraryFragment
import com.example.musichub.Fragment1.SearchFragment
import com.example.musichub.Fragment2.EtcFragment
import com.example.musichub.Fragment2.MediaFragment
import com.example.musichub.Fragment2.PlaylistFragment
import com.example.musichub.Interface.MusicListener
import com.example.musichub.RoomDB.PlaylistDatabase
import com.example.musichub.RoomDB.PlaylistEntity
import com.example.musichub.Service.MusicService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.MoreExecutors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), MusicListener {

    lateinit var bar_thumnail:ImageView
    lateinit var bar_playlist_btn:ImageView
    lateinit var bar_song:TextView
    lateinit var bar_artist:TextView
    lateinit var bottomNavigationView:BottomNavigationView
    lateinit var bar_progress:LinearProgressIndicator
    lateinit var bar_play_btn:ImageView
    lateinit var handler:Handler
    lateinit var preference:SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    lateinit var mediaFragment:MediaFragment
    lateinit var playlistFragment: PlaylistFragment
    lateinit var etcFragment: EtcFragment

    lateinit var email: String

    private var mediaController: MediaController? = null
    private var db: PlaylistDatabase? = null
    var current_url:String = ""

    override fun onStart() {
        val sharedUrl: String = preference.getString("url", null).toString()
        if (!sharedUrl.equals("")) {
            getMusic(sharedUrl)
            current_url = sharedUrl
        }

        val sessionToken = SessionToken(this@MainActivity, ComponentName(this@MainActivity, MusicService::class.java))
        val controllerFuture = MediaController.Builder(this@MainActivity, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            if((sharedUrl != "") and (mediaController?.isPlaying == false)){
                readyMusic(sharedUrl)
            }
            updateProgressIndicator()
        }, MoreExecutors.directExecutor())

        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handler = Handler(Looper.getMainLooper())
        email = FirebaseAuth.getInstance().currentUser?.email.toString()
        db = PlaylistDatabase.getInstance(this)

        mediaFragment = MediaFragment(this)
        playlistFragment = PlaylistFragment()
        etcFragment = EtcFragment()

        bar_progress = findViewById(R.id.media_bar_progress)
        bar_play_btn = findViewById(R.id.bar_play_pause_btn)
        bar_thumnail = findViewById(R.id.bar_song_thumnail)
        bar_playlist_btn = findViewById(R.id.bar_playlist_btn)
        bar_song = findViewById(R.id.bar_song_name)
        bar_artist = findViewById(R.id.bar_song_artist)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // 새로고침
        val swipe: SwipeRefreshLayout = findViewById(R.id.refreshLayout)
        swipe.setDistanceToTriggerSync(600)
        swipe.setOnRefreshListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.container)
            if (fragment != null) {
                supportFragmentManager.beginTransaction().detach(fragment).commit()
                supportFragmentManager.beginTransaction().attach(fragment).commit()
            }
            swipe.isRefreshing = false
        }

        // bottomNavigationView
        supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment(this)).commit()
        bottomNavigationView.setOnItemSelectedListener {
            replaceFragment(
                when (it.itemId) {
                    R.id.bottom_home -> HomeFragment(this)
                    R.id.bottom_feed -> FeedFragment()
                    R.id.bottom_search -> SearchFragment()
                    else -> LibraryFragment()
                }
            )
            true
        }
        bar_song.setSingleLine(true)
        bar_song.ellipsize = TextUtils.TruncateAt.MARQUEE // 흐르게 만들기
        bar_song.isSelected = true

        // media controller
        val frameLayout:FrameLayout = findViewById(R.id.media_player_bar_bg)
        frameLayout.setOnClickListener{
            if(bar_song.text != ""){
                mediaFragment.show(supportFragmentManager, mediaFragment.tag)
                mediaFragment.setController(mediaController)
            }
        }

        // 플레이리스트
        bar_playlist_btn.setOnClickListener{
            if(bar_song.text != ""){
                playlistFragment.show(supportFragmentManager, playlistFragment.tag)
                playlistFragment.setController(mediaController)
            }
        }

        // 음악 재생
        bar_play_btn.setOnClickListener{
            if(bar_song.text != ""){
                if(mediaController?.isPlaying == true){
                    mediaController?.pause()
                    bar_play_btn.setImageResource(R.drawable.play_arrow)
                } else {
                    mediaController?.play()
                    bar_play_btn.setImageResource(R.drawable.pause)
                }
            }
        }

        // 듣고 있던 곡 저장
        preference = getSharedPreferences("pref", Activity.MODE_PRIVATE)
        editor = preference.edit()
        // preferences 초기값
        val current: String = preference.getString("url", "").toString()
        editor.putString("url", current)
        editor.apply() // 저장

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun updateProgressIndicator(){
        if (mediaController?.isPlaying() == true) {
            bar_progress.setMax(mediaController?.duration!!.toInt())
            bar_progress.setProgress(mediaController?.currentPosition!!.toInt())

            if (current_url != preference.getString("url", null)) {
                current_url = preference.getString("url", null).toString()
                getMusic(current_url)
                if(playlistFragment.isAdded){
                    playlistFragment.setUpCurrent(current_url)
                }
                if(mediaFragment.isAdded){
                    mediaFragment.setUpCurrent(current_url)
                }
            }

            if(playlistFragment.isAdded){
                playlistFragment.playlist_progress.setProgress(mediaController?.currentPosition!!.toInt())
                playlistFragment.playlist_progress.max = mediaController?.duration!!.toInt()
            }
            if(mediaFragment.isAdded){
                mediaFragment.media_seekbar.setProgress(mediaController?.currentPosition!!.toInt())
                mediaFragment.media_seekbar.max = mediaController?.duration!!.toInt()
                updatePlayingTime()
            }
        }
        checkPlayState()
        checkInternet()
    }

    // MediaFragment 플레이 시간 업데이트
    @SuppressLint("SetTextI18n")
    private fun updatePlayingTime(){
        val millis = mediaController?.currentPosition!!
        val totalSecs = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS)
        val minute = TimeUnit.MINUTES.convert(totalSecs, TimeUnit.SECONDS)
        val secs = totalSecs - (minute * 60)

        if(secs < 10){
            mediaFragment.media_song_current.text = "$minute:0$secs"
        } else {
            mediaFragment.media_song_current.text = "$minute:$secs"
        }
    }

    // 상태 확인
    private fun checkPlayState() {
        if (mediaController?.isPlaying() == true) {
            bar_play_btn.setImageResource(R.drawable.pause)
        } else if (mediaController?.isPlaying() == false) {
            bar_play_btn.setImageResource(R.drawable.play_arrow)
        }
    }

    // 인터넷 연결 확인
    private fun checkInternet(){
        val internet = Command().getInternet(this)
        if(internet == 0){
            val alertEx: AlertDialog.Builder = AlertDialog.Builder(this)
            alertEx.setMessage("네트워크 연결을 해주시길 바랍니다")
            alertEx.setNegativeButton("확인") { _, _ ->
                finishAffinity()
                onDestroy()
            }.setCancelable(false)
            val alert = alertEx.create()
            alert.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
            alert.show()
        } else {
            handler.postDelayed({ this.updateProgressIndicator() }, 330)
        }
    }

    // fragment
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    private fun getMusic(url: String){
        editor.putString("url", url)
        editor.apply()

        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val mld = ds.getValue<MusicData>()
                        if (mld != null) {
                            if(!this@MainActivity.isFinishing){
                                Glide.with(this@MainActivity).load(mld.imageUrl).into(bar_thumnail)
                            }
                            bar_song.text = mld.songName
                            bar_artist.text = mld.songArtist
                            saveSong(mld.songUrl)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "오류가 발생했습니다! 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun readyMusic(url: String){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val mld = ds.getValue<MusicData>()
                        if (mld != null) {
                            val item: MediaItem = MediaItem.Builder()
                                .setMediaId("musichub").setUri(mld.songUrl)
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setArtist(mld.songArtist)
                                        .setTitle(mld.songName)
                                        .setArtworkUri(Uri.parse(mld.imageUrl))
                                        .build()
                                ).build()
                            mediaController?.setMediaItem(item)
                            mediaController?.prepare()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // roomDB 저장
    fun saveSong(url: String){
        val r = Runnable {
            if(db?.musicDAO()?.getCount(url)!! < 1){
                val data = PlaylistEntity(songUrl = url, time = Command().getTime2())
                db?.musicDAO()?.saveSong(data)!!
            }
        }
        val thread = Thread(r)
        thread.start()
    }

    override fun playMusic(str: String) {
        getMusic(str)
        readyMusic(str)
        putHistory(str)
        if(mediaController != null){
            mediaController?.play()
        }
    }

    override fun nextMusic() {
        nextSong()
    }

    override fun prevMusic() {
        prevSong()
    }

    // 다음 곡
    private fun nextSong(){
         val run = Runnable {
            try {
                val playlistData = db?.musicDAO()?.getPlaylist()!!
                var num = 0

                for(i in playlistData.indices){
                    if(playlistData[i].songUrl == preference.getString("url", null)){
                        num = i
                        break
                    }
                }
                if(playlistData.isNotEmpty()){
                    if(num < playlistData.size - 1){
                        editor.putString("url", playlistData[num + 1].songUrl)
                        editor.apply()
                        readyMusic(playlistData[num + 1].songUrl)
                        mediaController?.play()
                    } else {
                        editor.putString("url", playlistData[0].songUrl)
                        editor.apply()
                        readyMusic(playlistData[0].songUrl)
                        mediaController?.play()
                    }
                } else {
                    mediaController?.pause()
                }
            } catch (e: Exception) {
                Log.d("tag", "Error - $e")
            }
        }
        val thread = Thread(run)
        thread.start()
    }

    // 전 곡
    private fun prevSong(){
        val run = Runnable {
            try {
                val playlistData = db?.musicDAO()?.getPlaylist()!!
                var num = 0

                for(i in playlistData.indices){
                    if(playlistData[i].songUrl == preference.getString("url", null)){
                        num = i
                        break
                    }
                }
                if(playlistData.isNotEmpty()){
                    if(num > 0){
                        editor.putString("url", playlistData[num - 1].songUrl)
                        editor.apply()
                        readyMusic(playlistData[num - 1].songUrl)
                        mediaController?.play()
                    } else {
                        editor.putString("url", playlistData[playlistData.size - 1].songUrl)
                        editor.apply()
                        readyMusic(playlistData[playlistData.size - 1].songUrl)
                        mediaController?.play()
                    }
                } else {
                    mediaController?.pause()
                }
            } catch (e: Exception) {
                Log.d("tag", "Error - $e")
            }
        }
        val thread = Thread(run)
        thread.start()
    }

    private fun putHistory(str: String){
        // 현재 타임스탬프를 가져오기
        val timestamp = Calendar.getInstance().timeInMillis
        // 최대 타임스탬프 값에서 현재 타임스탬프를 뺀 값을 키로 사용, 데이터를 내림차순으로 저장
        val descendingKey = Long.MAX_VALUE - timestamp

        val historyData = HistoryData(songUrl = str, email = email, time = Command().getTime2())
        FirebaseDatabase.getInstance().getReference("History").child(descendingKey.toString()).setValue(historyData)
    }

    private var backPressedTime: Long = 0
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val fragment = supportFragmentManager.findFragmentById(R.id.container)

            if(fragment is HomeFragment) {
                if (System.currentTimeMillis() > backPressedTime + 2000) {
                    backPressedTime = System.currentTimeMillis()
                    Snackbar.make(
                        findViewById(R.id.refreshLayout),
                        "뒤로 버튼을 한번 더 누르면 종료됩니다",
                        Snackbar.LENGTH_SHORT
                    ).setAnchorView(R.id.include)
                        .setTextColor(Color.WHITE).setBackgroundTint(Color.parseColor("#323232"))
                        .show()
                } else if (System.currentTimeMillis() <= backPressedTime + 2000) {
                    finishAffinity()
                }
            } else if (fragment is FeedFragment || fragment is SearchFragment || fragment is LibraryFragment) {
                supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment(MainActivity())).commit()
                bottomNavigationView.selectedItemId = R.id.bottom_home
            } else {
                if (fragment != null) {
                    supportFragmentManager.beginTransaction().remove(fragment).commit()
                    supportFragmentManager.popBackStack()
                }
            }
        }
    }
}