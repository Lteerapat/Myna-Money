package com.teerapat.moneydivider.utils

import com.teerapat.moneydivider.widget.CustomAlertDialog
import com.teerapat.moneydivider.widget.customCheckBox.CustomCheckBoxDialog
import com.teerapat.moneydivider.widget.customRadio.CustomRadioDialog

object DialogUtil {
    var dialog: CustomAlertDialog? = null
    var checkBoxDialog: CustomCheckBoxDialog? = null
    var radioDialog: CustomRadioDialog? = null

    fun isDialogShowing(): Boolean = dialog?.isShowing ?: false
    fun isCheckBoxDialogShowing(): Boolean = checkBoxDialog?.isShowing ?: false
    fun isRadioDialogShowing(): Boolean = radioDialog?.isShowing ?: false
}