package com.teerapat.moneydivider.utils

import android.app.AlertDialog
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.teerapat.moneydivider.R

fun Fragment.showDeleteItemConfirmationDialog(onDeleteConfirmed: () -> Unit) {
    AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.confirm_delete_title))
        .setMessage(getString(R.string.confirm_delete_message))
        .setPositiveButton(getString(R.string.yes_btn)) { _, _ ->
            onDeleteConfirmed()
        }
        .setNegativeButton(getString(R.string.no_btn), null)
        .show()
}

fun Fragment.showAlertOnIncompleteCard(message: String, onDismiss: () -> Unit) {
    AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.incomplete_item))
        .setMessage(message)
        .setPositiveButton(getString(R.string.ok_btn), null)
        .setOnDismissListener { onDismiss() }
        .show()
}

fun Fragment.showAlertZeroCardList(onDismiss: () -> Unit) {
    AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.incomplete_item))
        .setMessage(getString(R.string.incomplete_card_at_least_1_message))
        .setPositiveButton(getString(R.string.ok_btn), null)
        .setOnDismissListener { onDismiss() }
        .show()
}

fun Fragment.showAlertOverLimitItemCard(maxItems: Int) {
    AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.item_limit_exceeded_title))
        .setMessage(getString(R.string.item_limit_exceeded_message, maxItems))
        .setPositiveButton(getString(R.string.ok_btn), null)
        .show()
}

fun Fragment.showAlertDuplicateNames() {
    AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.duplicate_name_alert_title))
        .setMessage(getString(R.string.duplicate_name_alert_message))
        .setPositiveButton(getString(R.string.ok_btn), null)
        .show()
}

fun Fragment.showContinueDialog(onContinueConfirm: () -> Unit) {
    AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.next_btn_alert_title))
        .setPositiveButton(getString(R.string.yes_btn)) { _, _ ->
            onContinueConfirm()
        }
        .setNegativeButton(getString(R.string.no_btn), null)
        .show()
}

fun Fragment.showAlertOnVScDis(
    title: String,
    message: String,
    serviceChargeField: EditText?,
    vatField: EditText?,
    discountField: EditText?
) {
    when (title) {
        getString(R.string.service_charge) -> serviceChargeField?.text?.clear()
        getString(R.string.vat) -> vatField?.text?.clear()
        getString(R.string.discount) -> discountField?.text?.clear()
    }

    AlertDialog.Builder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(getString(R.string.ok_btn), null)
        .show()
}

fun Fragment.showTogglePercentageAmountDialog(
    onPercentageSelected: () -> Unit,
    onAmountSelected: () -> Unit
) {
    AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.percentage_or_amount_toggle_title))
        .setPositiveButton(getString(R.string.percentage_message)) { _, _ ->
            onPercentageSelected()
        }
        .setNegativeButton(getString(R.string.amount_message)) { _, _ ->
            onAmountSelected()
        }
        .show()
}