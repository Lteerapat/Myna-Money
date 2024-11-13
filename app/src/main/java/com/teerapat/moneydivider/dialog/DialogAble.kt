package com.teerapat.moneydivider.dialog

import android.content.DialogInterface
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.teerapat.moneydivider.R

interface DialogAble {
    fun show(
        @StringRes title: Int,
        @StringRes description: Int,
        descriptionArg: Any? = null,
        isShowPositiveButton: Boolean = true,
        @DrawableRes titleBackground: Int = R.drawable.rounded_top_corner_pink_dialog,
        onPositiveButtonClick: (dialog: DialogInterface) -> Unit = {},
        onNegativeButtonClick: (dialog: DialogInterface) -> Unit = {},
        onDismissListener: () -> Unit = {}
    )

    fun showNameSelectionDialog(
        names: Array<String> = emptyArray(),
        isCheckedArray: BooleanArray = BooleanArray(0),
        onPositiveButtonClick: (dialog: DialogInterface) -> Unit = {},
        onNegativeButtonClick: (dialog: DialogInterface) -> Unit = {}
    )

    fun showSingleItemSelectionDialog(
        options: Array<String> = arrayOf(
            "Add 5 items",
            "Add 10 items",
            "Add 15 items",
            "Add 20 items"
        ),
        onPositiveButtonClick: (dialog: DialogInterface, selectedOptionPosition: Int) -> Unit = { _, _ -> }
    )
}
