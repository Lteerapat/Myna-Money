package com.teerapat.moneydivider.addfoodlist

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.adapter.FoodListAdapter
import com.teerapat.moneydivider.addnamelist.IncompleteCard
import com.teerapat.moneydivider.data.FoodInfo
import com.teerapat.moneydivider.data.FoodNameInfo
import com.teerapat.moneydivider.data.FoodPriceInfo
import com.teerapat.moneydivider.data.NameChipInfo
import com.teerapat.moneydivider.data.NameInfo
import com.teerapat.moneydivider.databinding.FragmentAddFoodListBinding
import com.teerapat.moneydivider.utils.openSoftKeyboard
import com.teerapat.moneydivider.utils.showAlertDuplicateNames
import com.teerapat.moneydivider.utils.showAlertOnIncompleteCard
import com.teerapat.moneydivider.utils.showAlertOnVScDis
import com.teerapat.moneydivider.utils.showAlertOverLimitItemCard
import com.teerapat.moneydivider.utils.showAlertZeroCardList
import com.teerapat.moneydivider.utils.showContinueDialog
import com.teerapat.moneydivider.utils.showTogglePercentageAmountDialog
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


class AddFoodListFragment : Fragment() {
    private lateinit var viewModel: AddFoodListViewModel
    private var _binding: FragmentAddFoodListBinding? = null
    private val binding get() = _binding!!
    private lateinit var foodListAdapter: FoodListAdapter

    private val nameList: MutableList<NameInfo> by lazy {
        arguments?.getParcelableArrayList<NameInfo>("nameList")?.map {
            NameInfo(it.name, it.isChecked)
        }?.toMutableList() ?: mutableListOf()
    }
    private val nameStateMap = mutableMapOf<Int, List<NameInfo>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[AddFoodListViewModel::class.java]
        observe()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFoodListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFoodListRecyclerView()
        setUpAddButton()
        setUpVScDisIsPercentage()
        setUpToggleListeners()
        setUpAmountOfVScDis()
        setUpNextButton()
        loadInitialData()
    }

    private fun observe() {
    }

    private fun setupFoodListRecyclerView() {
        foodListAdapter = FoodListAdapter(requireContext(), nameList)
        binding.rvFoodList.adapter = foodListAdapter
        foodListAdapter.setOnDataChangedListener {
            calculateTotalAmount()
        }
    }

    private fun setUpAddButton() {
        binding.btnAddFoodList.setOnClickListener {
            if (foodListAdapter.itemCount >= MAX_FOOD_CARD) {
                showAlertOverLimitItemCard(MAX_FOOD_CARD)
                return@setOnClickListener
            }
            foodListAdapter.addItem(FoodInfo(FoodNameInfo(), FoodPriceInfo(), NameChipInfo()))
            focusOnCard(position = foodListAdapter.itemCount - 1, incompleteField = ET_FOOD_LIST)
        }
    }

    private fun loadInitialData() {
        val existingFoodList = viewModel.foodList
        val existingDiscount = viewModel.discount
        val existingServiceCharge = viewModel.serviceCharge
        val existingVat = viewModel.vat

        if (existingFoodList.isNotEmpty()) {
            foodListAdapter.setItems(existingFoodList)
        } else {
            foodListAdapter.addItem(FoodInfo(FoodNameInfo(), FoodPriceInfo(), NameChipInfo()))
        }

        if (existingDiscount.isNotEmpty()) binding.etDiscountAmount.setText(existingDiscount)
        if (existingServiceCharge.isNotEmpty()) binding.etServiceChargeAmount.setText(
            existingServiceCharge
        )
        if (existingVat.isNotEmpty()) binding.etVatAmount.setText(existingVat)
    }

    private fun setUpToggleListeners() {
        binding.btnPercentageToggle.setOnClickListener {
            binding.btnPercentageToggle.isEnabled = false
            showTogglePercentageAmountDialog(
                binding.btnPercentageToggle,
                onPercentageSelected = {
                    if (!viewModel.isPercentage) {
                        viewModel.setIsPercentage(true)
                        updateAmountOfVScDis()
                    }
                },
                onAmountSelected = {
                    if (viewModel.isPercentage) {
                        viewModel.setIsPercentage(false)
                        updateAmountOfVScDis()
                    }
                }
            )
        }
    }

    private fun updateAmountOfVScDis() {
        setUpVScDisIsPercentage()

        binding.etServiceChargeAmount.text?.clear()
        binding.etDiscountAmount.text?.clear()
        binding.etVatAmount.text?.clear()
    }


    private fun setUpVScDisIsPercentage() {
        val symbol =
            if (viewModel.isPercentage) getString(R.string.percentage_sign) else getString(R.string.baht_sign)

        binding.tvServiceChargePercentage.text = symbol
        binding.tvDiscountPercentage.text = symbol
        binding.tvVatPercentage.text = symbol
    }

    private fun setUpAmountOfVScDis() {
        binding.etDiscountAmount.addTextChangedListener { calculateTotalAmount() }
        binding.etServiceChargeAmount.addTextChangedListener { calculateTotalAmount() }
        binding.etVatAmount.addTextChangedListener { calculateTotalAmount() }
    }

    private fun calculateTotalAmount() {
        var totalAmountBeforeCalculation = 0.0
        val discountText = binding.etDiscountAmount.text.toString()
        val discount = removeCommasAndReturnDouble(discountText)

        val serviceChargeText = binding.etServiceChargeAmount.text.toString()
        val serviceCharge = removeCommasAndReturnDouble(serviceChargeText)

        val vatText = binding.etVatAmount.text.toString()
        val vat = removeCommasAndReturnDouble(vatText)

        viewModel.saveDiscount(discount)
        viewModel.saveServiceCharge(serviceCharge)
        viewModel.saveVat(vat)
        viewModel.saveVatScDcBundle(discount, serviceCharge, vat)

        for (food in foodListAdapter.getFoodList()) {
            totalAmountBeforeCalculation += removeCommasAndReturnDouble(food.foodPrice.price)
        }

        if (!viewModel.isPercentage) {
            if (discount > totalAmountBeforeCalculation) {
                showAlertOnVScDis(
                    getString(R.string.discount),
                    getString(R.string.discount_exceeded_alert_message),
                    discountField = binding.etDiscountAmount
                )
            } else {
                totalAmountBeforeCalculation -= discount
            }

            totalAmountBeforeCalculation += serviceCharge
            totalAmountBeforeCalculation += vat
        } else {
            val totalAmountAfterDiscount =
                totalAmountBeforeCalculation * (1 - calculateDiscountFraction(
                    discount,
                    totalAmountBeforeCalculation
                ))

            val totalAmountAfterDiscountAndServiceCharge =
                totalAmountAfterDiscount * (1 + calculateServiceChargeFraction(
                    serviceCharge,
                    totalAmountAfterDiscount
                ))

            val totalAmountAfterDiscountAndServiceChargeAndVat =
                totalAmountAfterDiscountAndServiceCharge * (1 + calculateVatFraction(
                    vat,
                    totalAmountAfterDiscountAndServiceCharge
                ))

//            viewModel.saveVatScDcBundle(
//                dc = totalAmountBeforeCalculation - totalAmountAfterDiscount,
//                sc = totalAmountAfterDiscount * calculateServiceChargeFraction(
//                    serviceCharge,
//                    totalAmountAfterDiscount
//                ),
//                vat = totalAmountAfterDiscountAndServiceCharge * calculateVatFraction(
//                    vat,
//                    totalAmountAfterDiscountAndServiceCharge
//                )
//            )

            totalAmountBeforeCalculation = totalAmountAfterDiscountAndServiceChargeAndVat
        }

        binding.tvTotalAmount.text =
            thousandSeparator(totalAmountBeforeCalculation)
        viewModel.saveTotalAmount(totalAmountBeforeCalculation)
    }


    private fun calculateDiscountFraction(
        discount: Double,
        totalAmountBeforeCalculation: Double
    ): Double {
        return if (viewModel.isPercentage) {
            if (discount > 100) {
                showAlertOnVScDis(
                    getString(R.string.discount),
                    getString(R.string.percentage_exceeded_alert_message),
                    discountField = binding.etDiscountAmount
                )

                0.0
            } else {
                discount / 100
            }
        } else {
            convertAmountToFraction(discount, totalAmountBeforeCalculation)
        }
    }

    private fun calculateServiceChargeFraction(
        serviceCharge: Double,
        totalAmountAfterDiscount: Double
    ): Double {
        return if (viewModel.isPercentage) {
            if (serviceCharge > 100) {
                showAlertOnVScDis(
                    getString(R.string.service_charge),
                    getString(R.string.percentage_exceeded_alert_message),
                    serviceChargeField = binding.etServiceChargeAmount,
                )

                0.0
            } else {
                serviceCharge / 100
            }
        } else {
            convertAmountToFraction(serviceCharge, totalAmountAfterDiscount)
        }
    }

    private fun calculateVatFraction(
        vat: Double,
        totalAmountAfterDiscountAndServiceCharge: Double
    ): Double {
        return if (viewModel.isPercentage) {
            if (vat > 100) {
                showAlertOnVScDis(
                    getString(R.string.vat),
                    getString(R.string.percentage_exceeded_alert_message),
                    vatField = binding.etVatAmount,
                )

                0.0
            } else {
                vat / 100
            }
        } else {
            convertAmountToFraction(vat, totalAmountAfterDiscountAndServiceCharge)
        }
    }

    private fun setUpNextButton() {
        binding.btnNext.setOnClickListener {
            val btnNext = binding.btnNext
            btnNext.isEnabled = false
            val foodList = foodListAdapter.getFoodList()
            val incompleteCard = findFirstIncompleteCard(foodList)

            when {
                foodList.isEmpty() -> {
                    showAlertZeroCardList {
                        foodListAdapter.addItem(
                            FoodInfo(
                                FoodNameInfo(isIncomplete = true),
                                FoodPriceInfo(),
                                NameChipInfo()
                            )
                        )
                        focusOnCard(
                            position = 0,
                            isIncompleteCard = true,
                            incompleteField = ET_FOOD_LIST
                        )
                        btnNext.isEnabled = true
                    }
                }

                incompleteCard != null -> {
                    showAlertOnIncompleteCard(incompleteCard.message) {
                        focusOnCard(
                            position = incompleteCard.position,
                            isIncompleteCard = true,
                            incompleteField = incompleteCard.incompleteField
                        )
                        btnNext.isEnabled = true
                    }
                }

                hasDuplicateNames(foodList) -> {
                    showAlertDuplicateNames { btnNext.isEnabled = true }
                }

                else -> {
                    showContinueDialog(binding.btnNext) {
                        viewModel.saveFoodList(foodListAdapter.getFoodList())
                        findNavController().navigate(
                            R.id.action_addFoodListFragment_to_summaryFragment,
                            buildBundle()
                        )
                    }
                }
            }
        }
    }

    private fun hasDuplicateNames(nameList: List<FoodInfo>): Boolean {
        val names = nameList.map { it.foodName.name.trim() }
        return names.size != names.toSet().size
    }

    private fun buildBundle(): Bundle {
        val vatScDcBundle = viewModel.vatScDcBundle
        val foodList = viewModel.foodList

        return Bundle().apply {
            putParcelable("vatScDcBundle", vatScDcBundle)
            putParcelableArrayList("foodList", ArrayList(foodList))
        }
    }

    private fun convertAmountToFraction(numerator: Double, denominator: Double): Double {
        if (denominator == 0.0) {
            throw IllegalArgumentException("Denominator cannot be zero")
        }

        return numerator / denominator
    }

    private fun focusOnCard(
        position: Int,
        isIncompleteCard: Boolean = false,
        incompleteField: String
    ) {
        binding.rvFoodList.scrollToPosition(position)
        binding.rvFoodList.post {
            val viewHolder =
                binding.rvFoodList.findViewHolderForAdapterPosition(position) as? FoodListAdapter.FoodListViewHolder
            val etFoodList = viewHolder?.binding?.etFoodList
            val etFoodPrice = viewHolder?.binding?.etFoodPrice
            val foodCardContainer = viewHolder?.binding?.foodCardContainer

            when (incompleteField) {
                ET_FOOD_LIST -> {
                    etFoodList?.let {
                        openSoftKeyboard(requireContext(), it)
                    }
                    if (isIncompleteCard) {
                        etFoodList?.text?.clear()
                        etFoodList?.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.red)
                        )
                    }
                }

                ET_FOOD_PRICE -> {
                    etFoodPrice?.let {
                        openSoftKeyboard(requireContext(), it)
                    }
                    if (isIncompleteCard) {
                        etFoodPrice?.text?.clear()
                        etFoodPrice?.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.red)
                        )
                    }
                }

                FOOD_CARD_CONTAINER -> {
                    foodCardContainer?.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.incomplete_card_border
                    )
                }
            }
        }
    }

    private fun findFirstIncompleteCard(foodList: List<FoodInfo>): IncompleteCard? {
        foodList.forEachIndexed { index, foodInfo ->
            val foodName = foodInfo.foodName.name.trim()
            val foodPrice = foodInfo.foodPrice.price
            val name = foodInfo.name.nameList

            when {
                !foodName.matches(REGEX) -> {
                    foodList[index].foodName.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = getString(R.string.incomplete_card_letter_or_num_message),
                        incompleteField = ET_FOOD_LIST
                    )
                }

                foodName.isBlank() -> {
                    foodList[index].foodName.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = getString(R.string.incomplete_card_empty_message),
                        incompleteField = ET_FOOD_LIST
                    )
                }

                foodPrice.isBlank() -> {
                    foodList[index].foodPrice.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = getString(R.string.incomplete_card_empty_message),
                        incompleteField = ET_FOOD_PRICE
                    )
                }

                foodName.matches(NUM_REGEX) -> {
                    foodList[index].foodName.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = getString(R.string.incomplete_card_num_only_message),
                        incompleteField = ET_FOOD_LIST
                    )
                }

                removeCommasAndReturnDouble(foodPrice) == 0.0 -> {
                    foodList[index].foodPrice.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = getString(R.string.incomplete_card_zero_message),
                        incompleteField = ET_FOOD_PRICE
                    )
                }

                name.isEmpty() -> {
                    foodList[index].name.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = getString(R.string.incomplete_card_zero_chip_message),
                        incompleteField = FOOD_CARD_CONTAINER
                    )
                }

                else -> {
                    getString(R.string.incomplete_item)
                }
            }
        }

        return null
    }

    private fun thousandSeparator(amount: Double): String {
        val decimalFormat = DecimalFormat(DECIMAL_PATTERN, DecimalFormatSymbols(Locale.US))
        return "${decimalFormat.format(amount)} ฿"
    }

    private fun removeCommasAndReturnDouble(amount: String): Double {
        return if (amount.isBlank()) {
            0.0
        } else {
            amount.replace(",", "").toDoubleOrNull() ?: 0.0
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveFoodList(foodListAdapter.getFoodList())
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveFoodList(foodListAdapter.getFoodList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val REGEX = Regex("^[A-Za-z0-9ก-๏ ]*$")
        private val NUM_REGEX = Regex("[0-9]*$")
        private const val DECIMAL_PATTERN = "#,###.##"
        private const val MAX_FOOD_CARD = 50
        private const val ET_FOOD_LIST = "etFoodList"
        private const val ET_FOOD_PRICE = "etFoodPrice"
        private const val FOOD_CARD_CONTAINER = "foodCardContainer"
    }
}