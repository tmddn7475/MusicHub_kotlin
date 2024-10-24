package com.example.musichub.Activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.musichub.Object.Command
import com.example.musichub.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // preference
        val preference = getSharedPreferences("com.example.musichub_preferences", Activity.MODE_PRIVATE)
        val editor = preference.edit()

        val current: String = preference.getString("display_mode", "system").toString()
        editor.putString("display_mode", current)
        editor.apply() // 저장

        // 다크 모드 설정
        val str = preference.getString("display_mode", null).toString()
        when (str){
            "system" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        val internet = Command.getInternet(this)
        if(internet == 0){
            val alertEx: AlertDialog.Builder = AlertDialog.Builder(this)
            alertEx.setMessage(getString(R.string.check_network))
            alertEx.setNegativeButton(getString(R.string.ok)) { _, _ ->
                finishAffinity()
            }.setCancelable(false)
            val alert = alertEx.create()
            alert.show()
        } else {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }, 1500)
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // intro 화면 동안 뒤로 버튼을 눌러도 종료가 되지 않도록 설정
        }
    }
}