package com.teerapat.moneydivider.addlist

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.teerapat.moneydivider.databinding.FoodListCardBinding
import com.teerapat.moneydivider.utils.DecimalDigitsInputFilter

class FoodListAdapter(private val context: Context) :
    RecyclerView.Adapter<FoodListAdapter.FoodListViewHolder>() {

    private val items = mutableListOf<Pair<String, String>>()
    var onItemChanged: ((Int, String, String) -> Unit)? = null

    inner class FoodListViewHolder(val binding: FoodListCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.etFoodPrice.filters = arrayOf(
                DecimalDigitsInputFilter(7, 2)
            )
            binding.ivDeleteFoodList.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val foodListText = binding.etFoodList.text.toString()
                    val foodPriceText = binding.etFoodPrice.text.toString()

                    if (foodListText.isNotBlank() || foodPriceText.isNotBlank()) {
                        showConfirmationDialog(position)
                    } else {
                        removeItem(position)
                    }
                }
            }

            binding.etFoodList.addTextChangedListener { text ->
                val price = binding.etFoodPrice.text.toString()
                onItemChanged?.invoke(bindingAdapterPosition, text.toString(), price)
            }

            binding.etFoodPrice.addTextChangedListener { text ->
                val name = binding.etFoodList.text.toString()
                onItemChanged?.invoke(bindingAdapterPosition, name, text.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodListViewHolder {
        val binding =
            FoodListCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodListViewHolder, position: Int) {
        val (name, price) = items[position]
        holder.binding.etFoodList.setText(name)
        holder.binding.etFoodPrice.setText(price)
    }

    override fun getItemCount() = items.size

    fun addItem(name: String, price: String) {
        items.add(Pair(name, price))
        notifyItemInserted(items.size - 1)
    }

    private fun removeItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount - position)
        }
    }

    private fun showConfirmationDialog(position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { _, _ ->
                removeItem(position)
            }
            .setNegativeButton("No", null)
            .setCancelable(false)
            .show()
    }
}
