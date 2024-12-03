package com.teerapat.moneydivider

import androidx.lifecycle.ViewModel

abstract class BaseViewModel:ViewModel() {
    protected fun removeCommasAndReturnDouble(amount: String): Double {
        return if (amount.isBlank()) {
            0.0
        } else {
            amount.replace(",", "").toDoubleOrNull() ?: 0.0
        }
    }
}