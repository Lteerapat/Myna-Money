package com.teerapat.moneydivider.utils

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.DigitsKeyListener
import java.util.Locale

open class MoneyValueFormatter(
    digits: Int
) : DigitsKeyListener(Locale.US, false, true) {
    private var digits: Int = 0

    init {
        this.digits = digits
    }

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence {
        val out = super.filter(source, start, end, dest, dstart, dend)

        var fSource: CharSequence? = source
        var fStart: Int = start
        var fend: Int = end

        if (out != null) {
            fSource = out
            fStart = 0
            fend = out.length
        }

        val len = fend - fStart

        if (len == 0) {
            return fSource!!
        }

        val dLen = dest!!.length

        for (i in 0 until dstart) {
            if (dest[i] == '.') {
                return String().getDecimalFormattedString(
                    if (dLen - (i + 1) + len > digits) "" else SpannableStringBuilder(
                        fSource,
                        fStart,
                        fend
                    ).toString()
                )
            }
        }

        for (i in fStart until fend) {
            if (fSource!![i] == '.') {
                return if (dLen - dend + (fend - (i + 1)) > digits) "" else break
            }
        }

        return String().getDecimalFormattedString(
            SpannableStringBuilder(
                fSource,
                fStart,
                fend
            ).toString()
        )
    }
}


