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
import com.teerapat.moneydivider.addnamelist.NameInfo
import com.teerapat.moneydivider.databinding.NameListCardBinding
import com.teerapat.moneydivider.utils.showDeleteItemConfirmationDialog

@SuppressLint("NotifyDataSetChanged")
class NameListAdapter(
    private val context: Context,
) :
    RecyclerView.Adapter<NameListAdapter.NameListViewHolder>() {
    private val nameCardInfo = mutableListOf<NameInfo>()

    fun setItems(items: List<NameInfo>) {
        nameCardInfo.clear()
        nameCardInfo.addAll(items)
        notifyDataSetChanged()
    }

    fun addItem(item: NameInfo) {
        nameCardInfo.add(item)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < nameCardInfo.size) {
            nameCardInfo.removeAt(position)
            notifyDataSetChanged()
        }
    }

    fun getNameList(): List<NameInfo> {
        return nameCardInfo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = NameListCardBinding.inflate(layoutInflater, parent, false)

        return NameListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NameListViewHolder, position: Int) {
        holder.apply {
            bindView(nameCardInfo[absoluteAdapterPosition], absoluteAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return nameCardInfo.size
    }

    inner class NameListViewHolder(val binding: NameListCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentTextWatcher: TextWatcher? = null
        fun bindView(nameInfo: NameInfo, position: Int) {
            currentTextWatcher?.let { binding.etNameList.removeTextChangedListener(it) }
            binding.etNameList.setText(nameInfo.name)

            if (nameInfo.isIncomplete) {
                binding.etNameList.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red))
            } else {
                binding.etNameList.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.teal_700))
            }

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
                    nameCardInfo[position].isIncomplete = false
                    binding.etNameList.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.teal_700))
                    nameCardInfo[position].name = binding.etNameList.text.toString()
                }
            }
            binding.etNameList.addTextChangedListener(currentTextWatcher)

            binding.ivDeleteNameList.setOnClickListener {
                binding.ivDeleteNameList.isEnabled = false
                val nameListText = binding.etNameList.text.toString()
                if (nameListText.isNotEmpty()) {
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
}