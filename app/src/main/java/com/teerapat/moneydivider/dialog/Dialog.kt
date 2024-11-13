package com.teerapat.moneydivider.dialog

import android.content.DialogInterface
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.teerapat.moneydivider.utils.DialogUtil
import com.teerapat.moneydivider.widget.CustomAlertDialog
import com.teerapat.moneydivider.widget.customCheckBox.CustomCheckBoxDialog
import com.teerapat.moneydivider.widget.customRadio.CustomRadioDialog

open class Dialog constructor(private val fragmentActivity: FragmentActivity) : DialogAble {
    override fun show(
        @StringRes title: Int,
        @StringRes description: Int,
        descriptionArg: Any?,
        isShowPositiveButton: Boolean,
        @ColorRes titleBackground: Int,
        onPositiveButtonClick: (dialog: DialogInterface) -> Unit,
        onNegativeButtonClick: (dialog: DialogInterface) -> Unit,
        onDismissListener: () -> Unit
    ) {
        val descriptionText = if (descriptionArg != null) {
            fragmentActivity.getString(description, descriptionArg)
        } else {
            fragmentActivity.getString(description)
        }

        displayDialog(
            title = fragmentActivity.getString(title),
            description = descriptionText,
            isShowPositiveButton = isShowPositiveButton,
            titleBackground = titleBackground,
            onPositiveButtonClick = onPositiveButtonClick,
            onNegativeButtonClick = onNegativeButtonClick,
            onDismissListener = onDismissListener
        )
    }

    protected open fun displayDialog(
        title: String,
        description: String,
        isShowPositiveButton: Boolean,
        titleBackground: Int,
        onPositiveButtonClick: (dialog: DialogInterface) -> Unit,
        onNegativeButtonClick: (dialog: DialogInterface) -> Unit,
        onDismissListener: () -> Unit
    ) {
        DialogUtil.dialog = CustomAlertDialog.Builder(fragmentActivity)
            .setTitle(title)
            .setDescription(description)
            .setIsShowPositiveButton(isShowPositiveButton)
            .setTitleBackground(titleBackground)
            .setPositiveAction { dialog, _ -> onPositiveButtonClick(dialog) }
            .setNegativeAction { dialog, _ -> onNegativeButtonClick(dialog) }
            .setOnDismissListener { onDismissListener() }
            .show()
    }

    override fun showNameSelectionDialog(
        names: Array<String>,
        isCheckedArray: BooleanArray,
        onPositiveButtonClick: (dialog: DialogInterface) -> Unit,
        onNegativeButtonClick: (dialog: DialogInterface) -> Unit
    ) {
        displayNameSelectionDialog(
            names = names,
            isCheckedArray = isCheckedArray,
            onPositiveButtonClick = onPositiveButtonClick,
            onNegativeButtonClick = onNegativeButtonClick
        )
    }

    protected open fun displayNameSelectionDialog(
        names: Array<String>,
        isCheckedArray: BooleanArray,
        onPositiveButtonClick: (dialog: DialogInterface) -> Unit,
        onNegativeButtonClick: (dialog: DialogInterface) -> Unit
    ) {
        DialogUtil.checkBoxDialog = CustomCheckBoxDialog.Builder(fragmentActivity)
            .setNames(names)
            .setIsCheckedArray(isCheckedArray)
            .setPositiveAction { dialog, _ -> onPositiveButtonClick(dialog) }
            .setSelectAllAction() { dialog, _ -> onNegativeButtonClick(dialog) }
            .show()
    }

    override fun showSingleItemSelectionDialog(
        options: Array<String>,
        onPositiveButtonClick: (dialog: DialogInterface, selectedOptionPosition: Int) -> Unit
    ) {
        displaySingleItemSelectionDialog(
            options = options,
            onPositiveButtonClick = onPositiveButtonClick
        )
    }

    protected open fun displaySingleItemSelectionDialog(
        options: Array<String>,
        onPositiveButtonClick: (dialog: DialogInterface, selectedOptionPosition: Int) -> Unit,
    ) {
        DialogUtil.radioDialog = CustomRadioDialog.Builder(fragmentActivity)
            .setOptions(options)
            .setPositiveAction { dialog, selectedOptionPosition ->
                onPositiveButtonClick(
                    dialog,
                    selectedOptionPosition
                )
            }
            .show()
    }
}