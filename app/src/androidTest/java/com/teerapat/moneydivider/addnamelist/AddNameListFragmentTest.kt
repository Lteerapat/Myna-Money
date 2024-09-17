package com.teerapat.moneydivider.addnamelist

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.teerapat.moneydivider.MainActivity
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.util.withBackgroundTintColor
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test

class AddNameListFragmentTest {
    @get: Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun `addNameListFragment should already has 1 editText existed in the recyclerView`() {
        onView(withId(R.id.rvNameList)).check(matches(hasChildCount(1)))
    }

    @Test
    fun `etNameList should be able to perform editing the text`() {
        onView(withId(R.id.etNameList)).perform(typeText(name))
        onView(withId(R.id.etNameList)).check(matches(withText(name)))
    }

    @Test
    fun `if btnAddNameList is clicked it should add new editText to the recyclerView`() {
        onView(withId(R.id.btnAddNameList)).perform(click())
        onView(withId(R.id.rvNameList)).check(
            matches(hasChildCount(2))
        )
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
            assertEquals(newlyAddedEtNameList?.hasFocus(), true)
        }
    }

    @Test
    fun `when click next button but no editText is existed in the recyclerView it should trigger showAlertZeroCardList then add new editText and focus that editText after click ok then clear red tint if editText is filled`() {
        onView(withId(R.id.ivDeleteNameList)).perform(click())
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.incomplete_card_at_least_1_message)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etNameList)).check(matches(isDisplayed()))
        onView(withId(R.id.etNameList)).check(matches(hasFocus()))
        onView(withId(R.id.etNameList)).check(matches(withBackgroundTintColor(R.color.red)))
        onView(withId(R.id.etNameList)).perform(typeText(name))
        onView(withId(R.id.etNameList)).check(matches(withBackgroundTintColor(R.color.teal_700)))
    }

    @Test
    fun `when click next button but no editText is filled it should trigger showAlertOnIncompleteCard then focus that editText after click ok then clear red tint if editText is filled`() {
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.incomplete_empty_card_message_2)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etNameList)).check(matches(hasFocus()))
        onView(withId(R.id.etNameList)).check(matches(withBackgroundTintColor(R.color.red)))
        onView(withId(R.id.etNameList)).perform(typeText(name))
        onView(withId(R.id.etNameList)).check(matches(withBackgroundTintColor(R.color.teal_700)))
    }

    @Test
    fun `when click next button but no editText is filled with empty space from space bar it should trigger showAlertOnIncompleteCard then focus that editText and clear the text after click ok then clear red tint if editText is filled`() {
        onView(withId(R.id.etNameList)).perform(typeText("    "))
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.incomplete_empty_card_message_2)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etNameList)).check(matches(hasFocus()))
        onView(withId(R.id.etNameList)).check(matches(withText("")))
        onView(withId(R.id.etNameList)).check(matches(withBackgroundTintColor(R.color.red)))
        onView(withId(R.id.etNameList)).perform(typeText(name))
        onView(withId(R.id.etNameList)).check(matches(withBackgroundTintColor(R.color.teal_700)))
    }

    @Test
    fun `when click next button but editText is filled with only number it should trigger showAlertOnIncompleteCard then focus that editText and clear the text after click ok then clear red tint if editText is filled`() {
        onView(withId(R.id.etNameList)).perform(typeText("9"))
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.incomplete_card_num_only_message_2)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etNameList)).check(matches(hasFocus()))
        onView(withId(R.id.etNameList)).check(matches(withText("")))
        onView(withId(R.id.etNameList)).check(matches(withBackgroundTintColor(R.color.red)))
        onView(withId(R.id.etNameList)).perform(typeText(name))
        onView(withId(R.id.etNameList)).check(matches(withBackgroundTintColor(R.color.teal_700)))
    }

    @Test
    fun `when click next button but editText is not filled with number or letter it should trigger showAlertOnIncompleteCard then focus that editText and clear the text after click ok then clear red tint if editText is filled`() {
        onView(withId(R.id.etNameList)).perform(typeText("."))
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.incomplete_letter_or_num_message_2)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etNameList)).check(matches(hasFocus()))
        onView(withId(R.id.etNameList)).check(matches(withText("")))
        onView(withId(R.id.etNameList)).check(matches(withBackgroundTintColor(R.color.red)))
        onView(withId(R.id.etNameList)).perform(typeText(name))
        onView(withId(R.id.etNameList)).check(matches(withBackgroundTintColor(R.color.teal_700)))
    }

    @Test
    fun `when click next button but duplicate name existed it should trigger showAlertDuplicateNames`() {
        onView(withId(R.id.etNameList)).perform(typeText(name))
        onView(withId(R.id.btnAddNameList)).perform(click())
        onView(withId(R.id.rvNameList)).perform(
            actionOnItemAtPosition<NameListAdapter.NameListViewHolder>(
                1,
                typeText(name)
            )
        )
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.duplicate_name_alert_message)).check(matches(isDisplayed()))
    }

    @Test
    fun `after click delete button and that editText is empty then it should delete that editText`() {
        onView(withId(R.id.ivDeleteNameList)).perform(click())
        onView(withId(R.id.rvNameList)).check(matches(hasChildCount(0)))
    }

    @Test
    fun `after click delete button and that editText has at least 1 char it should trigger showDeleteItemConfirmationDialog then click no it should not delete that editText`() {
        onView(withId(R.id.etNameList)).perform(typeText(name))
        onView(withId(R.id.ivDeleteNameList)).perform(click())
        onView(withText(R.string.confirm_delete_message)).check(matches(isDisplayed()))
        onView(withText(R.string.no_btn)).perform(click())
        onView(withId(R.id.etNameList)).check(matches(isDisplayed()))
    }

    @Test
    fun `after click delete button and that editText has at least 1 char it should trigger showDeleteItemConfirmationDialog then click ok it should delete that editText`() {
        onView(withId(R.id.etNameList)).perform(typeText(name))
        onView(withId(R.id.ivDeleteNameList)).perform(click())
        onView(withText(R.string.confirm_delete_message)).check(matches(isDisplayed()))
        onView(withText(R.string.yes_btn)).perform(click())
        onView(withId(R.id.rvNameList)).check(matches(hasChildCount(0)))
    }

    @Test
    fun `if adding the editText until it hits the limit then it should trigger showAlertOverLimitItemCard and after click ok it should not add more editText`() {
        for (i in 0 until MAX_NAME_CARD) {
            onView(withId(R.id.btnAddNameList)).perform(click())
        }
        onView(withText(R.string.item_limit_exceeded_title)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.rvNameList)).check { view, _ ->
            val recyclerView = view as RecyclerView
            val adapterItemCount = recyclerView.adapter?.itemCount ?: 0
            assertEquals(adapterItemCount, MAX_NAME_CARD)
        }
    }

    @Test
    fun `if everything is complete and click next button it should trigger showContinueDialog then click no it should close showContinueDialog`() {
        onView(withId(R.id.etNameList)).perform(typeText(name))
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.next_btn_alert_title)).check(matches(isDisplayed()))
        onView(withText(R.string.no_btn)).perform(click())
        onView(withId(R.id.etNameList)).check(matches(isDisplayed()))
    }

    @Test
    fun `if everything is complete and click next button it should trigger showContinueDialog then click yes it should navigate to addFoodListFragment`() {
        onView(withId(R.id.etNameList)).perform(typeText(name))
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.next_btn_alert_title)).check(matches(isDisplayed()))
        onView(withText(R.string.yes_btn)).perform(click())
        onView(withId(R.id.etFoodList)).check(matches(isDisplayed()))
    }

    @Test
    fun `navigate back from addFoodListFragment should not delete the added editText`() {
        `if everything is complete and click next button it should trigger showContinueDialog then click yes it should navigate to addFoodListFragment`()
        pressBack()
        onView(withId(R.id.etNameList)).check(matches(withText(name)))
    }

    companion object {
        private const val name = "John Doe"
        private const val MAX_NAME_CARD = 20
    }
}