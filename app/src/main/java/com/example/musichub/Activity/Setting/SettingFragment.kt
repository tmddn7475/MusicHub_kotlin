package com.example.musichub.Activity.Setting

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.musichub.Activity.AccountEditActivity
import com.example.musichub.Activity.UploadActivity
import com.example.musichub.R

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
            else -> logOut()
        }
        return false
    }

    private fun logOut(){

    }
}