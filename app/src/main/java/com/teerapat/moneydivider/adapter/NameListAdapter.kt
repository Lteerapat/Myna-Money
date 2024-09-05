package com.teerapat.moneydivider.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.data.NameInfo
import com.teerapat.moneydivider.databinding.NameListCardBinding
import com.teerapat.moneydivider.utils.showDeleteItemConfirmationDialog

class NameListAdapter(
    private val context: Context,
) : RecyclerView.Adapter<NameListAdapter.NameListViewHolder>() {
    private val nameInfo = mutableListOf<NameInfo>()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<NameInfo>) {
        nameInfo.clear()
        nameInfo.addAll(items)
        notifyDataSetChanged()
    }

    fun addItem(item: NameInfo) {
        nameInfo.add(item)
        notifyItemInserted(nameInfo.size - 1)
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < nameInfo.size) {
            nameInfo.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getNameList(): List<NameInfo> {
        return nameInfo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = NameListCardBinding.inflate(layoutInflater, parent, false)

        return NameListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NameListViewHolder, position: Int) {
        holder.apply {
            bindView(nameInfo[position])
        }
    }

    override fun getItemCount(): Int {
        return nameInfo.size
    }

    inner class NameListViewHolder(val binding: NameListCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var currentTextWatcher: TextWatcher? = null

        fun bindView(nameInfo: NameInfo) {
            currentTextWatcher?.let { binding.etNameList.removeTextChangedListener(it) }
            binding.etNameList.setText(nameInfo.name)
            setBackgroundTint(nameInfo.isIncomplete)

            currentTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    nameInfo.name = s.toString()
                    nameInfo.isIncomplete = false
                    setBackgroundTint(false)
                }
            }
            binding.etNameList.addTextChangedListener(currentTextWatcher)

            binding.ivDeleteNameList.setOnClickListener {
                if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    handleDelete(absoluteAdapterPosition)
                }
            }
        }

        private fun setBackgroundTint(isIncomplete: Boolean) {
            val color = if (isIncomplete) {
                R.color.red
            } else {
                R.color.teal_700
            }
            binding.etNameList.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, color))
        }

        private fun handleDelete(position: Int) {
            binding.ivDeleteNameList.isEnabled = false
            val nameListText = binding.etNameList.text.toString()

            if (nameListText.isNotBlank()) {
                showDeleteItemConfirmationDialog(context, binding.ivDeleteNameList) {
                    removeItem(position)
                }
            } else {
                removeItem(position)
                binding.ivDeleteNameList.isEnabled = true
            }
        }
    }
}