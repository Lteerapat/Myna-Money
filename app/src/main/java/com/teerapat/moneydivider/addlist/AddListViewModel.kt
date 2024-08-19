package com.teerapat.moneydivider.addlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent

class AddListViewModel : ViewModel() {
    private val _foodListItems = LiveEvent<FoodListItem>()
    val foodListItems: LiveData<FoodListItem> get() = _foodListItems

    private val _serviceChargeAmount = LiveEvent<Double>()
    val serviceChargeAmount: LiveData<Double>
        get() = _serviceChargeAmount

    private val _vatAmount = LiveEvent<Double>()
    val vatAmount: LiveData<Double>
        get() = _vatAmount

    private val _discountAmount = LiveEvent<Double>()
    val discountAmount: LiveData<Double>
        get() = _discountAmount

//    fun addFoodListItem(item: FoodListItem) {
//        val currentList = _foodListItems.value ?: emptyList<FoodListItem>()
//        _foodListItems.value = currentList + item
//    }
//
//    fun removeFoodListItem(item: FoodListItem) {
//        val currentList = _foodListItems.value ?: emptyList<FoodListItem>()
//        _foodListItems.value = currentList - item
//    }
}

