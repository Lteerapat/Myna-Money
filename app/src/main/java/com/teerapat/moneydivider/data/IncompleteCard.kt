package com.teerapat.moneydivider.data

import androidx.annotation.StringRes

data class IncompleteCard(
    val position: Int,
    @StringRes val message: Int,
    val incompleteField: String
)