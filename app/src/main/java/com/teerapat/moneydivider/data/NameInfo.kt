package com.teerapat.moneydivider.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NameInfo(
    var name: String = "",
    var isChecked: Boolean = false,
    var isIncomplete: Boolean = false
) : Parcelable
