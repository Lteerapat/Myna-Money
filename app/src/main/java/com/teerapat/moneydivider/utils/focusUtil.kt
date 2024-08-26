package com.teerapat.moneydivider.utils

import android.content.res.ColorStateList
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.teerapat.moneydivider.R

fun Fragment.focusOnCard(cardView: View, getTargetEditText: (View) -> EditText) {
    val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)

    val targetEditText = getTargetEditText(cardView)

    targetEditText.requestFocus()
    targetEditText.text.clear()
    targetEditText.backgroundTintList = ColorStateList.valueOf(
        ContextCompat.getColor(requireContext(), R.color.red)
    )

    targetEditText.postDelayed({
        imm?.showSoftInput(targetEditText, InputMethodManager.SHOW_IMPLICIT)
    }, 100)

    targetEditText.addTextChangedListener {
        targetEditText.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.teal_700))
    }
}