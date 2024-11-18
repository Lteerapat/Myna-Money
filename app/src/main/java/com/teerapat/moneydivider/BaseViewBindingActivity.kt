package com.teerapat.moneydivider

import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

abstract class BaseViewBindingActivity<T : ViewBinding> : BaseActivity() {

    private var _binding: T? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = createBinding(layoutInflater)
        _binding?.let {
            setContentView(it.root)
        }
    }

    abstract fun createBinding(inflater: LayoutInflater): T
}