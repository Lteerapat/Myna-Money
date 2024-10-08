package com.teerapat.moneydivider.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.data.NameInfo

fun Fragment.showDeleteItemConfirmationDialog(
    deleteIcon: ImageView,
    onDeleteConfirmed: () -> Unit
) {
    AlertDialog.Builder(context)
        .setTitle(R.string.confirm_delete_title)
        .setMessage(R.string.confirm_delete_message)
        .setPositiveButton(R.string.yes_btn) { _, _ ->
            onDeleteConfirmed()
        }
        .setNegativeButton(R.string.no_btn, null)
        .setOnDismissListener { deleteIcon.isEnabled = true }
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

fun Fragment.showAlertDuplicateNames(onDismiss: () -> Unit) {
    AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.duplicate_name_alert_title))
        .setMessage(getString(R.string.duplicate_name_alert_message))
        .setPositiveButton(getString(R.string.ok_btn), null)
        .setOnDismissListener { onDismiss() }
        .show()
}

fun Fragment.showContinueDialog(btnNext: Button, onContinueConfirm: () -> Unit) {
    AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.next_btn_alert_title))
        .setPositiveButton(getString(R.string.yes_btn)) { _, _ ->
            onContinueConfirm()
        }
        .setNegativeButton(getString(R.string.no_btn), null)
        .setOnDismissListener { btnNext.isEnabled = true }
        .show()
}

fun Fragment.showAlertOnVScDis(
    title: String,
    message: String
) {
    AlertDialog.Builder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(getString(R.string.ok_btn), null)
        .show()
}

fun Fragment.showTogglePercentageAmountDialog(
    toggleButton: Button,
    onPercentageSelected: () -> Unit,
    onAmountSelected: () -> Unit
) {
    AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.percentage_or_amount_toggle_title))
        .setMessage(getString(R.string.percentage_or_amount_toggle_message))
        .setPositiveButton(getString(R.string.percentage_message)) { _, _ ->
            onPercentageSelected()
        }
        .setNegativeButton(getString(R.string.amount_message)) { _, _ ->
            onAmountSelected()
        }
        .setOnDismissListener { toggleButton.isEnabled = true }
        .show()
}

fun showNameSelectionDialog(
    context: Context,
    names: Array<String>,
    isCheckedArray: BooleanArray,
    ivAddNameList: ImageView,
    onOkClick: (List<NameInfo>) -> Unit,
) {
    val dialog = AlertDialog.Builder(context)
        .setTitle(R.string.add_name_for_food_list_card_title)
        .setMultiChoiceItems(
            names,
            isCheckedArray
        ) { _, position, isChecked ->
            isCheckedArray[position] = isChecked
        }
        .setPositiveButton(R.string.ok_btn) { _, _ ->
            val selectedNames = names.mapIndexed { index, name ->
                NameInfo(name, isCheckedArray[index])
            }.filter { it.isChecked }
            onOkClick(selectedNames)
        }
        .setNegativeButton(R.string.select_all, null)
        .setOnDismissListener {
            ivAddNameList.isEnabled = true
        }
        .create()

    dialog.setOnShowListener { _ ->
        val button = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE)
        button.setOnClickListener {
            for (i in isCheckedArray.indices) {
                isCheckedArray[i] = true
                dialog.listView.setItemChecked(i, true)
            }
        }
    }

    dialog.show()
}