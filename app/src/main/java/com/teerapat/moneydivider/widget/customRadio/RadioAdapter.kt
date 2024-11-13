package com.teerapat.moneydivider.widget.customRadio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teerapat.moneydivider.databinding.RadioItemBinding

class RadioAdapter(
    private val options: Array<String>
) : RecyclerView.Adapter<RadioAdapter.RadioAdapterViewHolder>() {
    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioAdapterViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RadioItemBinding.inflate(layoutInflater, parent, false)
        return RadioAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RadioAdapterViewHolder, position: Int) {
        holder.apply {
            bindView(options[position])
        }
    }

    override fun getItemCount(): Int {
        return options.size
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }

    inner class RadioAdapterViewHolder(val binding: RadioItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(option: String) {
            with(binding.radio) {
                text = option
                isChecked = absoluteAdapterPosition == selectedPosition

                setOnClickListener {
                    if (selectedPosition != absoluteAdapterPosition) {
                        val previousPosition = selectedPosition
                        selectedPosition = absoluteAdapterPosition
                        notifyItemChanged(previousPosition)
                        notifyItemChanged(selectedPosition)
                    }
                }
            }
        }
    }
}