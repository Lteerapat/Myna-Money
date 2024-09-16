package com.teerapat.moneydivider.addnamelist

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.teerapat.moneydivider.R
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class AddNameListFragmentTest {
    private lateinit var scenario: FragmentScenario<AddNameListFragment>

    @Before
    fun setUp() {
        scenario = launchFragmentInContainer()
        scenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun `name list fragment should already has 1 editText exist in the recycler view`() {
        onView(withId(R.id.rvNameList)).check(matches(hasChildCount(1)))
    }

    @Test
    fun `etNameList should be able to perform editing the text`() {
        onView(withId(R.id.etNameList)).perform(typeText("John Doe"))
        onView(withId(R.id.etNameList)).check(matches(withText("John Doe")))
    }

    @Test
    fun `if add name button is clicked it should add new editText to the recycler view`() {
        onView(withId(R.id.btnAddNameList)).perform(click())
        onView(withId(R.id.rvNameList)).check(matches(hasChildCount(2)))
        onView(withId(R.id.rvNameList)).check(
            matches(
                hasDescendant(withId(R.id.etNameList))
            )
        )
    }

    @Test
    fun `if new editText is added to the recyclerView it should focus that editText`() {
        onView(withId(R.id.btnAddNameList)).perform(click())
        onView(withId(R.id.rvNameList)).check { view, _ ->
            val recyclerView = view as RecyclerView
            val newlyAddedEtNameList = recyclerView.findViewHolderForAdapterPosition(1)?.itemView
            assertThat(newlyAddedEtNameList?.hasFocus(), `is`(true))
        }
    }

    @Test
    fun `when click next button but no editText is filled it should trigger showAlertOnIncompleteCard`() {
        onView(withId(R.id.btnNext)).perform(click())
    }
}