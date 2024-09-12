package com.teerapat.moneydivider.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.data.SummaryFoodItemInfo
import com.teerapat.moneydivider.databinding.SummaryFoodItemCardBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

class SummaryCardAdapter(private val foodItemList: List<SummaryFoodItemInfo>) :
    RecyclerView.Adapter<SummaryCardAdapter.FoodItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = SummaryFoodItemCardBinding.inflate(layoutInflater, parent, false)

        return FoodItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
        holder.bindView(foodItemList[position])
    }

    override fun getItemCount(): Int {
        return foodItemList.size
    }

    inner class FoodItemViewHolder(private val binding: SummaryFoodItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(foodItem: SummaryFoodItemInfo) {
            binding.tvSummaryFoodName.text = foodItem.foodName.trim()
            if (foodItem.foodName == DISCOUNT) {
                binding.tvSummaryFoodPrice.text = itemView.context.getString(
                    R.string.negative_amount_format,
                    thousandSeparator(foodItem.price)
                )
            } else {
                binding.tvSummaryFoodPrice.text = itemView.context.getString(
                    R.string.amount_format,
                    thousandSeparator(foodItem.price)
                )
            }
        }
    }

    private fun thousandSeparator(amount: Double): String {
        val decimalFormat = DecimalFormat(
            DECIMAL_PATTERN, DecimalFormatSymbols(
                Locale.US
            )
        )
        return decimalFormat.format(abs(amount))
    }

    companion object {
        private const val DECIMAL_PATTERN = "#,##0.00"
        private const val DISCOUNT = "Discount"
    }
}