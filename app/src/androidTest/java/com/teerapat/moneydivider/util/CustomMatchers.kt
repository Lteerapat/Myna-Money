package com.teerapat.moneydivider.util

import android.content.res.ColorStateList
import android.view.View
import android.widget.EditText
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun withBackgroundTintColor(expectedColorResId: Int): Matcher<View> {
    return object : BoundedMatcher<View, EditText>(EditText::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with background tint color: ")
        }

        override fun matchesSafely(editText: EditText): Boolean {
            return editText.backgroundTintList == ColorStateList.valueOf(
                ContextCompat.getColor(editText.context, expectedColorResId)
            )
        }
    }
}

fun withBackgroundColor(expectedBackgroundColorResId: Int): Matcher<View> {
    return object : BoundedMatcher<View, CardView>(CardView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with background color resource ID: $expectedBackgroundColorResId")
        }

        override fun matchesSafely(cardView: CardView): Boolean {
            val expectedDrawable =
                ContextCompat.getDrawable(cardView.context, expectedBackgroundColorResId)
            val actualDrawable = cardView.background

            return expectedDrawable != null && actualDrawable != null &&
                    expectedDrawable.constantState == actualDrawable.constantState
        }
    }
}

fun withRecyclerView(position: Int, targetViewId: Int, recyclerViewId: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("RecyclerView with id: $recyclerViewId at position: $position")
        }

        public override fun matchesSafely(view: View): Boolean {
            val recyclerView = view.rootView.findViewById<RecyclerView>(recyclerViewId)
            val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)
            val targetView = viewHolder?.itemView?.findViewById<View>(targetViewId)
            return view == targetView
        }
    }
}
