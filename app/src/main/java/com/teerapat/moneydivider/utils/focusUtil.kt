package com.teerapat.moneydivider.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.openSoftKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    postDelayed({
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }, 100)
}