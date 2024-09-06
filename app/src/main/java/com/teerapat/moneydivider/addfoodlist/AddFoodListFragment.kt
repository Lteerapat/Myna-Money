package com.teerapat.moneydivider.addfoodlist

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.adapter.FoodListAdapter
import com.teerapat.moneydivider.addnamelist.IncompleteCard
import com.teerapat.moneydivider.data.FoodInfo
import com.teerapat.moneydivider.data.NameInfo
import com.teerapat.moneydivider.databinding.FoodListCardBinding
import com.teerapat.moneydivider.databinding.FragmentAddFoodListBinding
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

    private val nameList = arguments?.getParcelableArrayList<NameInfo>("nameList")?.map {
        NameInfo(it.name, it.isChecked)
    }?.toMutableList() ?: mutableListOf()
    private val nameStateMap = mutableMapOf<Int, List<NameInfo>>()
    private var cardIndexCounter = 0

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
//        setUpFoodListCard()
        setUpVScDisIsPercentage()
        setUpToggleListeners()
        setUpAmountOfVScDis()
        setUpNextButton()
        loadInitialData()
    }

    private fun observe() {
    }

    private fun setupFoodListRecyclerView() {
        foodListAdapter = FoodListAdapter(requireContext())
        binding.rvFoodList.adapter = foodListAdapter
        foodListAdapter.setOnDataChangedListener {
            calculateTotalAmount()
        }
    }

//    private fun setUpFoodListCard() {
////        for (nameModal in viewModel.nameList) {
//        addFoodListCard(cardIndexCounter++)
////        }
//
//        binding.btnAddFoodList.setOnClickListener {
//            focusOnCard(
//                addFoodListCard(cardIndexCounter++).findViewById(R.id.etFoodList),
//                isIncompleteCard = false
//            )
//        }
//    }

    private fun setUpAddButton() {
        binding.btnAddFoodList.setOnClickListener {
            if (foodListAdapter.itemCount >= MAX_FOOD_CARD) {
                showAlertOverLimitItemCard(MAX_FOOD_CARD)
                return@setOnClickListener
            }
            foodListAdapter.addItem(FoodInfo())
//            focusOnCard(foodListAdapter.itemCount - 1)
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
            foodListAdapter.addItem(FoodInfo())
        }

        if (existingDiscount.isNotEmpty()) binding.etDiscountAmount.setText(existingDiscount)
        if (existingServiceCharge.isNotEmpty()) binding.etServiceChargeAmount.setText(
            existingServiceCharge
        )
        if (existingVat.isNotEmpty()) binding.etVatAmount.setText(existingVat)
    }

//    private fun addFoodListCard(
//        cardIndex: Int,
//        foodName: String = "",
//        foodPrice: Double = 0.0
//    ): View {
//        val inflater = LayoutInflater.from(requireContext())
//        val foodListCardBinding = FoodListCardBinding.inflate(inflater)
//        val foodListCard = foodListCardBinding.root
//
//        val layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        )
//        foodListCard.layoutParams = layoutParams
//
//        foodListCardBinding.ivDeleteFoodList.setOnClickListener {
//            foodListCardBinding.ivDeleteFoodList.isEnabled = false
//            val foodListText = foodListCardBinding.etFoodList.text.toString()
//            val foodPriceText = foodListCardBinding.etFoodPrice.text.toString()
//            if (foodListText.isNotBlank() || foodPriceText.isNotBlank() || foodListCardBinding.nameChipContainer.childCount > 0) {
//                showDeleteItemConfirmationDialog(
//                    requireContext(),
//                    foodListCardBinding.ivDeleteFoodList
//                ) {
//                    (foodListCard.parent as? LinearLayout)?.removeView(foodListCard)
//                    calculateTotalAmount()
//                }
//            } else {
//                (foodListCard.parent as? LinearLayout)?.removeView(foodListCard)
//                calculateTotalAmount()
//            }
//        }
//
//        foodListCardBinding.ivAddNameList.setOnClickListener {
//            foodListCardBinding.ivAddNameList.isEnabled = false
//
//            val nameList = nameStateMap[cardIndex]?.toMutableList()
//                ?: arguments?.getParcelableArrayList<NameInfo>("nameList")?.map {
//                    NameInfo(it.name, it.isChecked)
//                }?.toMutableList() ?: mutableListOf()
//            nameList.sortBy { it.name }
//            val onlyNameList = nameList.map { it.name }.toTypedArray()
//            val onlyIsCheckedList = nameList.map { it.isChecked }.toBooleanArray()
//            showNameSelectionDialog(
//                onlyNameList,
//                onlyIsCheckedList,
//                foodListCardBinding.ivAddNameList
//            )
//            { updatedList ->
//                val checkedNameList = updatedList.filter { it.isChecked }.map { it.name }
//                nameStateMap[cardIndex] = updatedList
//                addNameChip(foodListCardBinding.nameChipContainer, checkedNameList, cardIndex)
//            }
//        }
//
//        addChipListener(foodListCardBinding)
//
//        foodListCardBinding.etFoodList.addTextChangedListener { calculateTotalAmount() }
//        foodListCardBinding.etFoodPrice.addTextChangedListener { calculateTotalAmount() }
//
//        if (binding.foodListContainer.childCount == MAX_FOOD_CARD) {
//            showAlertOverLimitItemCard(MAX_FOOD_CARD)
//        } else {
//            binding.foodListContainer.addView(foodListCard)
//        }
//
//        return foodListCard
//    }

    private fun addChipListener(foodListCardBinding: FoodListCardBinding) {
        val tvPersonPerFoodCard = foodListCardBinding.tvPersonPerFoodCard

        foodListCardBinding.nameChipContainer.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            private var previousChipCount = foodListCardBinding.nameChipContainer.childCount

            override fun onGlobalLayout() {
                val currentChipCount = foodListCardBinding.nameChipContainer.childCount

                if (currentChipCount > 0) {
                    tvPersonPerFoodCard.visibility = View.VISIBLE
                    tvPersonPerFoodCard.text =
                        getString(R.string.person_count, currentChipCount)
                } else {
                    tvPersonPerFoodCard.visibility = View.GONE
                }

                previousChipCount = currentChipCount
            }
        })
    }

    private fun setUpToggleListeners() {
        binding.btnPercentageToggle.setOnClickListener {
            binding.btnPercentageToggle.isEnabled = false
            showTogglePercentageAmountDialog(
                binding.btnPercentageToggle,
                onPercentageSelected = {
                    if (!viewModel.isPercentage) {
                        viewModel.setIsPercentage(true)
                        updateTextViewOfVScDis()
                    }
                },
                onAmountSelected = {
                    if (viewModel.isPercentage) {
                        viewModel.setIsPercentage(false)
                        updateTextViewOfVScDis()
                    }
                }
            )
        }
    }

    private fun updateTextViewOfVScDis() {
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
            totalAmountBeforeCalculation += removeCommasAndReturnDouble(food.foodPrice)
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

            viewModel.saveVatScDcBundle(
                dc = totalAmountBeforeCalculation - totalAmountAfterDiscount,
                sc = totalAmountAfterDiscount * calculateServiceChargeFraction(
                    serviceCharge,
                    totalAmountAfterDiscount
                ),
                vat = totalAmountAfterDiscountAndServiceCharge * calculateVatFraction(
                    vat,
                    totalAmountAfterDiscountAndServiceCharge
                )
            )

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
                        foodListAdapter.addItem(FoodInfo())
//                        focusOnCard(0, isIncompleteCard = true)
                        btnNext.isEnabled = true
                    }
                }

                incompleteCard != null -> {
                    showAlertOnIncompleteCard(incompleteCard.message) {
//                        focusOnCard(incompleteCard) { _ ->
//                            when {
//                                !etFoodList.text.toString().matches(REGEX) -> etFoodList
//                                etFoodList.text.isBlank() -> etFoodList
//                                etFoodPrice.text!!.isBlank() -> etFoodPrice
//                                etFoodList.text.toString().matches(NUM_REGEX) -> etFoodList
//                                etFoodPrice.text.toString()
//                                    .toDoubleOrNull() == 0.0 -> etFoodPrice
//
//                                foodListCardBinding.nameChipContainer.childCount <= 0 -> incompleteCard.findViewById(
//                                    R.id.foodCardContainer
//                                )
//
//                                else -> etFoodList
//                            }
//                        }
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
        val names = nameList.map { it.foodName.trim() }
        return names.size != names.toSet().size
    }

    private fun addNameChip(
        chipGroup: ChipGroup,
        checkedNameList: List<String>,
        cardIndex: Int
    ) {
        chipGroup.removeAllViews()
        checkedNameList.forEach { name ->
            val existingChip = chipGroup.findViewWithTag<Chip>(name)

            if (existingChip == null) {
                val chip = Chip(context).apply {
                    text = name
                    tag = name
                    isClickable = false
                    ellipsize = TextUtils.TruncateAt.END
                    maxWidth = resources.getDimensionPixelSize(R.dimen.space_85dp)
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        chipGroup.removeView(this)

                        nameStateMap[cardIndex]?.find { addNameModal -> addNameModal.name == name }?.isChecked =
                            false
                    }
                }
                chipGroup.addView(chip)
            }
        }

        val horizontalScrollView = chipGroup.parent as? View
        val foodCardContainer = horizontalScrollView?.parent as? View

        foodCardContainer?.let {
            val originalDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.rounded_corner_white_bg)
            if (it.background != originalDrawable) {
                it.background = originalDrawable
            }
        }
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

//    private fun focusOnCard(
//        position: Int,
//        isIncompleteCard: Boolean = false,
//        getTargetView: (View) -> View = { it }
//    ) {
//        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
//        val targetView = getTargetView(cardView)
//        targetView.requestFocus()
//
//        binding.rvFoodList.post {
//            val viewHolder =
//                binding.rvFoodList.findViewHolderForAdapterPosition(position) as? FoodListAdapter.FoodListViewHolder
//            val etFoodList = viewHolder?.binding?.etFoodList
//            val etFoodPrice = viewHolder?.binding?.etFoodPrice
//            etNameList?.requestFocus()
//            etNameList?.text?.clear()
//
//            etNameList?.postDelayed({
//                imm?.showSoftInput(etNameList, InputMethodManager.SHOW_IMPLICIT)
//            }, 100)
//
//            if (isIncompleteCard) {
//                etNameList?.backgroundTintList = ColorStateList.valueOf(
//                    ContextCompat.getColor(requireContext(), R.color.red)
//                )
//            }
//        }
//    }

    private fun findFirstIncompleteCard(foodList: List<FoodInfo>): IncompleteCard? {
        foodList.forEachIndexed { index, foodInfo ->
            val foodName = foodInfo.foodName.trim()
            val foodPrice = foodInfo.foodPrice
            val name = foodInfo.name

            when {
                !foodName.matches(REGEX) -> {
                    foodList[index].isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = getString(R.string.incomplete_card_letter_or_num_message)
                    )
                }


                foodName.isBlank() || foodPrice.isBlank() -> {
                    foodList[index].isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = getString(R.string.incomplete_card_empty_message)
                    )
                }

                foodName.matches(NUM_REGEX) -> {
                    foodList[index].isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = getString(R.string.incomplete_card_num_only_message)
                    )
                }

                removeCommasAndReturnDouble(foodPrice) == 0.0 -> {
                    foodList[index].isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = getString(R.string.incomplete_card_zero_message)
                    )
                }

//                name.isEmpty() -> {
//                    foodList[index].isIncomplete = true
//                    return IncompleteCard(
//                        position = index,
//                        message = getString(R.string.incomplete_card_zero_chip_message)
//                    )
//                }

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
    }
}