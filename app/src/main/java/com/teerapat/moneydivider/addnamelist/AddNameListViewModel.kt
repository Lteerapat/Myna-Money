package com.teerapat.moneydivider.addnamelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddNameListViewModel : ViewModel() {
    private val _nameList: MutableLiveData<MutableList<NameInfo>> = MutableLiveData(mutableListOf())
    val nameList: LiveData<MutableList<NameInfo>> = _nameList


    fun saveNameList(list: List<NameInfo>) {
        _nameList.value?.apply {
            clear()
            addAll(list)
            _nameList.value = this
        }
    }
}