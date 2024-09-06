package com.teerapat.moneydivider.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.teerapat.moneydivider.data.VatScDcBundleInfo
import com.teerapat.moneydivider.databinding.FragmentSummaryBinding

class SummaryFragment : Fragment() {
    private lateinit var viewModel: SummaryViewModel
    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SummaryViewModel::class.java]
        observe()
    }

    private fun observe() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMock()
    }

    private fun setUpMock() {
        val vatScDcBundle = arguments?.getParcelable<VatScDcBundleInfo>("vatScDcBundle")

        vatScDcBundle?.let {
            binding.tvSummaryTotalAmount.text = String.format("%.2f", it.discount)
            binding.vat.text = String.format("%.2f", it.vat)
            binding.sc.text = String.format("%.2f", it.serviceCharge)
        }
    }
}