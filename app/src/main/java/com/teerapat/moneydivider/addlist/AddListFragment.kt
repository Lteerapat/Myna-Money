package com.teerapat.moneydivider.addlist

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.addnamelist.AddNameModal
import com.teerapat.moneydivider.databinding.FoodListCardBinding
import com.teerapat.moneydivider.databinding.FragmentAddListBinding
import com.teerapat.moneydivider.utils.focusOnCard
import com.teerapat.moneydivider.utils.showAlertDuplicateNames
import com.teerapat.moneydivider.utils.showAlertOnIncompleteCard
import com.teerapat.moneydivider.utils.showAlertOnVScDis
import com.teerapat.moneydivider.utils.showAlertOverLimitItemCard
import com.teerapat.moneydivider.utils.showAlertZeroCardList
import com.teerapat.moneydivider.utils.showContinueDialog
import com.teerapat.moneydivider.utils.showDeleteItemConfirmationDialog
import com.teerapat.moneydivider.utils.showNameSelectionDialog
import com.teerapat.moneydivider.utils.showTogglePercentageAmountDialog
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


class AddListFragment : Fragment() {
    private lateinit var viewModel: AddListViewModel
    private var _binding: FragmentAddListBinding? = null
    private val binding get() = _binding!!

    private var isPercentage = true

    private val nameStateMap = mutableMapOf<Int, List<AddNameModal>>()
    private var cardIndexCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AddListViewModel::class.java]
//        observe()
    }

    private fun observe() {
        viewModel.foodListItems.observe(viewLifecycleOwner) { items ->
        }
        viewModel.serviceChargeAmount.observe(viewLifecycleOwner) { amount ->
        }
        viewModel.vatAmount.observe(viewLifecycleOwner) { amount ->
        }
        viewModel.discountAmount.observe(viewLifecycleOwner) { amount ->
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFoodListCard()
        setUpToggleListeners()
        setUpAmountOfVScDis()
        setUpNextButton()
    }

    private fun setUpFoodListCard() {
//        for (nameModal in viewModel.nameList) {
        addFoodListCard(cardIndexCounter++)
//        }

        binding.btnAddFoodList.setOnClickListener {
            focusOnCard(
                addFoodListCard(cardIndexCounter++).findViewById(R.id.etFoodList),
                isIncompleteCard = false
            )
        }
    }

    private fun addFoodListCard(
        cardIndex: Int,
        foodName: String = "",
        foodPrice: Double = 0.0
    ): View {
        val inflater = LayoutInflater.from(requireContext())
        val foodListCardBinding = FoodListCardBinding.inflate(inflater)
        val foodListCard = foodListCardBinding.root

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        foodListCard.layoutParams = layoutParams

        foodListCardBinding.ivDeleteFoodList.setOnClickListener {
            foodListCardBinding.ivDeleteFoodList.isEnabled = false
            val foodListText = foodListCardBinding.etFoodList.text.toString()
            val foodPriceText = foodListCardBinding.etFoodPrice.text.toString()
            if (foodListText.isNotBlank() || foodPriceText.isNotBlank() || foodListCardBinding.nameChipContainer.childCount > 0) {
                showDeleteItemConfirmationDialog(foodListCardBinding.ivDeleteFoodList) {
                    (foodListCard.parent as? LinearLayout)?.removeView(foodListCard)
                    calculateTotalAmount()
                }
            } else {
                (foodListCard.parent as? LinearLayout)?.removeView(foodListCard)
                calculateTotalAmount()
            }
        }

        foodListCardBinding.ivAddNameList.setOnClickListener {
            foodListCardBinding.ivAddNameList.isEnabled = false

            val nameList = nameStateMap[cardIndex]?.toMutableList()
                ?: arguments?.getParcelableArrayList<AddNameModal>("nameList")?.map {
                    AddNameModal(it.name, it.isChecked)
                }?.toMutableList() ?: mutableListOf()
            nameList.sortBy { it.name }
            val onlyNameList = nameList.map { it.name }.toTypedArray()
            val onlyIsCheckedList = nameList.map { it.isChecked }.toBooleanArray()
            showNameSelectionDialog(
                onlyNameList,
                onlyIsCheckedList,
                foodListCardBinding.ivAddNameList,
                onOkClick = { updatedList ->
                    val checkedNameList = updatedList.filter { it.isChecked }.map { it.name }
                    nameStateMap[cardIndex] = updatedList
                    addNameChip(foodListCardBinding.nameChipContainer, checkedNameList, cardIndex)
                },
                onSelectAllClick = { updatedList ->
                    val checkedNameList = updatedList.filter { it.isChecked }.map { it.name }
                    nameStateMap[cardIndex] = updatedList
                    addNameChip(foodListCardBinding.nameChipContainer, checkedNameList, cardIndex)
                })
        }

        addChipListener(foodListCardBinding)

        foodListCardBinding.etFoodList.addTextChangedListener { calculateTotalAmount() }
        foodListCardBinding.etFoodPrice.addTextChangedListener { calculateTotalAmount() }

        if (binding.foodListContainer.childCount == MAX_FOOD_CARD) {
            showAlertOverLimitItemCard(MAX_FOOD_CARD)
        } else {
            binding.foodListContainer.addView(foodListCard)
        }

        return foodListCard
    }

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
                    if (!isPercentage) {
                        isPercentage = true
                        updateTextViewOfVScDis()
                    }
                },
                onAmountSelected = {
                    if (isPercentage) {
                        isPercentage = false
                        updateTextViewOfVScDis()
                    }
                }
            )
        }
    }

    private fun updateTextViewOfVScDis() {
        val symbol =
            if (isPercentage) getString(R.string.percentage_sign) else getString(R.string.baht_sign)

        binding.tvServiceChargePercentage.text = symbol
        binding.tvDiscountPercentage.text = symbol
        binding.tvVatPercentage.text = symbol

        binding.etServiceChargeAmount.text?.clear()
        binding.etDiscountAmount.text?.clear()
        binding.etVatAmount.text?.clear()
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

        for (i in 0 until binding.foodListContainer.childCount) {
            val foodListCard = binding.foodListContainer.getChildAt(i)
            val foodListCardBinding = FoodListCardBinding.bind(foodListCard)
            val priceText = foodListCardBinding.etFoodPrice.text.toString()
            totalAmountBeforeCalculation += removeCommasAndReturnDouble(priceText)
        }

        if (!isPercentage) {
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

            totalAmountBeforeCalculation = totalAmountAfterDiscountAndServiceChargeAndVat
        }

        binding.tvTotalAmount.text =
            thousandSeparator(totalAmountBeforeCalculation)
    }

    private fun calculateDiscountFraction(
        discount: Double,
        totalAmountBeforeCalculation: Double
    ): Double {
        return if (isPercentage) {
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
        return if (isPercentage) {
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
        return if (isPercentage) {
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
            val incompleteCard = findFirstIncompleteCard()

            when {
                binding.foodListContainer.childCount <= 0 -> {
                    showAlertZeroCardList {
                        focusOnCard(addFoodListCard(cardIndexCounter++)) { cardView ->
                            FoodListCardBinding.bind(cardView).etFoodList
                        }
                    }
                }

                incompleteCard != null -> {
                    val foodListCardBinding = FoodListCardBinding.bind(incompleteCard)
                    val etFoodList = foodListCardBinding.etFoodList
                    val etFoodPrice = foodListCardBinding.etFoodPrice
                    val message = when {
                        !etFoodList.text.toString().matches(REGEX) -> {
                            getString(R.string.incomplete_card_letter_or_num_message)
                        }

                        etFoodList.text.isBlank() || etFoodPrice.text!!.isBlank() -> {
                            getString(R.string.incomplete_card_empty_message)
                        }

                        etFoodList.text.toString().matches(NUM_REGEX) -> {
                            getString(R.string.incomplete_card_num_only_message)
                        }

                        etFoodPrice.text.toString().toDoubleOrNull() == 0.0 -> {
                            getString(R.string.incomplete_card_zero_message)
                        }

                        foodListCardBinding.nameChipContainer.childCount <= 0 -> {
                            getString(R.string.incomplete_card_zero_chip_message)
                        }

                        else -> {
                            getString(R.string.incomplete_item)
                        }
                    }

                    showAlertOnIncompleteCard(message) {
                        focusOnCard(incompleteCard) { _ ->
                            when {
                                !etFoodList.text.toString().matches(REGEX) -> etFoodList
                                etFoodList.text.isBlank() -> etFoodList
                                etFoodPrice.text!!.isBlank() -> etFoodPrice
                                etFoodList.text.toString().matches(NUM_REGEX) -> etFoodList
                                etFoodPrice.text.toString()
                                    .toDoubleOrNull() == 0.0 -> etFoodPrice

                                foodListCardBinding.nameChipContainer.childCount <= 0 -> incompleteCard.findViewById(
                                    R.id.foodCardContainer
                                )

                                else -> etFoodList
                            }
                        }
                    }
                }

                hasDuplicateNames() -> {
                    showAlertDuplicateNames()
                }

                else -> {
                    binding.btnNext.isEnabled = false
                    showContinueDialog(binding.btnNext) {
                        findNavController().navigate(
                            R.id.action_addListFragment_to_summaryFragment,
                            buildBundle()
                        )
                    }
                }
            }
        }
    }

    private fun hasDuplicateNames(): Boolean {
        val nameSet = mutableSetOf<String>()

        for (i in 0 until binding.foodListContainer.childCount) {
            val foodListCard = binding.foodListContainer.getChildAt(i)
            val foodListCardBinding = FoodListCardBinding.bind(foodListCard)
            val name = foodListCardBinding.etFoodList.text.toString().trim()

            if (name in nameSet) {
                return true
            } else {
                nameSet.add(name)
            }
        }

        return false
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
        val scAmount = binding.etServiceChargeAmount.text.toString()
        val vatAmount = binding.etVatAmount.text.toString()
        val dcAmount = binding.etDiscountAmount.text.toString()

        return Bundle().apply {
            putString("scAmount", scAmount)
            putString("vatAmount", vatAmount)
            putString("dcAmount", dcAmount)

        }
    }

    private fun convertAmountToFraction(numerator: Double, denominator: Double): Double {
        if (denominator == 0.0) {
            return 0.0
        }

        return numerator / denominator
    }

    private fun findFirstIncompleteCard(): View? {
        for (i in 0 until binding.foodListContainer.childCount) {
            val foodListCard = binding.foodListContainer.getChildAt(i)
            val foodListCardBinding = FoodListCardBinding.bind(foodListCard)
            val foodName = foodListCardBinding.etFoodList.text.toString()
            val foodPrice = foodListCardBinding.etFoodPrice.text.toString()

            if (!foodName.matches(REGEX)) return foodListCard

            if (foodName.matches(NUM_REGEX)) return foodListCard

            if (foodName.isNotBlank() && foodPrice.isBlank()) {
                return foodListCard
            } else if (foodName.isBlank() && foodPrice.isNotBlank()) {
                return foodListCard
            } else if (foodName.isBlank() && foodPrice.isBlank()) {
                return foodListCard
            } else if (foodName.isNotBlank() && removeCommasAndReturnDouble(foodPrice) == 0.0) {
                return foodListCard
            }

            if (foodListCardBinding.nameChipContainer.childCount <= 0) return foodListCard
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

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    companion object {
        private val REGEX = Regex("^[A-Za-z0-9ก-๏ ]*$")
        private val NUM_REGEX = Regex("[0-9]*$")
        private const val DECIMAL_PATTERN = "#,###.##"
        private const val MAX_FOOD_CARD = 50
    }
}