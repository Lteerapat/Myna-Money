package com.teerapat.moneydivider.addfoodlist

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.data.FoodInfo
import com.teerapat.moneydivider.databinding.FoodListCardBinding

class FoodListAdapter : RecyclerView.Adapter<FoodListAdapter.FoodListViewHolder>() {
    private val foodInfoList = mutableListOf<FoodInfo>()
    private var onDataChangedListener: (() -> Unit)? = {}
    private var onClickButtonDelete: (deleteInfo: Pair<ImageView, Int>) -> Unit = {}
    private var onShowCheckboxDialog: ((FoodInfo) -> Unit)? = {}

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<FoodInfo>) {
        foodInfoList.clear()
        foodInfoList.addAll(items)
        notifyDataSetChanged()
        onDataChangedListener?.invoke()
    }

    fun addItem(item: FoodInfo) {
        foodInfoList.add(item)
        notifyItemInserted(foodInfoList.size - 1)
        onDataChangedListener?.invoke()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < foodInfoList.size) {
            foodInfoList.removeAt(position)
            notifyItemRemoved(position)
            onDataChangedListener?.invoke()
        }
    }

    fun setOnDataChangedListener(listener: () -> Unit) = apply {
        onDataChangedListener = listener
    }

    fun setOnShowCheckboxDialog(listener: (FoodInfo) -> Unit) = apply {
        onShowCheckboxDialog = listener
    }

    fun setOnClickButtonDelete(block: (Pair<ImageView, Int>) -> Unit) = apply {
        onClickButtonDelete = block
    }

    fun getFoodList(): List<FoodInfo> {
        return foodInfoList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = FoodListCardBinding.inflate(layoutInflater, parent, false)
        return FoodListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodListViewHolder, position: Int) {
        holder.apply {
            bindView(foodInfoList[position])
        }
    }

    override fun getItemCount(): Int {
        return foodInfoList.size
    }

    fun updateChips(foodInfo: FoodInfo) {
        val position = foodInfoList.indexOf(foodInfo)
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    inner class FoodListViewHolder(val binding: FoodListCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var currentEtFoodListTextWatcher: TextWatcher? = null
        private var currentEtFoodPriceTextWatcher: TextWatcher? = null

        fun bindView(foodInfo: FoodInfo) {
            currentEtFoodListTextWatcher?.let { binding.etFoodList.removeTextChangedListener(it) }
            currentEtFoodPriceTextWatcher?.let { binding.etFoodPrice.removeTextChangedListener(it) }

            binding.etFoodList.setText(foodInfo.foodName.name)
            binding.etFoodPrice.setText(foodInfo.foodPrice.price)

            setBackgroundTint(foodInfo.foodName.isIncomplete, FOOD_NAME)
            setBackgroundTint(foodInfo.foodPrice.isIncomplete, FOOD_Price)
            updateChips(foodInfo)

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
                    foodInfo.foodName.name = s.toString()
                    foodInfo.foodName.isIncomplete = foodInfo.foodName.name.isEmpty()
                    setBackgroundTint(foodInfo.foodName.isIncomplete, FOOD_NAME)
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
                    foodInfo.foodPrice.price = s.toString()
                    foodInfo.foodPrice.isIncomplete = foodInfo.foodPrice.price.isEmpty()
                    setBackgroundTint(foodInfo.foodPrice.isIncomplete, FOOD_Price)
                    onDataChangedListener?.invoke()
                }
            }
            binding.etFoodPrice.addTextChangedListener(currentEtFoodPriceTextWatcher)

            binding.ivAddNameList.setOnClickListener {
                onShowCheckboxDialog?.invoke(foodInfo)
            }

            binding.ivDeleteFoodList.setOnClickListener {
                if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    handleDelete(absoluteAdapterPosition)
                }
            }
        }

        private fun updateChips(foodInfo: FoodInfo) {
            binding.nameChipContainer.removeAllViews()
            foodInfo.name.nameList.forEach { name ->
                addNameChip(name, foodInfo)
            }

            setCardBackgroundColor(foodInfo.name.isIncomplete)
            nameChipCountUpdate(foodInfo)
        }

        private fun addNameChip(name: String, foodInfo: FoodInfo) {
            val chip = Chip(itemView.context).apply {
                setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)))
                chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        context,
                        R.color.colorGreen066E38
                    )
                )
                text = name
                typeface = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.font_zen_maru_gothic),
                    Typeface.NORMAL
                )
                textSize = 16f
                chipCornerRadius = 10f
                isCloseIconVisible = true
                setCloseIconTintResource(R.color.white)
                isClickable = false
                ellipsize = TextUtils.TruncateAt.END
                maxWidth = resources.getDimensionPixelSize(R.dimen.space_100dp)
                setOnCloseIconClickListener {
                    binding.nameChipContainer.removeView(this)
                    foodInfo.name.nameList = foodInfo.name.nameList.filter { it != name }
                    nameChipCountUpdate(foodInfo)
                }
            }
            binding.nameChipContainer.addView(chip)
            foodInfo.name.isIncomplete = false
            setCardBackgroundColor(false)
        }

        private fun nameChipCountUpdate(foodInfo: FoodInfo) {
            val nameList = foodInfo.name.nameList

            with(binding.tvPersonPerFoodCard) {
                isVisible = nameList.isNotEmpty()
                if (isVisible) {
                    text = itemView.context.getString(R.string.person_count, nameList.size)
                }
            }
        }

        private fun setBackgroundTint(isIncomplete: Boolean, tag: String) {
            val color = if (isIncomplete) {
                R.color.red
            } else {
                R.color.colorGreen066E38
            }

            when (tag) {
                FOOD_NAME -> {
                    with(binding.etFoodList) {
                        setHintTextColor(itemView.context.resources.getColor(color))
                        backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(itemView.context, color))
                    }
                }

                FOOD_Price -> {
                    with(binding.etFoodPrice) {
                        setHintTextColor(itemView.context.resources.getColor(color))
                        backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(itemView.context, color))
                    }
                }
            }
        }

        private fun setCardBackgroundColor(isIncomplete: Boolean) {
            val background = if (isIncomplete) {
                R.drawable.incomplete_card_border
            } else {
                R.drawable.rounded_corner_white_bg
            }

            binding.foodCardContainer.background =
                ContextCompat.getDrawable(itemView.context, background)
        }

        private fun handleDelete(position: Int) {
            val foodListText = binding.etFoodList.text.toString()
            val foodPriceText = binding.etFoodPrice.text.toString()

            if (foodListText.isNotBlank() || foodPriceText.isNotBlank() || binding.nameChipContainer.childCount > 0) {
                onClickButtonDelete.invoke(Pair(binding.ivDeleteFoodList, position))
            } else {
                removeItem(position)
            }

        }
    }

    companion object {
        private const val FOOD_NAME = "foodName"
        private const val FOOD_Price = "foodPrice"
    }
}