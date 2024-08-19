package com.teerapat.moneydivider.confirm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.teerapat.moneydivider.R

class ConfirmFragment : Fragment() {

    companion object {
        fun newInstance() = ConfirmFragment()
    }

    private lateinit var viewModel: ConfirmViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_confirm, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ConfirmViewModel::class.java)
        // TODO: Use the ViewModel
    }

}