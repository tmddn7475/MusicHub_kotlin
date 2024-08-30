package com.example.musichub.Activity

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.musichub.MainActivity
import com.example.musichub.R
import com.example.musichub.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private var saveLoginData:Boolean = false
    private lateinit var appData: SharedPreferences
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityLoginBinding

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser != null && appData.getBoolean("SAVE_LOGIN_DATA", false)){
            complete()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)

        appData = getSharedPreferences("appData", MODE_PRIVATE)
        load()

        binding.loginBtn.setOnClickListener{
            val email:String = binding.emailEdit.text.toString()
            val password:String = binding.passwordEdit.text.toString()
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
                    Toast.makeText(this, getString(R.string.try_again), Toast.LENGTH_SHORT).show()
                }
            }

        }

        binding.registerBtn.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun save(){
        val editor = appData.edit()
        editor.putBoolean("SAVE_LOGIN_DATA", binding.loginChk.isChecked)
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
                Snackbar.make(findViewById(R.id.login_activity), getString(R.string.tap_back_again), Snackbar.LENGTH_SHORT)
                    .setTextColor(Color.WHITE).setBackgroundTint(Color.parseColor("#323232")).show()
            } else if (System.currentTimeMillis() <= backPressedTime + 2000){
                finishAffinity()
            }
        }
    }
}