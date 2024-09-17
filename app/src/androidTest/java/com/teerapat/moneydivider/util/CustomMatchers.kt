package com.teerapat.moneydivider.util

import android.content.res.ColorStateList
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

fun withBackgroundTintColor(expectedColorResId: Int): Matcher<View> {
    return object : BoundedMatcher<View, EditText>(EditText::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with background color: ")
        }

        override fun matchesSafely(editText: EditText): Boolean {
            return editText.backgroundTintList == ColorStateList.valueOf(
                ContextCompat.getColor(editText.context, expectedColorResId)
            )
        }
    }
}