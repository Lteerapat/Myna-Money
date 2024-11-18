package com.teerapat.moneydivider

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.teerapat.moneydivider.dialog.Dialog
import com.teerapat.moneydivider.dialog.DialogAble
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

abstract class BaseFragment : Fragment() {
    val dialogAble: DialogAble by lazy {
        Dialog(requireActivity())
    }

    protected open fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    protected fun next(@IdRes resId: Int, bundle: Bundle? = null) {
        try {
            findNavController().navigate(resId, bundle)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    protected fun removeCommasAndReturnDouble(amount: String): Double {
        return if (amount.isBlank()) {
            0.0
        } else {
            amount.replace(",", "").toDoubleOrNull() ?: 0.0
        }
    }

    protected fun thousandSeparator(amount: Double): String {
        val decimalFormat = DecimalFormat(
            DECIMAL_PATTERN, DecimalFormatSymbols(
                Locale.US
            )
        )
        return "${decimalFormat.format(amount)} ฿"
    }

    protected fun convertAmountToFraction(numerator: Double, denominator: Double): Double {
        if (denominator == 0.0) {
            return 1.0
        }

        return numerator / denominator
    }

    companion object {
        private const val DECIMAL_PATTERN = "#,##0.00"
    }
}