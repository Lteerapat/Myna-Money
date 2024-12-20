package com.teerapat.moneydivider.addnamelist

import android.annotation.SuppressLint
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

@SuppressLint("NotifyDataSetChanged")
class NameListAdapter : RecyclerView.Adapter<NameListAdapter.NameListViewHolder>() {
    private val nameInfoList = mutableListOf<NameInfo>()
    private var onClickButtonDelete: (position: Int) -> Unit = {}

    fun setItems(items: List<NameInfo>) {
        nameInfoList.clear()
        nameInfoList.addAll(items)
        notifyDataSetChanged()
    }

    fun addItem(item: NameInfo) {
        nameInfoList.add(item)
        notifyItemInserted(nameInfoList.size - 1)
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < nameInfoList.size) {
            nameInfoList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun removeAllItem() {
        nameInfoList.clear()
        notifyDataSetChanged()
    }

    fun getNameList(): List<NameInfo> {
        return nameInfoList
    }

    fun setOnClickButtonDelete(block: (Int) -> Unit) = apply {
        onClickButtonDelete = block
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = NameListCardBinding.inflate(layoutInflater, parent, false)

        return NameListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NameListViewHolder, position: Int) {
        holder.apply {
            bindView(nameInfoList[position])
        }
    }

    override fun getItemCount(): Int {
        return nameInfoList.size
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
                    nameInfo.isIncomplete = nameInfo.name.isEmpty()
                    setBackgroundTint(nameInfo.isIncomplete)
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
                R.color.colorGreen066E38
            }

            with(binding.etNameList) {
                setHintTextColor(itemView.context.resources.getColor(color))
                backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(itemView.context, color))
            }
        }

        private fun handleDelete(position: Int) {
            val nameListText = binding.etNameList.text.toString()

            if (nameListText.isNotBlank()) {
                onClickButtonDelete.invoke(position)
            } else {
                removeItem(position)
            }
        }
    }
}