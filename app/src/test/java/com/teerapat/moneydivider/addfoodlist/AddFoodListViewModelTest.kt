package com.teerapat.moneydivider.addfoodlist

import com.teerapat.moneydivider.data.FoodInfo
import com.teerapat.moneydivider.data.FoodNameInfo
import com.teerapat.moneydivider.data.FoodPriceInfo
import com.teerapat.moneydivider.data.NameChipInfo
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class AddFoodListViewModelTest {
    private lateinit var viewModel: AddFoodListViewModel

    @Before
    fun setup() {
        viewModel = AddFoodListViewModel()
    }

    @Test
    fun `saveFoodList should clear old list and add new list`() {
        val initialList = listOf(
            FoodInfo(FoodNameInfo(FOOD_1), FoodPriceInfo(), NameChipInfo()),
            FoodInfo(FoodNameInfo(FOOD_1), FoodPriceInfo(), NameChipInfo())
        )
        val newList = listOf(
            FoodInfo(FoodNameInfo(FOOD_2), FoodPriceInfo(), NameChipInfo()),
            FoodInfo(FoodNameInfo(FOOD_3), FoodPriceInfo(), NameChipInfo())
        )

        viewModel.saveFoodList(initialList)
        viewModel.saveFoodList(newList)

        assertEquals(2, viewModel.foodList.size)
        assertEquals(FOOD_2, viewModel.foodList[0].foodName.name)
        assertEquals(FOOD_3, viewModel.foodList[1].foodName.name)
    }

    @Test
    fun `saveFoodList should add all new items to empty list`() {
        val newList = listOf(
            FoodInfo(FoodNameInfo(FOOD_2), FoodPriceInfo(), NameChipInfo()),
            FoodInfo(FoodNameInfo(FOOD_3), FoodPriceInfo(), NameChipInfo())
        )

        viewModel.saveFoodList(newList)

        assertEquals(2, viewModel.foodList.size)
        assertEquals(FOOD_2, viewModel.foodList[0].foodName.name)
        assertEquals(FOOD_3, viewModel.foodList[1].foodName.name)
    }

    @Test
    fun `saveFoodList should handle empty input list`() {
        val emptyList = listOf<FoodInfo>()

        viewModel.saveFoodList(emptyList)

        assertEquals(0, viewModel.foodList.size)
    }

    @Test
    fun `saveDiscount should save empty string if discount is zero`() {
        val discount = 0.0

        viewModel.saveDiscount(discount)

        assertEquals("", viewModel.discount)
    }

    @Test
    fun `saveDiscount should turn discount into integer before save to string if the discount is integer`() {
        val discount = 10.0

        viewModel.saveDiscount(discount)

        assertEquals("10", viewModel.discount)
    }

    @Test
    fun `saveDiscount should save discount as string`() {
        val discount = 123.123

        viewModel.saveDiscount(discount)

        assertEquals("123.123", viewModel.discount)
    }

    @Test
    fun `saveVat should save empty string if vat is zero`() {
        val vat = 0.0

        viewModel.saveVat(vat)

        assertEquals("", viewModel.vat)
    }

    @Test
    fun `saveVat should turn vat into integer before save to string if the vat is integer`() {
        val vat = 7.0

        viewModel.saveVat(vat)

        assertEquals("7", viewModel.vat)
    }

    @Test
    fun `saveVat should save vat as string`() {
        val vat = 7.75

        viewModel.saveVat(vat)

        assertEquals("7.75", viewModel.vat)
    }

    @Test
    fun `saveServiceCharge should save empty string if service charge is zero`() {
        val serviceCharge = 0.0

        viewModel.saveServiceCharge(serviceCharge)

        assertEquals("", viewModel.serviceCharge)
    }

    @Test
    fun `saveServiceCharge should turn service charge into integer before save to string if the service charge is integer`() {
        val serviceCharge = 15.0

        viewModel.saveServiceCharge(serviceCharge)

        assertEquals("15", viewModel.serviceCharge)
    }

    @Test
    fun `saveServiceCharge should save service charge as string`() {
        val serviceCharge = 15.5

        viewModel.saveServiceCharge(serviceCharge)

        assertEquals("15.5", viewModel.serviceCharge)
    }

    @Test
    fun `setIsPercentage should update isPercentage`() {
        viewModel.setIsPercentage(true)
        assertTrue(viewModel.isPercentage)

        viewModel.setIsPercentage(false)
        assertFalse(viewModel.isPercentage)
    }

    @Test
    fun `saveVatFractionBundle should update vatFraction`() {
        viewModel.saveVatFractionBundle(0.07)
        assertEquals(0.07, viewModel.vatFraction)
    }

    @Test
    fun `saveDiscountFractionBundle should update discountFraction`() {
        viewModel.saveDiscountFractionBundle(0.10)
        assertEquals(0.10, viewModel.discountFraction)
    }

    @Test
    fun `saveServiceChargeFractionBundle should update serviceChargeFraction`() {
        viewModel.saveServiceChargeFractionBundle(0.10)
        assertEquals(0.10, viewModel.serviceChargeFraction)
    }

    @Test
    fun `vScDcFractionBundle should return correct VatScDcBundleInfo`() {
        viewModel.saveVatFractionBundle(0.07)
        viewModel.saveDiscountFractionBundle(0.10)
        viewModel.saveServiceChargeFractionBundle(0.10)

        val bundle = viewModel.vScDcFractionBundle()

        assertEquals(0.10, bundle.discount)
        assertEquals(0.10, bundle.serviceCharge)
        assertEquals(0.07, bundle.vat)
    }

    companion object {
        private const val FOOD_1 = "Pizza"
        private const val FOOD_2 = "Sushi"
        private const val FOOD_3 = "Steak"
    }
}