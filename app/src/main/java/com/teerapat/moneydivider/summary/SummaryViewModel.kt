package com.teerapat.moneydivider.summary

import androidx.lifecycle.ViewModel
import com.teerapat.moneydivider.data.FoodInfo
import com.teerapat.moneydivider.data.VatScDcBundleInfo

class SummaryViewModel : ViewModel() {
    var vatScDcBundleInfo: VatScDcBundleInfo = VatScDcBundleInfo()
    var foodListBundle: MutableList<FoodInfo> = mutableListOf()
}