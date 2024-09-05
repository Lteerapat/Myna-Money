package com.teerapat.moneydivider.addnamelist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NameInfo(
    var name: String,
    var isChecked: Boolean = false,
    var isIncomplete: Boolean = false
) : Parcelable
