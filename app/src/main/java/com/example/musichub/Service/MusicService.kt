package com.example.musichub.Service

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.musichub.Data.MusicData
import com.example.musichub.MainActivity
import com.example.musichub.RoomDB.PlaylistDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    lateinit var preferences: SharedPreferences
    lateinit var editor: Editor
    lateinit var player: ExoPlayer

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        preferences = getSharedPreferences("pref", Activity.MODE_PRIVATE)
        editor = preferences.edit()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(openMainActivityPendingIntent()).build()

        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if(playbackState == Player.STATE_ENDED){
                        nextSong()
                    }
                    super.onPlaybackStateChanged(playbackState)
                }
            }
        )
    }

    override fun startForegroundService(service: Intent?): ComponentName? {
        return super.startForegroundService(service)
    }

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        super.onUpdateNotification(session, true)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession!!.player
        if (player.playWhenReady) {
            // Make sure the service is not in foreground.
            player.pause()
        }
        stopSelf()
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun openMainActivityPendingIntent(): PendingIntent {
        val notifyIntent = Intent(this, MainActivity::class.java)
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        return PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun nextSong(){
        val db = PlaylistDatabase.getInstance(this)
        val run = Runnable {
            try {
                val playlistData = db?.musicDAO()?.getPlaylist()!!
                var num = 0

                for(i in playlistData.indices){
                    if(playlistData[i].songUrl == preferences.getString("url", null)){
                        num = i
                        break
                    }
                }

                if(playlistData.isNotEmpty()){
                    if(num < playlistData.size - 1){
                        editor.putString("url", playlistData[num + 1].songUrl)
                        editor.apply()
                        playMusic(playlistData[num + 1].songUrl)
                    } else {
                        editor.putString("url", playlistData[0].songUrl)
                        editor.apply()
                        playMusic(playlistData[0].songUrl)
                    }
                } else {
                    player.pause()
                }
            } catch (e: Exception) {
                Log.d("tag", "Error - $e")
            }
        }
        val thread = Thread(run)
        thread.start()
    }

    private fun playMusic(url: String){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("songUrl").equalTo(url).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
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
                            player.setMediaItem(item)
                            player.prepare()
                            player.play()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    player.stop()
                    stopSelf()
                }
            })
    }
}