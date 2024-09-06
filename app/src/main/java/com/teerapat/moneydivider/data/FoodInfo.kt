package com.teerapat.moneydivider.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodInfo(
    var foodName: String = "",
    var foodPrice: String = "",
    var name: List<String> = mutableListOf(),
    var isIncomplete: Boolean = false
) : Parcelable
