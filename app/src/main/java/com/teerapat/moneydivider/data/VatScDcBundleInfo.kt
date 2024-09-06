package com.teerapat.moneydivider.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VatScDcBundleInfo(
    val discount: Double,
    val serviceCharge: Double,
    val vat: Double
) : Parcelable
