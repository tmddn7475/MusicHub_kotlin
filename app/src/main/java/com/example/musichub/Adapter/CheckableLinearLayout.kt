package com.example.musichub.Adapter

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.Checkable
import android.widget.LinearLayout
import com.example.musichub.R

class CheckableLinearLayout(context: Context, attrs: AttributeSet): LinearLayout(context, attrs), Checkable {
    override fun setChecked(checked: Boolean) {
        val cb: CheckBox = findViewById(R.id.edit_check)

        if (cb.isChecked != checked) {
            cb.isChecked = checked
        }
    }

    override fun isChecked(): Boolean {
        val cb: CheckBox = findViewById(R.id.edit_check)
        return cb.isChecked
    }

    override fun toggle() {
        val cb = findViewById<CheckBox>(R.id.edit_check)
        setChecked(!cb.isChecked)
    }
}