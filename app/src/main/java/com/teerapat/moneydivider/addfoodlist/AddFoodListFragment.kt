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
import com.teerapat.moneydivider.data.FoodInfo
import com.teerapat.moneydivider.data.FoodNameInfo
import com.teerapat.moneydivider.data.FoodPriceInfo
import com.teerapat.moneydivider.data.IncompleteCard
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
import com.teerapat.moneydivider.utils.showDeleteItemConfirmationDialog
import com.teerapat.moneydivider.utils.showTogglePercentageAmountDialog
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


class AddFoodListFragment : Fragment() {
    private lateinit var viewModel: AddFoodListViewModel
    private var _binding: FragmentAddFoodListBinding? = null
    private val binding get() = _binding!!
    private lateinit var foodListAdapter: FoodListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[AddFoodListViewModel::class.java]
        observe()
        getArgumentData()
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

    private fun getArgumentData() {
        arguments?.getParcelableArrayList("nameListBundle", NameInfo::class.java)?.let {
            viewModel.nameListBundle = it
        }
    }

    private fun setupFoodListRecyclerView() {
        foodListAdapter =
            FoodListAdapter(requireContext(), viewModel.nameListBundle)
                .setOnClickButtonDelete {
                    showDeleteItemConfirmationDialog(it.first) { foodListAdapter.removeItem(it.second) }
                }
                .setOnDataChangedListener {
                    calculateTotalAmount()
                }
        binding.rvFoodList.adapter = foodListAdapter
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
            val nameOnlyList = viewModel.nameListBundle.map { it.name }
            val updatedFoodList = existingFoodList.map { foodInfo ->
                val filteredNameChips = foodInfo.name.nameList.filter { nameChip ->
                    nameChip in nameOnlyList
                }
                foodInfo.copy(name = NameChipInfo(filteredNameChips))
            }
            foodListAdapter.setItems(updatedFoodList)
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

        binding.etDiscountAmount.text?.clear()
        binding.etServiceChargeAmount.text?.clear()
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
        binding.etDiscountAmount.addTextChangedListener { discount ->
            if (viewModel.isPercentage && removeCommasAndReturnDouble(discount.toString()) > 100) {
                showAlertOnVScDis(
                    getString(R.string.discount),
                    getString(R.string.percentage_exceeded_alert_message),
                )
                discount?.clear()
            }
            calculateTotalAmount()
        }
        binding.etServiceChargeAmount.addTextChangedListener { serviceCharge ->
            if (viewModel.isPercentage && removeCommasAndReturnDouble(serviceCharge.toString()) > 100) {
                showAlertOnVScDis(
                    getString(R.string.service_charge),
                    getString(R.string.percentage_exceeded_alert_message),
                )
                serviceCharge?.clear()
            }
            calculateTotalAmount()
        }
        binding.etVatAmount.addTextChangedListener { vat ->
            if (viewModel.isPercentage && removeCommasAndReturnDouble(vat.toString()) > 100) {
                showAlertOnVScDis(
                    getString(R.string.vat),
                    getString(R.string.percentage_exceeded_alert_message),
                )
                vat?.clear()
            }
            calculateTotalAmount()
        }
    }

    private fun calculateTotalAmount() {
        var totalAmountBeforeCalculation = 0.0
        var totalAmountAfterDiscount = 0.0
        val totalAmountAfterDiscountAndServiceCharge: Double
        val totalAmountAfterDiscountAndServiceChargeAndVat: Double

        val discountText = binding.etDiscountAmount.text.toString()
        val discount = removeCommasAndReturnDouble(discountText)

        val serviceChargeText = binding.etServiceChargeAmount.text.toString()
        val serviceCharge = removeCommasAndReturnDouble(serviceChargeText)

        val vatText = binding.etVatAmount.text.toString()
        val vat = removeCommasAndReturnDouble(vatText)

        viewModel.saveDiscount(discount)
        viewModel.saveServiceCharge(serviceCharge)
        viewModel.saveVat(vat)

        for (food in foodListAdapter.getFoodList()) {
            totalAmountBeforeCalculation += removeCommasAndReturnDouble(food.foodPrice.price)
        }

        if (!viewModel.isPercentage) {
            if (discount > totalAmountBeforeCalculation) {
                showAlertOnVScDis(
                    getString(R.string.discount),
                    getString(R.string.discount_exceeded_alert_message),
                )
                binding.etDiscountAmount.text?.clear()
            } else {
                totalAmountAfterDiscount = totalAmountBeforeCalculation - discount
            }
            totalAmountAfterDiscountAndServiceCharge = totalAmountAfterDiscount + serviceCharge
            totalAmountAfterDiscountAndServiceChargeAndVat =
                totalAmountAfterDiscountAndServiceCharge + vat

        } else {
            totalAmountAfterDiscount =
                totalAmountBeforeCalculation * (1 - (discount / 100))
            totalAmountAfterDiscountAndServiceCharge =
                totalAmountAfterDiscount * (1 + (serviceCharge / 100))
            totalAmountAfterDiscountAndServiceChargeAndVat =
                totalAmountAfterDiscountAndServiceCharge * (1 + (vat / 100))
        }

        binding.tvTotalAmount.text =
            thousandSeparator(totalAmountAfterDiscountAndServiceChargeAndVat)

        viewModel.saveDiscountFractionBundle(
            calculateDiscountFraction(
                discount,
                totalAmountBeforeCalculation
            )
        )
        viewModel.saveVatFractionBundle(
            calculateVatFraction(
                vat,
                totalAmountAfterDiscountAndServiceCharge
            )
        )
        viewModel.saveServiceChargeFractionBundle(
            calculateServiceChargeFraction(
                serviceCharge,
                totalAmountAfterDiscount
            )
        )
    }

    private fun calculateDiscountFraction(
        discount: Double,
        totalAmountBeforeCalculation: Double
    ): Double {
        return if (viewModel.isPercentage) {
            discount / 100
        } else {
            convertAmountToFraction(discount, totalAmountBeforeCalculation)
        }
    }

    private fun calculateServiceChargeFraction(
        serviceCharge: Double,
        totalAmountAfterDiscount: Double
    ): Double {
        return if (viewModel.isPercentage) {
            serviceCharge / 100
        } else {
            convertAmountToFraction(serviceCharge, totalAmountAfterDiscount)
        }
    }

    private fun calculateVatFraction(
        vat: Double,
        totalAmountAfterDiscountAndServiceCharge: Double
    ): Double {
        return if (viewModel.isPercentage) {
            vat / 100
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

    private fun hasDuplicateNames(foodList: List<FoodInfo>): Boolean {
        val names = foodList.map { it.foodName.name.trim() }
        return names.size != names.toSet().size
    }

    private fun buildBundle(): Bundle {
        val vatScDcBundle = viewModel.vScDcFractionBundle()
        val foodList = viewModel.foodList

        return Bundle().apply {
            putParcelable("vatScDcBundle", vatScDcBundle)
            putParcelableArrayList("foodListBundle", ArrayList(foodList))
        }
    }

    private fun convertAmountToFraction(numerator: Double, denominator: Double): Double {
        if (denominator == 0.0) {
            return 1.0
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
        private const val DECIMAL_PATTERN = "#,##0.00"
        private const val MAX_FOOD_CARD = 20
        private const val ET_FOOD_LIST = "etFoodList"
        private const val ET_FOOD_PRICE = "etFoodPrice"
        private const val FOOD_CARD_CONTAINER = "foodCardContainer"
    }
}