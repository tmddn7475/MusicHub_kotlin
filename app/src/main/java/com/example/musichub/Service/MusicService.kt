package com.example.musichub.Service

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ConnectionResult.AcceptedResultBuilder
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.musichub.Data.MusicData
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.RoomDB.PlaylistDatabase
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: Editor
    lateinit var player: ExoPlayer

    val PREV_MUSIC: String = "prev_music"
    val NEXT_MUSIC: String = "next_music"
    val STOP_MUSIC: String = "stop_music"

    val customCommandNext = SessionCommand(NEXT_MUSIC, Bundle.EMPTY)
    val customCommandPrev = SessionCommand(PREV_MUSIC, Bundle.EMPTY)
    val customCommandStop = SessionCommand(STOP_MUSIC, Bundle.EMPTY)

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val stopBtn = CommandButton.Builder().setDisplayName("Stop").setIconResId(R.drawable.baseline_clear_24)
            .setSessionCommand(SessionCommand(STOP_MUSIC, Bundle())).build()
        val prevBtn = CommandButton.Builder().setDisplayName("Prev").setIconResId(R.drawable.ic_previous_white)
            .setSessionCommand(SessionCommand(PREV_MUSIC, Bundle())).build()
        val nextBtn = CommandButton.Builder().setDisplayName("Next").setIconResId(R.drawable.ic_next_white)
            .setSessionCommand(SessionCommand(NEXT_MUSIC, Bundle())).build()

        preferences = getSharedPreferences("pref", Activity.MODE_PRIVATE)
        editor = preferences.edit()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(MediaSessionCallBack())
            .setCustomLayout(ImmutableList.of(prevBtn, nextBtn, stopBtn))
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

    // 다음 곡
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

    // 전 곡
    fun prevSong(){
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
                    if(num > 0){
                        editor.putString("url", playlistData[num - 1].songUrl)
                        editor.apply()
                        playMusic(playlistData[num - 1].songUrl)
                    } else {
                        editor.putString("url", playlistData[playlistData.size - 1].songUrl)
                        editor.apply()
                        playMusic(playlistData[playlistData.size - 1].songUrl)
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

    // 커스텀 레이아웃
    private inner class MediaSessionCallBack: MediaSession.Callback {
        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            return AcceptedResultBuilder(session)
                .setAvailablePlayerCommands(
                    MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                        .remove(COMMAND_SEEK_TO_NEXT)
                        .remove(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                        .remove(COMMAND_SEEK_TO_PREVIOUS)
                        .remove(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                        .build()
                )
                .setAvailableSessionCommands(
                    MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                        .add(customCommandStop)
                        .add(customCommandNext)
                        .add(customCommandPrev)
                        .build()
                )
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == NEXT_MUSIC) {
                nextSong()
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            } else if (customCommand.customAction == STOP_MUSIC){
                player.stop()
                stopSelf()
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            } else if (customCommand.customAction == PREV_MUSIC){
                prevSong()
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }
}