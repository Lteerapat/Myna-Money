package com.teerapat.moneydivider.addnamelist

import androidx.lifecycle.ViewModel

class AddNameListViewModel : ViewModel() {
    var nameList: MutableList<NameInfo> = mutableListOf()

    fun saveNameList(list: List<NameInfo>) {
        nameList.clear()
        nameList.addAll(list)
    }
}