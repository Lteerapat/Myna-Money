package com.teerapat.moneydivider.widget.customCheckBox

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.teerapat.moneydivider.databinding.CustomCheckBoxDialogBinding
import com.teerapat.moneydivider.utils.DialogUtil

@SuppressLint("NotifyDataSetChanged")
class CustomCheckBoxDialog private constructor(
    context: Context,
    names: Array<String>?,
    isCheckedArray: BooleanArray?,
    onPositiveButtonClick: DialogInterface.OnClickListener?,
    onNegativeButtonClick: DialogInterface.OnClickListener?
) :
    AlertDialog(context) {
    private val binding: CustomCheckBoxDialogBinding =
        CustomCheckBoxDialogBinding.inflate(layoutInflater)
    private var checkBoxAdapter: CheckBoxAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        checkBoxAdapter = CheckBoxAdapter(
            names = names ?: emptyArray(),
            isCheckedArray = isCheckedArray ?: BooleanArray(names?.size ?: 0),
        )

        binding.rvCheckBoxDialog.adapter = checkBoxAdapter

        binding.btnConfirm.apply {
            setOnClickListener {
                onPositiveButtonClick?.onClick(
                    this@CustomCheckBoxDialog,
                    YES_BUTTON
                )
                dismiss()
            }
        }

        binding.btnSelectAll.apply {
            setOnClickListener {
                onNegativeButtonClick?.onClick(
                    this@CustomCheckBoxDialog,
                    NO_BUTTON
                )
                checkBoxAdapter.notifyDataSetChanged()
            }
        }
    }

    class Builder(private val context: Context) {
        private var names: Array<String>? = null
        private var isCheckedArray: BooleanArray? = null
        private var onPositiveButtonClick: DialogInterface.OnClickListener? = null
        private var onNegativeButtonClick: DialogInterface.OnClickListener? = null

        fun setNames(names: Array<String>): Builder {
            this.names = names
            return this
        }

        fun setIsCheckedArray(isCheckedArray: BooleanArray): Builder {
            this.isCheckedArray = isCheckedArray
            return this
        }

        fun setPositiveAction(onPositiveButtonClick: DialogInterface.OnClickListener?): Builder {
            this.onPositiveButtonClick = onPositiveButtonClick
            return this
        }

        fun setSelectAllAction(onNegativeButtonClick: DialogInterface.OnClickListener?): Builder {
            this.onNegativeButtonClick = onNegativeButtonClick
            return this
        }

        fun show(): CustomCheckBoxDialog? {
            if (DialogUtil.isCheckBoxDialogShowing()) {
                return DialogUtil.checkBoxDialog
            }

            val customCheckBoxDialog = CustomCheckBoxDialog(
                context = context,
                names = names,
                isCheckedArray = isCheckedArray,
                onPositiveButtonClick = onPositiveButtonClick,
                onNegativeButtonClick = onNegativeButtonClick
            )

            customCheckBoxDialog.show()
            return customCheckBoxDialog
        }
    }

    companion object {
        const val YES_BUTTON = DialogInterface.BUTTON_POSITIVE
        const val NO_BUTTON = DialogInterface.BUTTON_NEGATIVE
    }
}