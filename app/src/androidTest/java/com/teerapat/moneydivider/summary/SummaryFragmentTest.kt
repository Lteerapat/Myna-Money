package com.teerapat.moneydivider.summary

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.teerapat.moneydivider.MainActivity
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.addnamelist.NameListAdapter
import com.teerapat.moneydivider.util.withRecyclerView
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SummaryFragmentTest {
    @get: Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun navigateToAddFoodListFragment() {
        onView(withId(R.id.etNameList)).perform(typeText(NAME))
        onView(withId(R.id.btnAddNameList)).perform(click())
        onView(withId(R.id.rvNameList)).perform(
            actionOnItemAtPosition<NameListAdapter.NameListViewHolder>(
                1,
                typeText(NAME_2)
            )
        )
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.yes_btn)).perform(click())
    }

    @Test
    fun `navigate to summary fragment and show summary`() {
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.etFoodPrice)).perform(typeText(PRICE))
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(R.string.select_all)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())

        onView(withId(R.id.btnPercentageToggle)).perform(click())
        onView(withText(R.string.amount_message)).perform(click())

        onView(withId(R.id.etServiceChargeAmount)).perform(typeText(PRICE))
        onView(withId(R.id.etVatAmount)).perform(typeText(PRICE))
        onView(withId(R.id.etDiscountAmount)).perform(typeText(PRICE_2))

        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.yes_btn)).perform(click())

        onView(withId(R.id.tvSummaryTotalAmount)).check(matches(withText("฿ 290.00")))
        onView(withId(R.id.rvSummary)).check(matches(hasChildCount(2)))
        onView(withRecyclerView(0, R.id.tvName, R.id.rvSummary)).check(matches(withText(NAME)))
        onView(withRecyclerView(1, R.id.tvName, R.id.rvSummary)).check(matches(withText(NAME_2)))
        onView(withRecyclerView(0, R.id.tvTotalPayAmountPerPerson, R.id.rvSummary)).check(
            matches(
                withText("฿ 145.00")
            )
        )
        onView(withRecyclerView(1, R.id.tvTotalPayAmountPerPerson, R.id.rvSummary)).check(
            matches(
                withText("฿ 145.00")
            )
        )
    }

    companion object {
        private const val NAME = "John Doe"
        private const val NAME_2 = "Mee pooh"
        private const val FOOD = "Pizza"
        private const val FOOD_2 = "Steak"
        private const val FOOD_3 = "Water"
        private const val PRICE = "100"
        private const val PRICE_2 = "10"
        private const val MAX_PRICE = "9,999,999.99"
    }
}