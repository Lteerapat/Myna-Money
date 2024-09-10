package com.teerapat.moneydivider.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VatScDcBundleInfo(
    val discount: Double = 0.0,
    val serviceCharge: Double = 0.0,
    val vat: Double = 0.0,
    val totalAmount: Double = 0.0
) : Parcelable
