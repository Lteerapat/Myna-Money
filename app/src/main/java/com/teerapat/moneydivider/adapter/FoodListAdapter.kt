package com.teerapat.moneydivider.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teerapat.moneydivider.databinding.FoodListCardBinding

class FoodListAdapter() : RecyclerView.Adapter<FoodListAdapter.ViewHolder>() {
    private var foodList: List<Editable> = listOf()

    @SuppressLint("NotifyDataSetChanged")
    fun addFoodListCard(list: List<Editable>) {
        this.foodList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = FoodListCardBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(foodList[position])
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    inner class ViewHolder(private val binding: FoodListCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(foodName: Editable) {
            binding.etFoodList.text = foodName

        }
    }
}