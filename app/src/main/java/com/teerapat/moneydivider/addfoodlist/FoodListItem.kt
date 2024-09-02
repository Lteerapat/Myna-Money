package com.teerapat.moneydivider.addfoodlist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodInfo(val foodName: String, val foodPrice: Double, val name: List<String>) :
    Parcelable
