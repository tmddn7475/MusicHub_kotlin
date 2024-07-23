package com.example.musichub.Activity.Setting

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.musichub.R

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        supportFragmentManager.beginTransaction().replace(R.id.setting_layout, SettingFragment()).commit()

        val settingBackBtn: ImageView = findViewById(R.id.setting_back_btn)
        settingBackBtn.setOnClickListener{
            finish()
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(this@SettingActivity)
        preferences.registerOnSharedPreferenceChangeListener { _, key ->
            Log.d("TAG", "onSharedPreferenceChanged: $key")
        }
    }
}