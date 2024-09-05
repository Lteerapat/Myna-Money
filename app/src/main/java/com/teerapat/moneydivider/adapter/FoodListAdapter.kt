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
import com.teerapat.moneydivider.data.FoodInfo
import com.teerapat.moneydivider.databinding.FoodListCardBinding
import com.teerapat.moneydivider.utils.showDeleteItemConfirmationDialog

class FoodListAdapter(
    private val context: Context
) : RecyclerView.Adapter<FoodListAdapter.FoodListViewHolder>() {
    private val foodInfo = mutableListOf<FoodInfo>()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<FoodInfo>) {
        foodInfo.clear()
        foodInfo.addAll(items)
        notifyDataSetChanged()
    }

    fun addItem(item: FoodInfo) {
        foodInfo.add(item)
        notifyItemInserted(foodInfo.size - 1)
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < foodInfo.size) {
            foodInfo.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getFoodList(): List<FoodInfo> {
        return foodInfo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = FoodListCardBinding.inflate(layoutInflater, parent, false)

        return FoodListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodListViewHolder, position: Int) {
        holder.apply {
            bindView(foodInfo[position])
        }
    }

    override fun getItemCount(): Int {
        return foodInfo.size
    }

    inner class FoodListViewHolder(val binding: FoodListCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var currentTextWatcher: TextWatcher? = null

        fun bindView(foodInfo: FoodInfo) {
            currentTextWatcher?.let { binding.etFoodList.removeTextChangedListener(it) }
            binding.etFoodList.setText(foodInfo.foodName)
            setBackgroundTint(foodInfo.isIncomplete)

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
                    foodInfo.foodName = s.toString()
                    foodInfo.isIncomplete = false
                    setBackgroundTint(false)
                }
            }
            binding.etFoodList.addTextChangedListener(currentTextWatcher)

            binding.ivDeleteFoodList.setOnClickListener {
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
            binding.etFoodList.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, color))
        }

        private fun handleDelete(position: Int) {
            binding.ivDeleteFoodList.isEnabled = false
            val nameListText = binding.etFoodList.text.toString()

            if (nameListText.isNotEmpty()) {
                showDeleteItemConfirmationDialog(context, binding.ivDeleteFoodList) {
                    removeItem(position)
                }
            } else {
                removeItem(position)
                binding.ivDeleteFoodList.isEnabled = true
            }
        }
    }


}