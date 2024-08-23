package com.teerapat.moneydivider.addnamelist

import androidx.lifecycle.ViewModel
import com.teerapat.moneydivider.addlist.AddNameModal

class AddNameListViewModel : ViewModel() {
    var nameList: MutableList<AddNameModal> = mutableListOf()

    fun saveNameList(list: List<AddNameModal>) {
        nameList.clear()
        nameList.addAll(list)
    }
}