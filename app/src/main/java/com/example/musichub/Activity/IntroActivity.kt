package com.example.musichub.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.musichub.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }, 1500) //1.5초 후 로그인 액티비티로 넘어감
    }
}