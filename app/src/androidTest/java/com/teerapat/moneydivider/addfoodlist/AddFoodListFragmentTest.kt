package com.teerapat.moneydivider.addfoodlist

import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.teerapat.moneydivider.MainActivity
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.addnamelist.NameListAdapter
import com.teerapat.moneydivider.util.clickChildViewWithId
import com.teerapat.moneydivider.util.clickChipCloseIcon
import com.teerapat.moneydivider.util.setCursorPosition
import com.teerapat.moneydivider.util.withBackgroundColor
import com.teerapat.moneydivider.util.withBackgroundTintColor
import com.teerapat.moneydivider.util.withRecyclerView
import junit.framework.TestCase.assertEquals
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddFoodListFragmentTest {
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

    // recyclerView
    @Test
    fun `AddFoodListFragment should already has 1 item existed in the recyclerView`() {
        onView(withId(R.id.rvFoodList)).check(matches(hasChildCount(1)))
    }

    @Test
    fun `if btnAddFoodList is clicked it should add new item to the recyclerView`() {
        onView(withId(R.id.btnAddFoodList)).perform(click())
        onView(withId(R.id.rvFoodList)).check(
            matches(hasChildCount(2))
        )
        onView(withId(R.id.rvFoodList)).check(
            matches(
                hasDescendant(withId(R.id.etFoodList))
            )
        )
    }

    @Test
    fun `if new item is added to the recyclerView it should focus etFoodList of that item`() {
        onView(withId(R.id.btnAddFoodList)).perform(click())
        onView(withId(R.id.rvFoodList)).check { view, _ ->
            val recyclerView = view as RecyclerView
            val newlyAddedItem = recyclerView.findViewHolderForAdapterPosition(1)?.itemView
            val etFoodList = newlyAddedItem?.findViewById<EditText>(R.id.etFoodList)
            assertEquals(etFoodList?.hasFocus(), true)
        }
    }

    @Test
    fun `if adding the item until it hits the limit then it should trigger showAlertOverLimitItemCard and after click ok it should not add more item`() {
        for (i in 0 until MAX_FOOD_CARD) {
            onView(withId(R.id.btnAddFoodList)).perform(click())
        }
        onView(withText(R.string.item_limit_exceeded_title)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.rvFoodList)).check { view, _ ->
            val recyclerView = view as RecyclerView
            val adapterItemCount = recyclerView.adapter?.itemCount ?: 0
            assertEquals(adapterItemCount, MAX_FOOD_CARD)
        }
    }

    // etFoodList
    @Test
    fun `etFoodList should be able to perform editing the text`() {
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.etFoodList)).check(matches(withText(FOOD)))
    }

    @Test
    fun `etFoodList should limit the text to 25 char`() {
        onView(withId(R.id.etFoodList)).perform(typeText("12345678912345678912345678"))
        onView(withId(R.id.etFoodList)).check(matches(withText("1234567891234567891234567")))
    }

    // thousandSeparatorEditText
    @Test
    fun `thousandSeparatorEditText should be able to perform editing the number`() {
        onView(withId(R.id.etFoodPrice)).perform(typeText(PRICE))
        onView(withId(R.id.etFoodPrice)).check(matches(withText(PRICE)))
    }

    @Test
    fun `thousandSeparatorEditText should add zero before the decimal place if dot is the first char`() {
        onView(withId(R.id.etFoodPrice)).perform(typeText("."))
        onView(withId(R.id.etFoodPrice)).check(matches(withText("0.")))
    }

    @Test
    fun `thousandSeparatorEditText should not has zero leading`() {
        onView(withId(R.id.etFoodPrice)).perform(typeText("010"))
        onView(withId(R.id.etFoodPrice)).check(matches(withText(PRICE)))
        onView(withId(R.id.etFoodPrice)).perform(setCursorPosition(0))
        onView(withId(R.id.etFoodPrice)).perform(typeText("0"))
        onView(withId(R.id.etFoodPrice)).check(matches(withText(PRICE)))
    }

    @Test
    fun `when editing thousandSeparatorEditText at n digit and if the number is not hit the limit it should not replace the number of that digit but add more digit instead`() {
        onView(withId(R.id.etFoodPrice)).perform(typeText("134"))
        onView(withId(R.id.etFoodPrice)).perform(setCursorPosition(1))
        onView(withId(R.id.etFoodPrice)).perform(typeText("2"))
        onView(withId(R.id.etFoodPrice)).check(matches(withText("1,234")))
    }

    @Test
    fun `when editing thousandSeparatorEditText at n digit and if the number already hit the limit it should not replace the number of that digit or adding more digit`() {
        onView(withId(R.id.etFoodPrice)).perform(typeText("9999999.99"))
        onView(withId(R.id.etFoodPrice)).perform(setCursorPosition(0))
        onView(withId(R.id.etFoodPrice)).perform(typeText("2"))
        onView(withId(R.id.etFoodPrice)).check(matches(withText(MAX_PRICE)))
    }

    @Test
    fun `thousandSeparatorEditText should add thousand separator to the edited number`() {
        onView(withId(R.id.etFoodPrice)).perform(typeText("9999999.99"))
        onView(withId(R.id.etFoodPrice)).check(matches(withText(MAX_PRICE)))
    }

    @Test
    fun `thousandSeparatorEditText should limit the digit before decimal to 7 and after decimal to 2`() {
        onView(withId(R.id.etFoodPrice)).perform(typeText("99999999.999"))
        onView(withId(R.id.etFoodPrice)).check(matches(withText(MAX_PRICE)))
    }

    // nameChip
    @Test
    fun `showNameSelectionDialog should appear after click on ivAddNameList`() {
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(R.string.add_name_for_food_list_card_title)).check(matches(isDisplayed()))
    }

    @Test
    fun `showNameSelectionDialog should has the same list of name as the name list from AddNameListFragment`() {
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(NAME)).check(matches(isDisplayed()))
        onView(withText(NAME_2)).check(matches(isDisplayed()))
    }

    @Test
    fun `after checked the name on showNameSelectionDialog and click ok it should add the checked name as a chip to nameChipContainer`() {
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(NAME)).perform(click())
        onView(withText(NAME)).check(matches(isChecked()))
        onView(withText(NAME_2)).perform(click())
        onView(withText(NAME_2)).check(matches(isChecked()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.nameChipContainer)).check(matches(hasChildCount(2)))
        onView(withId(R.id.nameChipContainer)).check(matches(hasDescendant(withText(NAME))))
        onView(withId(R.id.nameChipContainer)).check(matches(hasDescendant(withText(NAME_2))))
    }

    @Test
    fun `click close icon on chip it should remove that chip from nameChipContainer and unchecked that deleted chip on showNameSelectionDialog`() {
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(NAME)).perform(click())
        onView(withText(NAME_2)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withText(NAME)).perform(clickChipCloseIcon())
        onView(withId(R.id.nameChipContainer)).check(matches(hasChildCount(1)))
        onView(withId(R.id.nameChipContainer)).check(matches(not(hasDescendant(withText(NAME)))))
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(NAME)).check(matches(not(isChecked())))
    }

    @Test
    fun `click select all should checked all name on showNameSelectionDialog and after click ok it should add all name as a chip to nameChipContainer`() {
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(R.string.select_all)).perform(click())
        onView(withText(NAME)).check(matches(isChecked()))
        onView(withText(NAME_2)).check(matches(isChecked()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.nameChipContainer)).check(matches(hasChildCount(2)))
        onView(withId(R.id.nameChipContainer)).check(matches(hasDescendant(withText(NAME))))
        onView(withId(R.id.nameChipContainer)).check(matches(hasDescendant(withText(NAME_2))))
    }

    @Test
    fun `after unchecked the checked name and click ok and open showNameSelectionDialog the checked and unchecked should remain the same`() {
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(NAME_2)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(NAME)).check(matches(not(isChecked())))
        onView(withText(NAME_2)).check(matches(isChecked()))
        onView(withText(NAME)).perform(click())
        onView(withText(NAME_2)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(NAME)).check(matches(isChecked()))
        onView(withText(NAME_2)).check(matches(not(isChecked())))
    }

    @Test
    fun `after select all name on showNameSelectionDialog and uncheck some of checked name and click ok it should not add that unchecked name as a chip`() {
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(R.string.select_all)).perform(click())
        onView(withText(NAME)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.nameChipContainer)).check(matches(hasChildCount(1)))
        onView(withId(R.id.nameChipContainer)).check(matches(not(hasDescendant(withText(NAME)))))
        onView(withId(R.id.nameChipContainer)).check(matches(hasDescendant(withText(NAME_2))))
    }

    @Test
    fun `after added name as a chip and open showNameSelectionDialog again and unchecked the checked name and click ok it should remove that unchecked name from the nameChipContainer`() {
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(R.string.select_all)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(NAME)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.nameChipContainer)).check(matches(hasChildCount(1)))
        onView(withId(R.id.nameChipContainer)).check(matches(not(hasDescendant(withText(NAME)))))
        onView(withId(R.id.nameChipContainer)).check(matches(hasDescendant(withText(NAME_2))))
    }

    @Test
    fun `after add chip to nameChipContainer and the go back to AddNameListFragment to remove some name and come back to AddFoodListFragment again it should remove that removed name from nameChipContainer and showDeleteItemConfirmationDialog`() {
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(R.string.select_all)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        pressBack()
        onView(withId(R.id.rvNameList)).perform(
            actionOnItemAtPosition<NameListAdapter.NameListViewHolder>(
                0,
                clickChildViewWithId(R.id.ivDeleteNameList)
            )
        )
        onView(withText(R.string.yes_btn))
            .inRoot(isDialog())
            .perform(click())
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.yes_btn)).perform(click())
        onView(withId(R.id.nameChipContainer)).check(matches(hasChildCount(1)))
        onView(withId(R.id.nameChipContainer)).check(matches(not(hasDescendant(withText(NAME)))))
        onView(withId(R.id.nameChipContainer)).check(matches(hasDescendant(withText(NAME_2))))
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(NAME)).check(doesNotExist())
        onView(withText(NAME_2)).check(matches(isChecked()))
    }


    // tvPersonPerFoodCard
    @Test
    fun `tvPersonPerFoodCard should not visible if no chip on nameChipContainer`() {
        onView(withId(R.id.tvPersonPerFoodCard)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `tvPersonPerFoodCard should update the count if the chip is added to nameChipContainer`() {
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(R.string.select_all)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.tvPersonPerFoodCard)).check(matches(isDisplayed()))
        onView(withId(R.id.tvPersonPerFoodCard)).check(matches(withText("/2")))
    }

    @Test
    fun `tvPersonPerFoodCard should update the count if the chip is removed from nameChipContainer`() {
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(R.string.select_all)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withText(NAME)).perform(clickChipCloseIcon())
        onView(withId(R.id.tvPersonPerFoodCard)).check(matches(isDisplayed()))
        onView(withId(R.id.tvPersonPerFoodCard)).check(matches(withText("/1")))
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(NAME_2)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.tvPersonPerFoodCard)).check(matches(not(isDisplayed())))
    }

    // delete
    @Test
    fun `after click delete button and if item is empty then it should delete that item`() {
        onView(withId(R.id.ivDeleteFoodList)).perform(click())
        onView(withId(R.id.rvFoodList)).check(matches(hasChildCount(0)))
    }

    @Test
    fun `after click delete button and if item contain at least 1 field it should trigger showDeleteItemConfirmationDialog`() {
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.ivDeleteFoodList)).perform(click())
        onView(withText(R.string.confirm_delete_message)).check(matches(isDisplayed()))
        onView(withText(R.string.no_btn)).perform(click())

        onView(withId(R.id.etFoodList)).perform(clearText())
        onView(withId(R.id.etFoodPrice)).perform(typeText(PRICE))
        onView(withId(R.id.ivDeleteFoodList)).perform(click())
        onView(withText(R.string.confirm_delete_message)).check(matches(isDisplayed()))
        onView(withText(R.string.no_btn)).perform(click())

        onView(withId(R.id.etFoodPrice)).perform(clearText())
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(R.string.select_all)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.ivDeleteFoodList)).perform(click())
        onView(withText(R.string.confirm_delete_message)).check(matches(isDisplayed()))
    }

    @Test
    fun `after click no on showDeleteItemConfirmationDialog it should not delete that item`() {
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.ivDeleteFoodList)).perform(click())
        onView(withText(R.string.confirm_delete_message)).check(matches(isDisplayed()))
        onView(withText(R.string.no_btn)).perform(click())
        onView(withId(R.id.rvFoodList)).check(matches(hasChildCount(1)))
        onView(withId(R.id.etFoodList)).check(matches(isDisplayed()))
    }

    @Test
    fun `after click yes on showDeleteItemConfirmationDialog it should delete that item`() {
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.ivDeleteFoodList)).perform(click())
        onView(withText(R.string.confirm_delete_message)).check(matches(isDisplayed()))
        onView(withText(R.string.yes_btn)).perform(click())
        onView(withId(R.id.rvFoodList)).check(matches(hasChildCount(0)))
    }

    // vScDis
    @Test
    fun `AddFoodListFragment should set default vScDis on percentage`() {
        onView(withId(R.id.tvServiceChargePercentage)).check(matches(withText(R.string.percentage_sign)))
        onView(withId(R.id.tvVatPercentage)).check(matches(withText(R.string.percentage_sign)))
        onView(withId(R.id.tvDiscountPercentage)).check(matches(withText(R.string.percentage_sign)))
    }

    @Test
    fun `after click btnPercentageToggle it should trigger showTogglePercentageAmountDialog`() {
        onView(withId(R.id.btnPercentageToggle)).perform(click())
        onView(withText(R.string.percentage_or_amount_toggle_title)).check(matches(isDisplayed()))
    }

    @Test
    fun `after click amount on showTogglePercentageAmountDialog it should set vScDis to amount`() {
        onView(withId(R.id.btnPercentageToggle)).perform(click())
        onView(withText(R.string.amount_message)).perform(click())
        onView(withId(R.id.tvServiceChargePercentage)).check(matches(withText(R.string.baht_sign)))
        onView(withId(R.id.tvVatPercentage)).check(matches(withText(R.string.baht_sign)))
        onView(withId(R.id.tvDiscountPercentage)).check(matches(withText(R.string.baht_sign)))
        onView(withId(R.id.btnPercentageToggle)).perform(click())
        onView(withText(R.string.amount_message)).perform(click())
        onView(withId(R.id.tvServiceChargePercentage)).check(matches(withText(R.string.baht_sign)))
        onView(withId(R.id.tvVatPercentage)).check(matches(withText(R.string.baht_sign)))
        onView(withId(R.id.tvDiscountPercentage)).check(matches(withText(R.string.baht_sign)))
    }

    @Test
    fun `after click percentage on showTogglePercentageAmountDialog it should set vScDis to percentage`() {
        onView(withId(R.id.btnPercentageToggle)).perform(click())
        onView(withText(R.string.percentage_message)).perform(click())
        onView(withId(R.id.tvServiceChargePercentage)).check(matches(withText(R.string.percentage_sign)))
        onView(withId(R.id.tvVatPercentage)).check(matches(withText(R.string.percentage_sign)))
        onView(withId(R.id.tvDiscountPercentage)).check(matches(withText(R.string.percentage_sign)))
        onView(withId(R.id.btnPercentageToggle)).perform(click())
        onView(withText(R.string.percentage_message)).perform(click())
        onView(withId(R.id.tvServiceChargePercentage)).check(matches(withText(R.string.percentage_sign)))
        onView(withId(R.id.tvVatPercentage)).check(matches(withText(R.string.percentage_sign)))
        onView(withId(R.id.tvDiscountPercentage)).check(matches(withText(R.string.percentage_sign)))
    }

    @Test
    fun `after swap from percentage to amount at vScDis field it should clear the input if it is filled`() {
        onView(withId(R.id.etServiceChargeAmount)).perform(typeText(PRICE))
        onView(withId(R.id.etVatAmount)).perform(typeText(PRICE))
        onView(withId(R.id.etDiscountAmount)).perform(typeText(PRICE))
        onView(withId(R.id.btnPercentageToggle)).perform(click())
        onView(withText(R.string.amount_message)).perform(click())
        onView(withId(R.id.etServiceChargeAmount)).check(matches(withText("")))
        onView(withId(R.id.etVatAmount)).check(matches(withText("")))
        onView(withId(R.id.etDiscountAmount)).check(matches(withText("")))
    }

    @Test
    fun `after swap from amount to percentage at vScDis field it should clear the input if it is filled`() {
        onView(withId(R.id.etFoodPrice)).perform(typeText(PRICE))
        onView(withId(R.id.btnPercentageToggle)).perform(click())
        onView(withText(R.string.amount_message)).perform(click())
        onView(withId(R.id.etServiceChargeAmount)).perform(typeText(PRICE))
        onView(withId(R.id.etVatAmount)).perform(typeText(PRICE))
        onView(withId(R.id.etDiscountAmount)).perform(typeText(PRICE))
        onView(withId(R.id.btnPercentageToggle)).perform(click())
        onView(withText(R.string.percentage_message)).perform(click())
        onView(withId(R.id.etServiceChargeAmount)).check(matches(withText("")))
        onView(withId(R.id.etVatAmount)).check(matches(withText("")))
        onView(withId(R.id.etDiscountAmount)).check(matches(withText("")))
    }

    @Test
    fun `after swap from amount to amount at vScDis field it should not clear the input if it is filled`() {
        onView(withId(R.id.etFoodPrice)).perform(typeText(PRICE))
        onView(withId(R.id.btnPercentageToggle)).perform(click())
        onView(withText(R.string.amount_message)).perform(click())
        onView(withId(R.id.etServiceChargeAmount)).perform(typeText(PRICE))
        onView(withId(R.id.etVatAmount)).perform(typeText(PRICE))
        onView(withId(R.id.etDiscountAmount)).perform(typeText(PRICE))
        onView(withId(R.id.btnPercentageToggle)).perform(click())
        onView(withText(R.string.amount_message)).perform(click())
        onView(withId(R.id.etServiceChargeAmount)).check(matches(withText(PRICE)))
        onView(withId(R.id.etVatAmount)).check(matches(withText(PRICE)))
        onView(withId(R.id.etDiscountAmount)).check(matches(withText(PRICE)))
    }

    @Test
    fun `after swap from percentage to percentage at vScDis field it should not clear the input if it is filled`() {
        onView(withId(R.id.etServiceChargeAmount)).perform(typeText(PRICE))
        onView(withId(R.id.etVatAmount)).perform(typeText(PRICE))
        onView(withId(R.id.etDiscountAmount)).perform(typeText(PRICE))
        onView(withId(R.id.btnPercentageToggle)).perform(click())
        onView(withText(R.string.percentage_message)).perform(click())
        onView(withId(R.id.etServiceChargeAmount)).check(matches(withText(PRICE)))
        onView(withId(R.id.etVatAmount)).check(matches(withText(PRICE)))
        onView(withId(R.id.etDiscountAmount)).check(matches(withText(PRICE)))
    }

    @Test
    fun `percentage of vScDis field can not exceed 100 and should clear the input if exceed 100`() {
        onView(withId(R.id.etServiceChargeAmount)).perform(typeText(MAX_PRICE))
        onView(withText(R.string.percentage_exceeded_alert_message)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etServiceChargeAmount)).check(matches(withText("")))
        onView(withId(R.id.etVatAmount)).perform(typeText(MAX_PRICE))
        onView(withText(R.string.percentage_exceeded_alert_message)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etVatAmount)).check(matches(withText("")))
        onView(withId(R.id.etDiscountAmount)).perform(typeText(MAX_PRICE))
        onView(withText(R.string.percentage_exceeded_alert_message)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etDiscountAmount)).check(matches(withText("")))
    }

    // next
    @Test
    fun `when click next button but no item is existed in the recyclerView it should trigger showAlertZeroCardList then add new item and focus etFoodList of that item after click ok then clear red tint if etFoodList is filled`() {
        onView(withId(R.id.ivDeleteFoodList)).perform(click())
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.incomplete_card_at_least_1_message)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etFoodList)).check(matches(isDisplayed()))
        onView(withId(R.id.etFoodList)).check(matches(hasFocus()))
        onView(withId(R.id.etFoodList)).check(matches(withBackgroundTintColor(R.color.red)))
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.etFoodList)).check(matches(withBackgroundTintColor(R.color.teal_700)))
    }

    @Test
    fun `when click next button but etFoodList is not filled it should trigger showAlertOnIncompleteCard then focus etFoodList of that item after click ok then clear red tint if etFoodList is filled`() {
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.incomplete_card_empty_message)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etFoodList)).check(matches(hasFocus()))
        onView(withId(R.id.etFoodList)).check(matches(withBackgroundTintColor(R.color.red)))
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.etFoodList)).check(matches(withBackgroundTintColor(R.color.teal_700)))
    }

    @Test
    fun `when click next button but etFoodPrice is not filled it should trigger showAlertOnIncompleteCard then focus etFoodPrice of that item after click ok then clear red tint if etFoodPrice is filled`() {
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.incomplete_card_empty_message)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etFoodPrice)).check(matches(hasFocus()))
        onView(withId(R.id.etFoodPrice)).check(matches(withBackgroundTintColor(R.color.red)))
        onView(withId(R.id.etFoodPrice)).perform(typeText(PRICE))
        onView(withId(R.id.etFoodPrice)).check(matches(withBackgroundTintColor(R.color.teal_700)))
    }

    @Test
    fun `when click next button but etFoodPrice is zero it should trigger showAlertOnIncompleteCard then focus etFoodPrice of that item and clear the text after click ok then clear red tint if etFoodPrice is filled`() {
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.etFoodPrice)).perform(typeText("0"))
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.incomplete_card_zero_message)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etFoodPrice)).check(matches(hasFocus()))
        onView(withId(R.id.etFoodPrice)).check(matches(withText("")))
        onView(withId(R.id.etFoodPrice)).check(matches(withBackgroundTintColor(R.color.red)))
        onView(withId(R.id.etFoodPrice)).perform(typeText(PRICE))
        onView(withId(R.id.etFoodPrice)).check(matches(withBackgroundTintColor(R.color.teal_700)))
    }

    @Test
    fun `when click next button but no name chip in nameChipContainer it should trigger showAlertOnIncompleteCard then change background color of that item to red border after click ok then clear red border after name chip is added`() {
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.etFoodPrice)).perform(typeText(PRICE))
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.incomplete_card_zero_chip_message)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.foodCardContainer)).check(matches(withBackgroundColor(R.drawable.incomplete_card_border)))
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(R.string.select_all)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.foodCardContainer)).check(matches(withBackgroundColor(R.drawable.rounded_corner_white_bg)))
    }

    @Test
    fun `when click next button but etFoodList is filled with empty space it should trigger showAlertOnIncompleteCard then focus etFoodList of that item and clear the text after click ok then clear red tint if etFoodList is filled`() {
        onView(withId(R.id.etFoodList)).perform(typeText("    "))
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.incomplete_card_empty_message)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etFoodList)).check(matches(hasFocus()))
        onView(withId(R.id.etFoodList)).check(matches(withText("")))
        onView(withId(R.id.etFoodList)).check(matches(withBackgroundTintColor(R.color.red)))
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.etFoodList)).check(matches(withBackgroundTintColor(R.color.teal_700)))
    }

    @Test
    fun `when click next button but etFoodList is filled with only number it should trigger showAlertOnIncompleteCard then focus etFoodList of that item and clear the text after click ok then clear red tint if etFoodList is filled`() {
        onView(withId(R.id.etFoodList)).perform(typeText(PRICE))
        onView(withId(R.id.etFoodPrice)).perform(typeText(PRICE))
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.incomplete_card_num_only_message)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etFoodList)).check(matches(hasFocus()))
        onView(withId(R.id.etFoodList)).check(matches(withText("")))
        onView(withId(R.id.etFoodList)).check(matches(withBackgroundTintColor(R.color.red)))
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.etFoodList)).check(matches(withBackgroundTintColor(R.color.teal_700)))
    }

    @Test
    fun `when click next button but etFoodList is not filled with number or letter it should trigger showAlertOnIncompleteCard then focus etFoodList of that item and clear the text after click ok then clear red tint if etFoodList is filled`() {
        onView(withId(R.id.etFoodList)).perform(typeText("."))
        onView(withId(R.id.etFoodPrice)).perform(typeText(PRICE))
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.incomplete_card_letter_or_num_message)).check(matches(isDisplayed()))
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etFoodList)).check(matches(hasFocus()))
        onView(withId(R.id.etFoodList)).check(matches(withText("")))
        onView(withId(R.id.etFoodList)).check(matches(withBackgroundTintColor(R.color.red)))
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.etFoodList)).check(matches(withBackgroundTintColor(R.color.teal_700)))
    }

    @Test
    fun `if everything is complete and click next button it should trigger showContinueDialog then click no it should close showContinueDialog`() {
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.etFoodPrice)).perform(typeText(PRICE))
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(R.string.select_all)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.next_btn_alert_title)).check(matches(isDisplayed()))
        onView(withText(R.string.no_btn)).perform(click())
        onView(withText(R.string.next_btn_alert_title)).check(doesNotExist())
    }

    @Test
    fun `if everything is complete with vScDis as percentage and click next button it should trigger showContinueDialog then click yes it should navigate to SummaryFragment`() {
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.etFoodPrice)).perform(typeText(PRICE))
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(R.string.select_all)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.etServiceChargeAmount)).perform(typeText(PRICE))
        onView(withId(R.id.etVatAmount)).perform(typeText(PRICE))
        onView(withId(R.id.etDiscountAmount)).perform(typeText(PRICE))
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.next_btn_alert_title)).check(matches(isDisplayed()))
        onView(withText(R.string.yes_btn)).perform(click())
        onView(withId(R.id.summaryHeader)).check(matches(isDisplayed()))
    }

    @Test
    fun `if everything is complete with vScDis as amount and click next button it should trigger showContinueDialog then click yes it should navigate to SummaryFragment`() {
        onView(withId(R.id.etFoodList)).perform(typeText(FOOD))
        onView(withId(R.id.etFoodPrice)).perform(typeText(PRICE))
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(R.string.select_all)).perform(click())
        onView(withText(R.string.ok_btn)).perform(click())
        onView(withId(R.id.btnPercentageToggle)).perform(click())
        onView(withText(R.string.amount_message)).perform(click())
        onView(withId(R.id.etServiceChargeAmount)).perform(typeText(PRICE))
        onView(withId(R.id.etVatAmount)).perform(typeText(PRICE))
        onView(withId(R.id.etDiscountAmount)).perform(typeText(PRICE))
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText(R.string.next_btn_alert_title)).check(matches(isDisplayed()))
        onView(withText(R.string.yes_btn)).perform(click())
        onView(withId(R.id.summaryHeader)).check(matches(isDisplayed()))
    }

    @Test
    fun `navigate back from addFoodListFragment should not delete the added items case vScDis as percentage`() {
        `if everything is complete with vScDis as percentage and click next button it should trigger showContinueDialog then click yes it should navigate to SummaryFragment`()
        pressBack()
        onView(withId(R.id.rvFoodList)).check(matches(isDisplayed()))
        onView(withId(R.id.etFoodList)).check(matches(withText(FOOD)))
        onView(withId(R.id.etFoodPrice)).check(matches(withText(PRICE)))
        onView(withId(R.id.tvPersonPerFoodCard)).check(matches(withText("/2")))
        onView(withId(R.id.nameChipContainer)).check(matches(hasChildCount(2)))
        onView(withId(R.id.nameChipContainer)).check(matches(hasDescendant(withText(NAME))))
        onView(withId(R.id.nameChipContainer)).check(matches(hasDescendant(withText(NAME_2))))
        onView(withId(R.id.etServiceChargeAmount)).check(matches(withText(PRICE)))
        onView(withId(R.id.etVatAmount)).check(matches(withText(PRICE)))
        onView(withId(R.id.etDiscountAmount)).check(matches(withText(PRICE)))
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(NAME)).check(matches(isChecked()))
        onView(withText(NAME_2)).check(matches(isChecked()))
    }

    @Test
    fun `navigate back from addFoodListFragment should not delete the added items case vScDis as amount`() {
        `if everything is complete with vScDis as amount and click next button it should trigger showContinueDialog then click yes it should navigate to SummaryFragment`()
        pressBack()
        onView(withId(R.id.rvFoodList)).check(matches(isDisplayed()))
        onView(withId(R.id.rvFoodList)).check(matches(hasChildCount(1)))
        onView(withId(R.id.etFoodList)).check(matches(withText(FOOD)))
        onView(withId(R.id.etFoodPrice)).check(matches(withText(PRICE)))
        onView(withId(R.id.tvPersonPerFoodCard)).check(matches(withText("/2")))
        onView(withId(R.id.nameChipContainer)).check(matches(hasChildCount(2)))
        onView(withId(R.id.nameChipContainer)).check(matches(hasDescendant(withText(NAME))))
        onView(withId(R.id.nameChipContainer)).check(matches(hasDescendant(withText(NAME_2))))
        onView(withId(R.id.etServiceChargeAmount)).check(matches(withText(PRICE)))
        onView(withId(R.id.etVatAmount)).check(matches(withText(PRICE)))
        onView(withId(R.id.etDiscountAmount)).check(matches(withText(PRICE)))
        onView(withId(R.id.tvServiceChargePercentage)).check(matches(withText(R.string.baht_sign)))
        onView(withId(R.id.tvVatPercentage)).check(matches(withText(R.string.baht_sign)))
        onView(withId(R.id.tvDiscountPercentage)).check(matches(withText(R.string.baht_sign)))
        onView(withId(R.id.ivAddNameList)).perform(click())
        onView(withText(NAME)).check(matches(isChecked()))
        onView(withText(NAME_2)).check(matches(isChecked()))
    }

    // total amount
    @Test
    fun `total amount should update and show correctly with percentage for vScDis`() {
        onView(withId(R.id.etFoodPrice)).perform(typeText("100"))
        onView(withId(R.id.btnAddFoodList)).perform(click())
        onView(withRecyclerView(1, R.id.etFoodPrice, R.id.rvFoodList))
            .perform(typeText("100"))
        onView(withId(R.id.tvTotalAmount)).check(matches(withText("200.00 ฿")))

        onView(withId(R.id.etServiceChargeAmount)).perform(typeText(PRICE))
        onView(withId(R.id.tvTotalAmount)).check(matches(withText("220.00 ฿")))

        onView(withId(R.id.etVatAmount)).perform(typeText("7"))
        onView(withId(R.id.tvTotalAmount)).check(matches(withText("235.40 ฿")))

        onView(withId(R.id.etDiscountAmount)).perform(typeText(PRICE))
        onView(withId(R.id.tvTotalAmount)).check(matches(withText("211.86 ฿")))
    }

    @Test
    fun `total amount should update and show correctly with amount for vScDis`() {
        onView(withId(R.id.etFoodPrice)).perform(typeText("100"))
        onView(withId(R.id.btnAddFoodList)).perform(click())
        onView(withRecyclerView(1, R.id.etFoodPrice, R.id.rvFoodList))
            .perform(typeText("100"))
        onView(withId(R.id.tvTotalAmount)).check(matches(withText("200.00 ฿")))

        onView(withId(R.id.btnPercentageToggle)).perform(click())
        onView(withText(R.string.amount_message)).perform(click())

        onView(withId(R.id.etServiceChargeAmount)).perform(typeText(PRICE))
        onView(withId(R.id.tvTotalAmount)).check(matches(withText("210.00 ฿")))

        onView(withId(R.id.etVatAmount)).perform(typeText("7"))
        onView(withId(R.id.tvTotalAmount)).check(matches(withText("217.00 ฿")))

        onView(withId(R.id.etDiscountAmount)).perform(typeText(PRICE))
        onView(withId(R.id.tvTotalAmount)).check(matches(withText("207.00 ฿")))
    }

    companion object {
        private const val NAME = "John Doe"
        private const val NAME_2 = "Mee pooh"
        private const val FOOD = "Pizza"
        private const val PRICE = "10"
        private const val MAX_PRICE = "9,999,999.99"
        private const val MAX_FOOD_CARD = 20
    }
}