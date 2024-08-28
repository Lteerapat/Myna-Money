package com.teerapat.moneydivider.utils

import android.content.res.ColorStateList
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.teerapat.moneydivider.R

fun Fragment.focusOnCard(
    cardView: View,
    isIncompleteCard: Boolean = true,
    getTargetView: (View) -> View = { it }
) {
    val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)

    val targetView = getTargetView(cardView)
    targetView.requestFocus()

    if (targetView is EditText) {
        targetView.text.clear()
        targetView.postDelayed({
            imm?.showSoftInput(targetView, InputMethodManager.SHOW_IMPLICIT)
        }, 100)

        if (isIncompleteCard) {
            targetView.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.red)
            )
            targetView.addTextChangedListener {
                targetView.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.teal_700
                        )
                    )

                val targetViewParent = targetView.parent as? View
                targetViewParent?.let {
                    val originalDrawable =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.rounded_corner_white_bg
                        )
                    if (it.background != originalDrawable) {
                        it.background = originalDrawable
                    }
                }
            }
        }
    } else {
        focusChangeBorderColorOnCard(targetView)
    }
}

fun Fragment.focusChangeBorderColorOnCard(targetView: View) {
    targetView.background =
        ContextCompat.getDrawable(requireContext(), R.drawable.incomplete_card_border)
}