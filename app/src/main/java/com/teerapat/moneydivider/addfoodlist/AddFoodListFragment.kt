package com.teerapat.moneydivider.addfoodlist

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.teerapat.moneydivider.BaseFragment
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.data.FoodInfo
import com.teerapat.moneydivider.data.FoodNameInfo
import com.teerapat.moneydivider.data.FoodPriceInfo
import com.teerapat.moneydivider.data.IncompleteCard
import com.teerapat.moneydivider.data.NameChipInfo
import com.teerapat.moneydivider.data.NameInfo
import com.teerapat.moneydivider.databinding.FragmentAddFoodListBinding
import com.teerapat.moneydivider.utils.openSoftKeyboard
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class AddFoodListFragment : BaseFragment() {
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
        setUpGroupAddButton()
        setUpVScDisIsPercentage()
        setUpToggleListeners()
        setUpAmountOfVScDis()
        setUpNextButton()
        setUpBackButton()
        loadInitialData()
    }

    private fun observe() {
    }

    private fun getArgumentData() {
        arguments?.getParcelableArrayList("nameListBundle", NameInfo::class.java)?.let {
            viewModel.nameListBundle = it
        }
    }

    private fun setUpBackButton() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupFoodListRecyclerView() {
        foodListAdapter =
            FoodListAdapter()
                .setOnClickButtonDelete { pair ->
                    dialogAble.show(
                        title = R.string.confirm_delete_title,
                        description = R.string.confirm_delete_message,
                        onPositiveButtonClick = { foodListAdapter.removeItem(pair.second) }
                    )
                }
                .setOnDataChangedListener {
                    calculateTotalAmount()
                }
                .setOnShowCheckboxDialog { foodInfo ->
                    val isCheckedArray = BooleanArray(viewModel.nameListBundle.size) { index ->
                        viewModel.nameListBundle[index].name in foodInfo.name.nameList
                    }
                    val names = viewModel.nameListBundle.map { it.name.trim() }.toTypedArray()

                    dialogAble.showNameSelectionDialog(
                        names = names,
                        isCheckedArray = isCheckedArray,
                        onPositiveButtonClick = {
                            foodInfo.name.nameList =
                                names.filterIndexed { index, _ -> isCheckedArray[index] }
                            foodListAdapter.updateChips(foodInfo)
                        },
                        onNegativeButtonClick = {
                            isCheckedArray.fill(true)
                        }
                    )
                }
        binding.rvFoodList.adapter = foodListAdapter
    }

    private fun setUpAddButton() {
        binding.btnAddFoodList.setOnClickListener {
            if (foodListAdapter.itemCount >= MAX_FOOD_CARD) {
                dialogAble.show(
                    title = R.string.item_limit_exceeded_title,
                    description = R.string.item_limit_exceeded_message,
                    descriptionArg = MAX_FOOD_CARD,
                    isShowPositiveButton = false
                )
                return@setOnClickListener
            }
            foodListAdapter.addItem(FoodInfo(FoodNameInfo(), FoodPriceInfo(), NameChipInfo()))
            focusOnCard(position = foodListAdapter.itemCount - 1, incompleteField = ET_FOOD_LIST)
        }
    }

    private fun setUpGroupAddButton() {
        binding.btnGroupAddFoodList.setOnClickListener {
            dialogAble.showSingleItemSelectionDialog { _, selectedOptionPosition ->
                val itemCountToAdd = when (selectedOptionPosition) {
                    0 -> 5
                    1 -> 10
                    2 -> 15
                    3 -> 20
                    else -> 0
                }

                val itemsToAdd = minOf(itemCountToAdd, MAX_FOOD_CARD - foodListAdapter.itemCount)

                repeat(itemsToAdd) {
                    foodListAdapter.addItem(
                        FoodInfo(
                            FoodNameInfo(),
                            FoodPriceInfo(),
                            NameChipInfo()
                        )
                    )
                }
            }
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
            val toggleOption =
                if (viewModel.isPercentage) getString(R.string.baht_btn) else getString(R.string.percentage_btn)

            dialogAble.showSingleItemSelectionDialog(arrayOf(toggleOption)) { _, _ ->
                viewModel.setIsPercentage(!viewModel.isPercentage)
                updateAmountOfVScDis()
            }
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

        binding.btnPercentageToggle.text =
            if (viewModel.isPercentage) {
                getString(R.string.percentage_btn).uppercase()
            } else {
                getString(R.string.baht_btn).uppercase()
            }

        binding.tvServiceChargePercentage.text = symbol
        binding.tvDiscountPercentage.text = symbol
        binding.tvVatPercentage.text = symbol
    }

    private fun setUpAmountOfVScDis() {
        binding.etDiscountAmount.addTextChangedListener { discount ->
            if (viewModel.isPercentage && removeCommasAndReturnDouble(discount.toString()) > 100) {
                dialogAble.show(
                    title = R.string.discount,
                    description = R.string.percentage_exceeded_alert_message,
                    isShowPositiveButton = false
                )
                discount?.clear()
            }
            calculateTotalAmount()
        }
        binding.etServiceChargeAmount.addTextChangedListener { serviceCharge ->
            if (viewModel.isPercentage && removeCommasAndReturnDouble(serviceCharge.toString()) > 100) {
                dialogAble.show(
                    title = R.string.service_charge,
                    description = R.string.percentage_exceeded_alert_message,
                    isShowPositiveButton = false
                )
                serviceCharge?.clear()
            }
            calculateTotalAmount()
        }
        binding.etVatAmount.addTextChangedListener { vat ->
            if (viewModel.isPercentage && removeCommasAndReturnDouble(vat.toString()) > 100) {
                dialogAble.show(
                    title = R.string.vat,
                    description = R.string.percentage_exceeded_alert_message,
                    isShowPositiveButton = false
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
                dialogAble.show(
                    title = R.string.discount,
                    description = R.string.discount_exceeded_alert_message,
                    isShowPositiveButton = false
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
            val foodList = foodListAdapter.getFoodList()
            val incompleteCard = findFirstIncompleteCard(foodList)

            when {
                foodList.isEmpty() -> {
                    dialogAble.show(
                        title = R.string.incomplete_item,
                        description = R.string.incomplete_card_at_least_1_message,
                        isShowPositiveButton = false,
                        onDismissListener = {
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
                        }
                    )
                }

                incompleteCard != null -> {
                    dialogAble.show(
                        title = R.string.incomplete_item,
                        description = incompleteCard.message,
                        isShowPositiveButton = false,
                        onDismissListener = {
                            focusOnCard(
                                incompleteCard.position,
                                isIncompleteCard = true,
                                incompleteField = incompleteCard.incompleteField
                            )
                        }
                    )
                }

                hasDuplicateNames(foodList) -> {
                    dialogAble.show(
                        title = R.string.duplicate_name_alert_title,
                        description = R.string.duplicate_name_alert_message,
                        isShowPositiveButton = false
                    )
                }

                else -> {
                    dialogAble.show(
                        title = R.string.next_btn_alert_title,
                        description = R.string.next_btn_alert_message,
                        titleBackground = R.drawable.rounded_top_corner_green_dialog,
                        onPositiveButtonClick = {
                            viewModel.saveFoodList(foodListAdapter.getFoodList())
                            findNavController().navigate(
                                R.id.action_addFoodListFragment_to_summaryFragment,
                                buildBundle()
                            )
                        }
                    )
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
                    etFoodList?.openSoftKeyboard()
                    if (isIncompleteCard) {
                        etFoodList?.let {
                            with(it) {
                                text?.clear()
                                setHintTextColor(resources.getColor(R.color.red))
                                backgroundTintList = ColorStateList.valueOf(
                                    ContextCompat.getColor(requireContext(), R.color.red)
                                )
                            }
                        }
                    }
                }

                ET_FOOD_PRICE -> {
                    etFoodPrice?.openSoftKeyboard()
                    if (isIncompleteCard) {
                        etFoodPrice?.let {
                            with(it) {
                                text?.clear()
                                setHintTextColor(resources.getColor(R.color.red))
                                backgroundTintList = ColorStateList.valueOf(
                                    ContextCompat.getColor(requireContext(), R.color.red)
                                )
                            }
                        }
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
                        message = R.string.incomplete_card_letter_or_num_message,
                        incompleteField = ET_FOOD_LIST
                    )
                }

                foodName.isBlank() -> {
                    foodList[index].foodName.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = R.string.incomplete_card_empty_message,
                        incompleteField = ET_FOOD_LIST
                    )
                }

                foodPrice.isBlank() -> {
                    foodList[index].foodPrice.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = R.string.incomplete_card_empty_message,
                        incompleteField = ET_FOOD_PRICE
                    )
                }

                foodName.matches(NUM_REGEX) -> {
                    foodList[index].foodName.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = R.string.incomplete_card_num_only_message,
                        incompleteField = ET_FOOD_LIST
                    )
                }

                removeCommasAndReturnDouble(foodPrice) == 0.0 -> {
                    foodList[index].foodPrice.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = R.string.incomplete_card_zero_message,
                        incompleteField = ET_FOOD_PRICE
                    )
                }

                name.isEmpty() -> {
                    foodList[index].name.isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = R.string.incomplete_card_zero_chip_message,
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