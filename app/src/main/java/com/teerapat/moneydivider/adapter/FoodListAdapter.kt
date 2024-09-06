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
    private var onDataChangedListener: (() -> Unit)? = null


    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<FoodInfo>) {
        foodInfo.clear()
        foodInfo.addAll(items)
        notifyDataSetChanged()
        onDataChangedListener?.invoke()
    }

    fun addItem(item: FoodInfo) {
        foodInfo.add(item)
        notifyItemInserted(foodInfo.size - 1)
        onDataChangedListener?.invoke()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < foodInfo.size) {
            foodInfo.removeAt(position)
            notifyItemRemoved(position)
            onDataChangedListener?.invoke()
        }
    }

    fun setOnDataChangedListener(listener: () -> Unit) {
        onDataChangedListener = listener
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
        private var currentEtFoodListTextWatcher: TextWatcher? = null
        private var currentEtFoodPriceTextWatcher: TextWatcher? = null

        fun bindView(foodInfo: FoodInfo) {
            currentEtFoodListTextWatcher?.let { binding.etFoodList.removeTextChangedListener(it) }
            currentEtFoodPriceTextWatcher?.let { binding.etFoodPrice.removeTextChangedListener(it) }
            binding.etFoodList.setText(foodInfo.foodName)
            binding.etFoodPrice.setText(foodInfo.foodPrice)
            setBackgroundTint(foodInfo.isIncomplete)

            currentEtFoodListTextWatcher = object : TextWatcher {
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
            binding.etFoodList.addTextChangedListener(currentEtFoodListTextWatcher)

            currentEtFoodPriceTextWatcher = object :
                TextWatcher {
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
                    foodInfo.foodPrice = s.toString().replace(",", "")
                    foodInfo.isIncomplete = false
                    setBackgroundTint(false)
                    onDataChangedListener?.invoke()
                }
            }
            binding.etFoodPrice.addTextChangedListener(currentEtFoodPriceTextWatcher)

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
            val foodListText = binding.etFoodList.text.toString()
            val foodPriceText = binding.etFoodPrice.text.toString()

            if (foodListText.isNotBlank() || foodPriceText.isNotBlank() || binding.nameChipContainer.childCount > 0) {
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