package com.teerapat.moneydivider.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SummaryInfo(
    var name: String = "",
    var totalAmountPerName: Double = 0.0,
    var summaryFoodItemInfo: List<SummaryFoodItemInfo> = mutableListOf()
) : Parcelable

@Parcelize
data class SummaryFoodItemInfo(
    var foodName: String = "",
    var price: Double = 0.0,
) : Parcelable