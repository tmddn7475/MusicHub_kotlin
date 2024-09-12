package com.example.musichub.Activity.Setting

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.musichub.Activity.AccountEditActivity
import com.example.musichub.Activity.LoginActivity
import com.example.musichub.Activity.UploadActivity
import com.example.musichub.Object.Command
import com.example.musichub.R
import com.google.firebase.auth.FirebaseAuth

class SettingFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val key: String = preference.key

        when (key) {
            "upload" -> {
                val intent = Intent(requireContext(), UploadActivity::class.java)
                startActivity(intent)
            }
            "account_edit" -> {
                val intent = Intent(requireContext(), AccountEditActivity::class.java)
                startActivity(intent)
            }
            "display_mode" -> {
                preference.setOnPreferenceChangeListener{ _, newValue ->
                    Log.d("Test", newValue.toString())
                    when (newValue){
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
                    true
                }
            }
            else -> logOut()
        }
        return false
    }

    private fun logOut(){
        val alertEx: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        alertEx.setMessage(getString(R.string.sign_out_alert))
        alertEx.setNegativeButton(getString(R.string.yes)) { _, _ ->
            Command.deleteAll(requireContext())
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            ActivityCompat.finishAffinity(requireActivity())
        }
        alertEx.setPositiveButton(getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }
        val alert = alertEx.create()
        alert.show()
    }
}