package com.teerapat.moneydivider.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class SeparateThousands(private val editText: EditText) : TextWatcher {

    private var busy = false

    override fun afterTextChanged(s: Editable?) {
        if (busy) return

        s?.let {
            busy = true

            // Save cursor position
            val cursorPosition = editText.selectionStart

            // Remove existing formatting (commas)
            val cleanString = s.toString().replace(",", "")

            // Format the number with commas
            val formattedString = try {
                val number = cleanString.toDouble()
                String.format("%,.2f", number)
            } catch (e: NumberFormatException) {
                cleanString
            }

            // Set the formatted string and restore cursor position
            editText.setText(formattedString)
            editText.setSelection(Math.min(formattedString.length, cursorPosition))

            busy = false
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}
