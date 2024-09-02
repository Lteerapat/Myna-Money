package com.teerapat.moneydivider.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teerapat.moneydivider.addnamelist.NameInfo
import com.teerapat.moneydivider.databinding.NameListCardBinding

class NameListAdapter() : RecyclerView.Adapter<NameListAdapter.NameListViewHolder>() {
    private var nameCardInfo: MutableList<NameInfo> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun addNameListCard(list: MutableList<NameInfo>) {
        this.nameCardInfo = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = NameListCardBinding.inflate(layoutInflater, parent, false)

        return NameListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NameListViewHolder, position: Int) {
        holder.bindView(nameCardInfo[position])
    }

    override fun getItemCount(): Int {
        return nameCardInfo.size
    }

    inner class NameListViewHolder(private val binding: NameListCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(nameCardInfo: NameInfo) {
            binding.etNameList.text = nameCardInfo.name.toEditable()
        }
    }

    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
}