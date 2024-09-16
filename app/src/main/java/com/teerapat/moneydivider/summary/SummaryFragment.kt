package com.teerapat.moneydivider.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.data.FoodInfo
import com.teerapat.moneydivider.data.SummaryFoodItemInfo
import com.teerapat.moneydivider.data.SummaryInfo
import com.teerapat.moneydivider.data.VatScDcBundleInfo
import com.teerapat.moneydivider.databinding.FragmentSummaryBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class SummaryFragment : Fragment() {
    private lateinit var viewModel: SummaryViewModel
    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!
    private lateinit var summaryAdapter: SummaryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SummaryViewModel::class.java]
        observe()
        getArgumentData()
    }

    private fun observe() {
    }

    private fun getArgumentData() {
        arguments?.getParcelable("vatScDcBundle", VatScDcBundleInfo::class.java)?.let {
            viewModel.vatScDcBundleInfo = it
        }
        arguments?.getParcelableArrayList("foodListBundle", FoodInfo::class.java)?.let {
            viewModel.foodListBundle = it
        }
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

        setupSummaryRecyclerView()
        setUpInitialData()
        setUpBackButton()
        setUpShareButton()
    }

    private fun setupSummaryRecyclerView() {
        summaryAdapter = SummaryAdapter()
        binding.rvSummary.adapter = summaryAdapter
    }

    private fun setUpInitialData() {
        val summaryInfoList = setUpSummaryInfo(viewModel.foodListBundle)

        summaryAdapter.setItems(summaryInfoList)

        val totalAmount = summaryInfoList.sumOf { it.totalAmountPerName }

        binding.tvSummaryTotalAmount.text =
            getString(R.string.amount_format, thousandSeparator(totalAmount))
    }

    private fun setUpSummaryInfo(foodList: List<FoodInfo>): List<SummaryInfo> {
        val summaryInfoList = mutableListOf<SummaryInfo>()

        val groupByNames = foodList.flatMap { foodInfo ->
            foodInfo.name.nameList.map { name ->
                name to foodInfo
            }
        }.groupBy({ it.first }, { it.second })

        for ((name, foodInfos) in groupByNames) {
            val summaryFoodItemInfoList = foodInfos.map { foodInfo ->
                val numberOfPeople = foodInfo.name.nameList.size
                val dividedPrice =
                    removeCommasAndReturnDouble(foodInfo.foodPrice.price) / numberOfPeople

                SummaryFoodItemInfo(
                    foodName = foodInfo.foodName.name,
                    price = dividedPrice
                )
            }.toMutableList()

            val rawTotalAmountPerName =
                summaryFoodItemInfoList.sumOf { it.price }
            val totalAmountPerNameAfterDiscount =
                rawTotalAmountPerName * (1 - viewModel.vatScDcBundleInfo.discount)
            val totalAmountPerNameAfterDiscountAndServiceCharge =
                totalAmountPerNameAfterDiscount * (1 + viewModel.vatScDcBundleInfo.serviceCharge)

            if (totalAmountPerNameAfterDiscount * viewModel.vatScDcBundleInfo.serviceCharge > 0) {
                summaryFoodItemInfoList.add(
                    SummaryFoodItemInfo(
                        foodName = getString(R.string.service_charge),
                        price = totalAmountPerNameAfterDiscount * viewModel.vatScDcBundleInfo.serviceCharge
                    )
                )
            }

            if (totalAmountPerNameAfterDiscountAndServiceCharge * viewModel.vatScDcBundleInfo.vat > 0) {
                summaryFoodItemInfoList.add(
                    SummaryFoodItemInfo(
                        foodName = getString(R.string.vat),
                        price = totalAmountPerNameAfterDiscountAndServiceCharge * viewModel.vatScDcBundleInfo.vat
                    )
                )
            }

            if (viewModel.vatScDcBundleInfo.discount > 0) {
                summaryFoodItemInfoList.add(
                    SummaryFoodItemInfo(
                        foodName = getString(R.string.discount),
                        price = totalAmountPerNameAfterDiscount - rawTotalAmountPerName
                    )
                )
            }

            val totalAmountPerName =
                summaryFoodItemInfoList.sumOf { it.price }

            val summaryInfo = SummaryInfo(
                name = name,
                totalAmountPerName = totalAmountPerName,
                summaryFoodItemInfo = summaryFoodItemInfoList
            )

            summaryInfoList.add(summaryInfo)
        }

        return summaryInfoList
    }

    private fun setUpBackButton() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setUpShareButton() {
        binding.ivShareBtn.setOnClickListener {
            binding.ivShareBtn.isEnabled = false
            val summaryInfoList = setUpSummaryInfo(viewModel.foodListBundle)
            val shareText = generateShareText(summaryInfoList)
            shareSummary(shareText)

            binding.ivShareBtn.postDelayed({
                binding.ivShareBtn.isEnabled = true
            }, 500)
        }
    }

    private fun generateShareText(summaryInfoList: List<SummaryInfo>): String {
        val header = getString(
            R.string.share_text_total,
            thousandSeparator(summaryInfoList.sumOf { it.totalAmountPerName })
        )
        val body = getString(R.string.share_text_price_per_name)
        val footer = summaryInfoList.joinToString("\n") { summaryInfo ->
            "${summaryInfo.name} ${thousandSeparator(summaryInfo.totalAmountPerName)}"
        }

        return header + body + footer
    }

    private fun shareSummary(shareText: String) {
        val shareIntent = ShareCompat.IntentBuilder(requireContext())
            .setType("text/plain")
            .setChooserTitle("Share summary with:")
            .setText(shareText)
            .createChooserIntent()

        startActivity(shareIntent)
    }

    private fun removeCommasAndReturnDouble(amount: String): Double {
        return if (amount.isBlank()) {
            0.0
        } else {
            amount.replace(",", "").toDoubleOrNull() ?: 0.0
        }
    }

    private fun thousandSeparator(amount: Double): String {
        val decimalFormat = DecimalFormat(
            DECIMAL_PATTERN, DecimalFormatSymbols(
                Locale.US
            )
        )
        return decimalFormat.format(amount)
    }

    companion object {
        private const val DECIMAL_PATTERN = "#,##0.00"
    }
}