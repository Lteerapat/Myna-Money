package com.teerapat.moneydivider.addfoodlist

import androidx.lifecycle.LiveData
import com.hadilq.liveevent.LiveEvent
import com.teerapat.moneydivider.BaseViewModel
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.data.FoodInfo
import com.teerapat.moneydivider.data.IncompleteCard
import com.teerapat.moneydivider.data.NameInfo
import com.teerapat.moneydivider.data.VatScDcBundleInfo
import com.teerapat.moneydivider.utils.action

class AddFoodListViewModel : BaseViewModel() {
    var foodList: MutableList<FoodInfo> = mutableListOf()
    var discount: String = ""
    var serviceCharge: String = ""
    var vat: String = ""
    var discountFraction: Double = 0.0
    var serviceChargeFraction: Double = 0.0
    var vatFraction: Double = 0.0
    var isPercentage = false
    var nameListBundle: MutableList<NameInfo> = mutableListOf()

    private val _showDialogIncompleteItem = LiveEvent<IncompleteCard>()
    val showDialogIncompleteItem: LiveData<IncompleteCard>
        get() = _showDialogIncompleteItem

    private val _showDialogEmptyFoodList = LiveEvent<Unit>()
    val showDialogEmptyFoodList: LiveData<Unit>
        get() = _showDialogEmptyFoodList

    private val _showDialogDuplicateFoodName = LiveEvent<Unit>()
    val showDialogDuplicateFoodName: LiveData<Unit>
        get() = _showDialogDuplicateFoodName

    private val _showDialogConfirmNavigate = LiveEvent<Unit>()
    val showDialogConfirmNavigate: LiveData<Unit>
        get() = _showDialogConfirmNavigate

    fun saveFoodList(list: List<FoodInfo>) {
        foodList.clear()
        foodList.addAll(list)
    }

    fun saveDiscount(discount: Double) {
        this.discount = if (discount == 0.0) {
            ""
        } else if (discount % 1 == 0.0) {
            discount.toInt().toString()
        } else {
            discount.toString()
        }
    }

    fun saveServiceCharge(serviceCharge: Double) {
        this.serviceCharge = if (serviceCharge == 0.0) {
            ""
        } else if (serviceCharge % 1 == 0.0) {
            serviceCharge.toInt().toString()
        } else {
            serviceCharge.toString()
        }
    }

    fun saveVat(vat: Double) {
        this.vat = if (vat == 0.0) {
            ""
        } else if (vat % 1 == 0.0) {
            vat.toInt().toString()
        } else {
            vat.toString()
        }
    }

    fun setIsPercentage(boolean: Boolean) {
        isPercentage = boolean
    }

    fun saveVatFractionBundle(vat: Double) {
        this.vatFraction = vat
    }

    fun saveDiscountFractionBundle(dc: Double) {
        this.discountFraction = dc
    }

    fun saveServiceChargeFractionBundle(sc: Double) {
        this.serviceChargeFraction = sc
    }

    fun vScDcFractionBundle(): VatScDcBundleInfo {
        return VatScDcBundleInfo(
            discount = discountFraction,
            serviceCharge = serviceChargeFraction,
            vat = vatFraction
        )
    }

    fun executeNextButton(foodList: List<FoodInfo>) {
        when {
            foodList.isEmpty() -> {
                _showDialogEmptyFoodList.action()
            }

            findFirstIncompleteCard(foodList)?.let {
                _showDialogIncompleteItem.action(it)
                true
            } == true -> {}

            hasDuplicateNames(foodList) -> {
                _showDialogDuplicateFoodName.action()
            }

            else -> {
                _showDialogConfirmNavigate.action()
            }
        }
    }

    private fun hasDuplicateNames(foodList: List<FoodInfo>): Boolean {
        val names = foodList.map { it.foodName.name.trim() }
        return names.size != names.toSet().size
    }

    private fun findFirstIncompleteCard(foodList: List<FoodInfo>): IncompleteCard? {
        foodList.forEachIndexed { index, foodInfo ->
            val foodName = foodInfo.foodName.name.trim()
            val foodPrice = foodInfo.foodPrice.price
            val name = foodInfo.name.nameList

            when {
                !foodName.matches(REGEX) -> {
                    foodList[index].foodName.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = R.string.incomplete_card_letter_or_num_message,
                        incompleteField = ET_FOOD_LIST
                    )
                }

                foodName.isBlank() -> {
                    foodList[index].foodName.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = R.string.incomplete_card_empty_message,
                        incompleteField = ET_FOOD_LIST
                    )
                }

                foodPrice.isBlank() -> {
                    foodList[index].foodPrice.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = R.string.incomplete_card_empty_message,
                        incompleteField = ET_FOOD_PRICE
                    )
                }

                foodName.matches(NUM_REGEX) -> {
                    foodList[index].foodName.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = R.string.incomplete_card_num_only_message,
                        incompleteField = ET_FOOD_LIST
                    )
                }

                removeCommasAndReturnDouble(foodPrice) == 0.0 -> {
                    foodList[index].foodPrice.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = R.string.incomplete_card_zero_message,
                        incompleteField = ET_FOOD_PRICE
                    )
                }

                name.isEmpty() -> {
                    foodList[index].name.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = R.string.incomplete_card_zero_chip_message,
                        incompleteField = FOOD_CARD_CONTAINER
                    )
                }
            }
        }

        return null
    }

    companion object {
        private val REGEX = Regex("^[A-Za-z0-9ก-๏ ]*$")
        private val NUM_REGEX = Regex("[0-9]*$")
        private const val ET_FOOD_LIST = "ET_FOOD_LIST"
        private const val ET_FOOD_PRICE = "ET_FOOD_PRICE"
        private const val FOOD_CARD_CONTAINER = "FOOD_CARD_CONTAINER"
    }
}

