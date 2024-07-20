package com.example.musichub.Activity

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private var saveLoginData:Boolean = false
    private lateinit var appData: SharedPreferences
    private lateinit var login_chk:CheckBox
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser != null && appData.getBoolean("SAVE_LOGIN_DATA", false)){
            complete()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)

        appData = getSharedPreferences("appData", MODE_PRIVATE)
        load()

        val email_edit:EditText = findViewById(R.id.email_edit)
        val password_edit:EditText = findViewById(R.id.password_edit)
        val login_btn:Button = findViewById(R.id.login_btn)
        val register_btn:Button = findViewById(R.id.register_btn)
        login_chk = findViewById(R.id.login_chk)

        login_btn.setOnClickListener{
            val email:String = email_edit.text.toString()
            val password:String = password_edit.text.toString()
            dialog.show()

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    dialog.dismiss()
                    save()
                    complete()
                } else {
                    // If sign in fails, display a message to the user.
                    dialog.dismiss()
                    Toast.makeText(this, "다시 시도해주세요", Toast.LENGTH_SHORT).show()
                }
            }

        }

        register_btn.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun save(){
        val editor = appData.edit()
        editor.putBoolean("SAVE_LOGIN_DATA", login_chk.isChecked)
        editor.apply()
    }

    private fun load(){
        saveLoginData = appData.getBoolean("SAVE_LOGIN_DATA", false)
    }

    private fun complete(){
        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    private var backPressedTime:Long = 0
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() > backPressedTime + 2000){
                backPressedTime = System.currentTimeMillis()
                Snackbar.make(findViewById(R.id.login_activity), "뒤로 버튼을 한번 더 누르면 종료됩니다", Snackbar.LENGTH_SHORT)
                    .setTextColor(Color.WHITE).setBackgroundTint(Color.parseColor("#323232")).show()
            } else if (System.currentTimeMillis() <= backPressedTime + 2000){
                finishAffinity()
            }
        }
    }
}