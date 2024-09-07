package com.teerapat.moneydivider.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodInfo(
    var foodName: FoodNameInfo,
    var foodPrice: FoodPriceInfo,
    var name: NameChipInfo,
) : Parcelable

@Parcelize
data class FoodNameInfo(
    var name: String = "",
    var isIncomplete: Boolean = false
) : Parcelable

@Parcelize
data class FoodPriceInfo(
    var price: String = "",
    var isIncomplete: Boolean = false
) : Parcelable

@Parcelize
data class NameChipInfo(
    var nameList: List<String> = mutableListOf(),
    var isIncomplete: Boolean = false
) : Parcelable
