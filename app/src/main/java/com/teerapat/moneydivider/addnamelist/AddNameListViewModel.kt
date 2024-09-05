package com.teerapat.moneydivider.addnamelist

import androidx.lifecycle.ViewModel
import com.teerapat.moneydivider.data.NameInfo

class AddNameListViewModel : ViewModel() {
    var nameList: MutableList<NameInfo> = mutableListOf()

    fun saveNameList(list: List<NameInfo>) {
        nameList.clear()
        nameList.addAll(list)
    }
}