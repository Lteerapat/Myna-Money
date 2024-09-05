package com.teerapat.moneydivider.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodInfo(
    var foodName: String = "",
    var foodPrice: Double = 0.0,
    var name: List<String>,
    var isIncomplete: Boolean = false
) : Parcelable
