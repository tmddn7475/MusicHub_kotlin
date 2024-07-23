package com.example.musichub.Activity

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.musichub.Command
import com.example.musichub.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val internet = Command().getInternet(this)

        if(internet == 0){
            val alert_ex: AlertDialog.Builder = AlertDialog.Builder(this)
            alert_ex.setMessage("네트워크 연결을 해주시길 바랍니다")
            alert_ex.setNegativeButton("확인") { _, _ ->
                finishAffinity()
            }.setCancelable(false)
            val alert = alert_ex.create()
            alert.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
            alert.show()
        } else {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }, 1500) //1.5초 후 로그인 액티비티로 넘어감
        }
    }
}