package com.teerapat.moneydivider.widget.customCheckBox

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teerapat.moneydivider.databinding.CheckBoxItemBinding

class CheckBoxAdapter(
    private val names: Array<String>,
    private val isCheckedArray: BooleanArray,
) : RecyclerView.Adapter<CheckBoxAdapter.CheckBoxViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckBoxViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CheckBoxItemBinding.inflate(layoutInflater, parent, false)
        return CheckBoxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CheckBoxViewHolder, position: Int) {
        holder.apply {
            bindView(names[position], isCheckedArray[position])
        }
    }

    override fun getItemCount(): Int {
        return names.size
    }

    inner class CheckBoxViewHolder(val binding: CheckBoxItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(name: String, isChecked: Boolean) {
            with(binding.checkBox) {
                text = name
                setChecked(isChecked)
                setOnCheckedChangeListener { _, isChecked ->
                    isCheckedArray[absoluteAdapterPosition] = isChecked
                }
            }
        }
    }
}
