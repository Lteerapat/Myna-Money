package com.teerapat.moneydivider.widget

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import com.google.android.material.textfield.TextInputEditText
import com.teerapat.moneydivider.utils.MoneyValueFormatter
import com.teerapat.moneydivider.utils.getDecimalFormattedString

class ThousandSeparatedEditText constructor(context: Context, attributeSet: AttributeSet) :
    TextInputEditText(context, attributeSet), TextWatcher {

    private var previousText: String = ""

    init {
        this.addTextChangedListener(this)
        this.keyListener = DigitsKeyListener.getInstance("0123456789.")
        gravity = Gravity.END
        textDirection = View.TEXT_DIRECTION_RTL
        initNoOfDecimals()
    }

    private fun initNoOfDecimals() {
        this@ThousandSeparatedEditText.filters = arrayOf<InputFilter>(
            MoneyValueFormatter(
                digits = maxDigitsAfterDecimal
            )
        )
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        previousText = p0.toString()
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        textDirection = View.TEXT_DIRECTION_LTR
    }

    override fun afterTextChanged(p0: Editable?) {
        val cursorPosition: Int = this@ThousandSeparatedEditText.selectionEnd
        val originalStr: String = this@ThousandSeparatedEditText.text.toString()

        try {
            this@ThousandSeparatedEditText.removeTextChangedListener(this)
            var value: String = this@ThousandSeparatedEditText.text.toString()
            val integerPart = value.replace(",", "").split(".")[0]

            if (value.isNotEmpty()) {
                if (integerPart.length > maxDigitsBeforeDecimal) {
                    this@ThousandSeparatedEditText.setText(previousText)
                    this@ThousandSeparatedEditText.setSelection(previousText.length)
                }

                if (value.startsWith("0") && !value.startsWith("0.") && value.length > 1) {
                    value = value.replaceFirst("0", "")
                    this@ThousandSeparatedEditText.setText(value)
                    this@ThousandSeparatedEditText.setSelection(cursorPosition - 1)
                }

                if (value.startsWith(".")) {
                    this@ThousandSeparatedEditText.setText("0.")
                }

                val str: String = this@ThousandSeparatedEditText.text.toString().replace(
                    ",".toRegex(),
                    ""
                )

                if (value.isNotEmpty()) this@ThousandSeparatedEditText.setText(
                    getDecimalFormattedString(
                        str
                    )
                )
                val diff: Int =
                    this@ThousandSeparatedEditText.text.toString().length - originalStr.length
                this@ThousandSeparatedEditText.setSelection(cursorPosition + diff)
            } else {
                textDirection = View.TEXT_DIRECTION_RTL
            }
            this@ThousandSeparatedEditText.addTextChangedListener(this)
        } catch (ex: Exception) {
            ex.printStackTrace()
            this@ThousandSeparatedEditText.addTextChangedListener(this)
        }
    }

    companion object {
        private const val maxDigitsAfterDecimal = 2
        private const val maxDigitsBeforeDecimal = 7
    }
}