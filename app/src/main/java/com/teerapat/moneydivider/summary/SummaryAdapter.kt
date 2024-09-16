package com.teerapat.moneydivider.summary

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.data.SummaryInfo
import com.teerapat.moneydivider.databinding.SummaryCardBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


class SummaryAdapter : RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder>() {
    private val summaryInfoList = mutableListOf<SummaryInfo>()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<SummaryInfo>) {
        summaryInfoList.clear()
        summaryInfoList.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = SummaryCardBinding.inflate(layoutInflater, parent, false)

        return SummaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        holder.apply {
            bindView(summaryInfoList[position])
        }
    }

    override fun getItemCount(): Int {
        return summaryInfoList.size
    }

    inner class SummaryViewHolder(private val binding: SummaryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(summaryInfo: SummaryInfo) {
            binding.tvName.text = summaryInfo.name
            binding.tvTotalPayAmountPerPerson.text = itemView.context.getString(
                R.string.amount_format,
                thousandSeparator(summaryInfo.totalAmountPerName)
            )

            val summaryCardAdapter = SummaryCardAdapter(summaryInfo.summaryFoodItemInfo)
            binding.rvPriceSummary.adapter = summaryCardAdapter
        }
    }

    private fun thousandSeparator(amount: Double): String {
        val decimalFormat = DecimalFormat(
            DECIMAL_PATTERN, DecimalFormatSymbols(
                Locale.US
            )
        )
        return decimalFormat.format(amount)
    }

    companion object {
        private const val DECIMAL_PATTERN = "#,##0.00"
    }
}