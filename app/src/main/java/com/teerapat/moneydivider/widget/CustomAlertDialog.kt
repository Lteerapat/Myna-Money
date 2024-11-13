package com.teerapat.moneydivider.widget

import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.teerapat.moneydivider.databinding.CustomAlertDialogBinding
import com.teerapat.moneydivider.utils.DialogUtil

class CustomAlertDialog private constructor(
    context: Context,
    title: CharSequence?,
    description: CharSequence?,
    isShowPositiveButton: Boolean?,
    titleBackground: Int?,
    onPositiveButtonClick: DialogInterface.OnClickListener?,
    onNegativeButtonClick: DialogInterface.OnClickListener?,
) : AlertDialog(context) {
    private val binding: CustomAlertDialogBinding = CustomAlertDialogBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        with(binding.dialogTitle) {
            title?.let {
                text = title
            }

            titleBackground?.let {
                background = ContextCompat.getDrawable(context, titleBackground)
            }
        }

        binding.dialogDescription.text = description

        binding.btnYes.apply {
            isShowPositiveButton?.let {
                isVisible = isShowPositiveButton
            }
            setOnClickListener {
                onPositiveButtonClick?.onClick(
                    this@CustomAlertDialog,
                    YES_BUTTON
                )
                dismiss()
            }
        }

        binding.btnNo.apply {
            setOnClickListener {
                onNegativeButtonClick?.onClick(
                    this@CustomAlertDialog,
                    NO_BUTTON
                )
                dismiss()
            }
        }
    }

    class Builder(private val context: Context) {
        private var title: CharSequence? = null
        private var description: CharSequence? = null
        private var isShowPositiveButton: Boolean? = null
        private var titleBackground: Int? = null
        private var onPositiveButtonClick: DialogInterface.OnClickListener? = null
        private var onNegativeButtonClick: DialogInterface.OnClickListener? = null
        private var onDismissListener: OnDismissListener? = null

        fun setTitle(title: CharSequence): Builder {
            this.title = title
            return this
        }

        fun setDescription(description: CharSequence): Builder {
            this.description = description
            return this
        }

        fun setIsShowPositiveButton(isShowPositiveButton: Boolean): Builder {
            this.isShowPositiveButton = isShowPositiveButton
            return this
        }

        fun setTitleBackground(titleBackground: Int): Builder {
            this.titleBackground = titleBackground
            return this
        }

        fun setPositiveAction(onPositiveButtonClick: DialogInterface.OnClickListener?): Builder {
            this.onPositiveButtonClick = onPositiveButtonClick
            return this
        }

        fun setNegativeAction(onNegativeButtonClick: DialogInterface.OnClickListener?): Builder {
            this.onNegativeButtonClick = onNegativeButtonClick
            return this
        }

        fun setOnDismissListener(onDismissListener: OnDismissListener?): Builder {
            this.onDismissListener = onDismissListener
            return this
        }

        fun show(): CustomAlertDialog? {
            if (DialogUtil.isDialogShowing()) {
                return DialogUtil.dialog
            }

            val customAlertDialog = CustomAlertDialog(
                context = context,
                title = title,
                description = description,
                isShowPositiveButton = isShowPositiveButton,
                titleBackground = titleBackground,
                onPositiveButtonClick = onPositiveButtonClick,
                onNegativeButtonClick = onNegativeButtonClick,
            )

            onDismissListener?.let {
                customAlertDialog.setOnDismissListener(it)
            }

            customAlertDialog.show()
            return customAlertDialog
        }
    }

    companion object {
        const val YES_BUTTON = DialogInterface.BUTTON_POSITIVE
        const val NO_BUTTON = DialogInterface.BUTTON_NEGATIVE
    }
}