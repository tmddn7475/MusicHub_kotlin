package com.example.musichub.Activity.Setting

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
            else -> logOut()
        }
        return false
    }

    private fun logOut(){
        val alert_ex: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        alert_ex.setMessage("로그아웃하시겠습니까?")
        alert_ex.setNegativeButton("네") { _, _ ->
            Command.deleteAll(requireContext())
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            ActivityCompat.finishAffinity(requireActivity())
        }
        alert_ex.setPositiveButton("아니요") { dialog, _ ->
            dialog.dismiss()
        }
        val alert = alert_ex.create()
        alert.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
        alert.show()
    }
}