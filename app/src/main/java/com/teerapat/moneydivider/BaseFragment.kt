package com.teerapat.moneydivider

import androidx.fragment.app.Fragment
import com.teerapat.moneydivider.dialog.Dialog
import com.teerapat.moneydivider.dialog.DialogAble

open class BaseFragment : Fragment() {
    val dialogAble: DialogAble by lazy {
        Dialog(requireActivity())
    }
}