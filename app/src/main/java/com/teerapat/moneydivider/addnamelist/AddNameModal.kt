package com.teerapat.moneydivider.addnamelist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddNameModal(
    val name: String,
    var isChecked: Boolean = false
): Parcelable
