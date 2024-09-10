package com.teerapat.moneydivider.addfoodlist

import androidx.lifecycle.ViewModel
import com.teerapat.moneydivider.data.FoodInfo
import com.teerapat.moneydivider.data.VatScDcBundleInfo

class AddFoodListViewModel : ViewModel() {
    var foodList: MutableList<FoodInfo> = mutableListOf()
    var discount: String = ""
    var serviceCharge: String = ""
    var vat: String = ""
    var discountFraction: Double = 0.0
    var serviceChargeFraction: Double = 0.0
    var vatFraction: Double = 0.0
    private var totalAmount: String = ""
    var isPercentage = true
    var vatScDcBundle: VatScDcBundleInfo? = null

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

    fun saveTotalAmount(totalAmount: Double) {
        this.totalAmount = if (totalAmount % 1 == 0.0) {
            totalAmount.toInt().toString()
        } else {
            totalAmount.toString()
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
}

