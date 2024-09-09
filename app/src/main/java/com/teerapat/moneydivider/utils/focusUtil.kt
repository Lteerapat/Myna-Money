package com.teerapat.moneydivider.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

fun Fragment.openSoftKeyboard(context: Context, view: View) {
    view.requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    view.postDelayed({
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }, 100)
}