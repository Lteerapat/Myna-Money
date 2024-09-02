package com.teerapat.moneydivider.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teerapat.moneydivider.addfoodlist.FoodInfo
import com.teerapat.moneydivider.databinding.FoodListCardBinding

class FoodListAdapter() : RecyclerView.Adapter<FoodListAdapter.FoodListViewHolder>() {
    private var foodCardInfo: MutableList<FoodInfo> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun addFoodListCard(list: MutableList<FoodInfo>) {
        this.foodCardInfo = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = FoodListCardBinding.inflate(layoutInflater, parent, false)

        return FoodListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodListViewHolder, position: Int) {
        holder.bindView(foodCardInfo[position])
    }

    override fun getItemCount(): Int {
        return foodCardInfo.size
    }

    inner class FoodListViewHolder(private val binding: FoodListCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(foodCardInfo: FoodInfo) {
            binding.etFoodList.text = foodCardInfo.foodName.toEditable()
        }
    }

    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

}