package com.teerapat.moneydivider.widget

import android.app.AlertDialog
import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.utils.MoneyValueFormatter
import com.teerapat.moneydivider.utils.getDecimalFormattedString

class ThousandSeparatedEditText constructor(context: Context, attributeSet: AttributeSet) :
    TextInputEditText(context, attributeSet), TextWatcher {

    private var attr = attributeSet
    private var isSeparatedByCommas: Boolean = false
    private var noOdfDecimals: Int = 2

    init {
        parseAttributes(context.obtainStyledAttributes(attr, R.styleable.ThousandSeparatedEditText))
        this.addTextChangedListener(this)
        this.keyListener = DigitsKeyListener.getInstance("0123456789.")
        initNoOfDecimals()
    }

    private fun parseAttributes(a: TypedArray) {
        isSeparatedByCommas = a.getBoolean(
            R.styleable.ThousandSeparatedEditText_isSeparateByCommas,
            true
        )
        noOdfDecimals = a.getInteger(
            R.styleable.ThousandSeparatedEditText_noOfDecimals,
            2
        )
    }

    private fun initNoOfDecimals() {
        this@ThousandSeparatedEditText.filters = arrayOf<InputFilter>(
            MoneyValueFormatter(
                sign = false,
                decimal = true,
                digits = noOdfDecimals
            )
        )
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
    }

    override fun afterTextChanged(p0: Editable?) {
        if (isSeparatedByCommas) {
            val cursorPosition: Int = this@ThousandSeparatedEditText.selectionEnd
            val originalStr: String = this@ThousandSeparatedEditText.text.toString()

            try {
                this@ThousandSeparatedEditText.removeTextChangedListener(this)
                val value: String = this@ThousandSeparatedEditText.text.toString()
                val integerPart = value.replace(",", "").split(".")[0]

                if (value != "") {
                    if (integerPart.length > maxDigitsBeforeDecimal) {
                        this@ThousandSeparatedEditText.text?.clear()
                        showAlertOverLimitDigits()
                        this@ThousandSeparatedEditText.setSelection(0)
                    }
                    if (value.startsWith(".")) {
                        this@ThousandSeparatedEditText.setText("0.")
                    }
                    val str: String = this@ThousandSeparatedEditText.text.toString().replace(
                        ",".toRegex(),
                        ""
                    )
                    if (value.isNotEmpty()) this@ThousandSeparatedEditText.setText(
                        String().getDecimalFormattedString(
                            str
                        )
                    )
                    val diff: Int =
                        this@ThousandSeparatedEditText.text.toString().length - originalStr.length
                    this@ThousandSeparatedEditText.setSelection(cursorPosition + diff)
                }
                this@ThousandSeparatedEditText.addTextChangedListener(this)
            } catch (ex: Exception) {
                ex.printStackTrace()
                this@ThousandSeparatedEditText.addTextChangedListener(this)
            }
        }
    }

    private fun showAlertOverLimitDigits() {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.input_limit_exceeded_title))
            .setMessage(context.getString(R.string.input_limit_exceeded_message))
            .setPositiveButton("OK", null)
            .show()
    }

    companion object {
        private const val maxDigitsAfterDecimal = 2
        private const val maxDigitsBeforeDecimal = 7
    }
}