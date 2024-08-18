package com.example.musichub.Activity

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.musichub.Object.Command
import com.example.musichub.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val internet = Command.getInternet(this)

        if(internet == 0){
            val alertEx: AlertDialog.Builder = AlertDialog.Builder(this)
            alertEx.setMessage(getString(R.string.check_network))
            alertEx.setNegativeButton(getString(R.string.ok)) { _, _ ->
                finishAffinity()
            }.setCancelable(false)
            val alert = alertEx.create()
            alert.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
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
            // intro 화면 동안 종료가 되지 않도록 설정
        }
    }
}