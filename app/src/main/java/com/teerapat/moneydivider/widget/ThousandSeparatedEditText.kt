package com.teerapat.moneydivider.widget

import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.Gravity
import com.google.android.material.textfield.TextInputEditText
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.utils.MoneyValueFormatter
import com.teerapat.moneydivider.utils.getDecimalFormattedString

class ThousandSeparatedEditText constructor(context: Context, attributeSet: AttributeSet) :
    TextInputEditText(context, attributeSet), TextWatcher {

    private var attr = attributeSet
    private var isSeparatedByCommas: Boolean = false
    private var noOdfDecimals: Int = maxDigitsAfterDecimal
    private var previousText: String = ""

    init {
        parseAttributes(context.obtainStyledAttributes(attr, R.styleable.ThousandSeparatedEditText))
        this.addTextChangedListener(this)
        this.keyListener = DigitsKeyListener.getInstance("0123456789.")
        gravity = Gravity.END
        initNoOfDecimals()
    }

    private fun parseAttributes(a: TypedArray) {
        isSeparatedByCommas = a.getBoolean(
            R.styleable.ThousandSeparatedEditText_isSeparateByCommas,
            true
        )
        noOdfDecimals = a.getInteger(
            R.styleable.ThousandSeparatedEditText_noOfDecimals,
            maxDigitsAfterDecimal
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
        previousText = p0.toString()
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

    companion object {
        private const val maxDigitsAfterDecimal = 2
        private const val maxDigitsBeforeDecimal = 7
    }
}