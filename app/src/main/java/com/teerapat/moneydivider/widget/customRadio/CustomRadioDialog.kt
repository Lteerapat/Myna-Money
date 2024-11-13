package com.teerapat.moneydivider.widget.customRadio

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.teerapat.moneydivider.databinding.CustomRadioDialogBinding
import com.teerapat.moneydivider.utils.DialogUtil

class CustomRadioDialog private constructor(
    context: Context,
    options: Array<String>?,
    private val onPositiveButtonClick: ((DialogInterface, Int) -> Unit)?
) :
    AlertDialog(context) {
    private val binding: CustomRadioDialogBinding =
        CustomRadioDialogBinding.inflate(layoutInflater)
    private var radioAdapter: RadioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        radioAdapter = RadioAdapter(
            options = options ?: emptyArray(),
        )

        binding.rvRadioDialog.adapter = radioAdapter

        binding.btnConfirm.apply {
            setOnClickListener {
                val selectedOptionPosition = radioAdapter.getSelectedPosition()
                onPositiveButtonClick?.invoke(this@CustomRadioDialog, selectedOptionPosition)
                dismiss()
            }
        }
    }

    class Builder(private val context: Context) {
        private var options: Array<String>? = null
        private var onPositiveButtonClick: ((DialogInterface, Int) -> Unit)? = null

        fun setOptions(options: Array<String>): Builder {
            this.options = options
            return this
        }

        fun setPositiveAction(onPositiveButtonClick: (DialogInterface, Int) -> Unit): Builder {
            this.onPositiveButtonClick = onPositiveButtonClick
            return this
        }

        fun show(): CustomRadioDialog? {
            if (DialogUtil.isRadioDialogShowing()) {
                return DialogUtil.radioDialog
            }

            val customRadioDialog = CustomRadioDialog(
                context = context,
                options = options,
                onPositiveButtonClick = onPositiveButtonClick,
            )

            customRadioDialog.show()
            return customRadioDialog
        }
    }
}