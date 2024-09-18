package com.teerapat.moneydivider.util

import android.view.View
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import com.google.android.material.chip.Chip
import com.teerapat.moneydivider.widget.ThousandSeparatedEditText
import org.hamcrest.Matcher

fun setCursorPosition(position: Int): ViewAction {
    return ViewActions.actionWithAssertions(object : ViewAction {
        override fun getDescription(): String = "Set cursor position to $position"

        override fun getConstraints(): Matcher<View> =
            isAssignableFrom(ThousandSeparatedEditText::class.java)

        override fun perform(uiController: androidx.test.espresso.UiController?, view: View?) {
            val thousandSeparatedEditText = view as ThousandSeparatedEditText
            thousandSeparatedEditText.setSelection(position)
        }
    })
}

fun clickChipCloseIcon(): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(Chip::class.java)
        }

        override fun getDescription(): String {
            return "Click on Chip close icon"
        }

        override fun perform(uiController: androidx.test.espresso.UiController?, view: View?) {
            val chip = view as Chip
            chip.performCloseIconClick()
        }
    }
}

fun clickChildViewWithId(id: Int): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(View::class.java)
        }

        override fun getDescription(): String {
            return "Click on a child view with specified id."
        }

        override fun perform(uiController: androidx.test.espresso.UiController?, view: View?) {
            val childView = view?.findViewById<View>(id)
            childView?.performClick()
        }
    }
}