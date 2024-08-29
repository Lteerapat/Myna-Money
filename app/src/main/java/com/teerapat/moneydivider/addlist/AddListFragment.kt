package com.teerapat.moneydivider.addlist

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private var isServiceChargePercentage = "%"
    private var isDiscountPercentage = "%"
    private var isVatPercentage = "%"

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
            if (foodListText.isNotBlank() || foodPriceText.isNotBlank()) {
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
                foodListCardBinding.ivAddNameList
            ) { updatedList ->
                val checkedNameList = updatedList.filter { it.isChecked }.map { it.name }
                nameStateMap[cardIndex] = updatedList
                addNameChip(foodListCardBinding.nameChipContainer, checkedNameList, cardIndex)
            }
        }

        foodListCardBinding.etFoodList.addTextChangedListener { calculateTotalAmount() }
        foodListCardBinding.etFoodPrice.addTextChangedListener { calculateTotalAmount() }

        if (binding.foodListContainer.childCount == MAX_FOOD_CARD) {
            showAlertOverLimitItemCard(MAX_FOOD_CARD)
        } else {
            binding.foodListContainer.addView(foodListCard)
        }

        return foodListCard
    }

    private fun setUpToggleListeners() {
        val toggleMap = mapOf(
            binding.ivServiceChargeToggle to SERVICE_CHARGE,
            binding.ivDiscountToggle to DISCOUNT,
            binding.ivVatToggle to VAT
        )

        toggleMap.forEach { (toggleView, type) ->
            toggleView.setOnClickListener {
                toggleView.isEnabled = false
                showTogglePercentageAmountDialog(
                    toggleView,
                    onPercentageSelected = {
                        updateTextViewOfVScDis(type, getString(R.string.percentage_sign))
                        isServiceChargePercentage = getString(R.string.percentage_sign)
                    },
                    onAmountSelected = {
                        updateTextViewOfVScDis(type, getString(R.string.baht_sign))
                        isServiceChargePercentage = getString(R.string.baht_sign)
                    }
                )
            }
        }
    }

    private fun updateTextViewOfVScDis(type: String, symbol: String) {
        when (type) {
            SERVICE_CHARGE -> {
                binding.tvServiceChargePercentage.text = symbol
                isServiceChargePercentage = symbol
                binding.etServiceChargeAmount.text?.clear()
            }

            DISCOUNT -> {
                binding.tvDiscountPercentage.text = symbol
                isDiscountPercentage = symbol
                binding.etDiscountAmount.text?.clear()
            }

            VAT -> {
                binding.tvVatPercentage.text = symbol
                isVatPercentage = symbol
                binding.etVatAmount.text?.clear()
            }
        }
    }

    private fun setUpAmountOfVScDis() {
        val editTexts = listOf(
            binding.etDiscountAmount,
            binding.etServiceChargeAmount,
            binding.etVatAmount
        )

        editTexts.forEach { editText ->
            editText.addTextChangedListener { calculateTotalAmount() }
        }

    }

    private fun calculateTotalAmount() {
        var totalAmount = 0.0

        for (i in 0 until binding.foodListContainer.childCount) {
            val foodListCard = binding.foodListContainer.getChildAt(i)
            val foodListCardBinding = FoodListCardBinding.bind(foodListCard)
            val priceText = foodListCardBinding.etFoodPrice.text.toString()
            totalAmount += removeCommasAndReturnDouble(priceText)
        }

        totalAmount = calculateDiscount(totalAmount)
        totalAmount = calculateServiceCharge(totalAmount)
        totalAmount = calculateVat(totalAmount)

        binding.tvTotalAmount.text = thousandSeparator(totalAmount)
    }

    private fun calculateServiceCharge(total: Double): Double {
        val serviceChargeText = binding.etServiceChargeAmount.text.toString()
        val serviceCharge = removeCommasAndReturnDouble(serviceChargeText)
        return if (isServiceChargePercentage == getString(R.string.percentage_sign)) {
            if (serviceCharge > 100) {
                showAlertOnVScDis(
                    getString(R.string.service_charge),
                    getString(R.string.percentage_exceeded_alert_message),
                    serviceChargeField = binding.etServiceChargeAmount,
                )
                total
            } else {
                total + (total * serviceCharge / 100)
            }
        } else {
            total + serviceCharge
        }
    }

    private fun calculateVat(total: Double): Double {
        val vatText = binding.etVatAmount.text.toString()
        val vat = removeCommasAndReturnDouble(vatText)
        return if (isVatPercentage == getString(R.string.percentage_sign)) {
            if (vat > 100) {
                showAlertOnVScDis(
                    getString(R.string.vat),
                    getString(R.string.percentage_exceeded_alert_message),
                    vatField = binding.etVatAmount,
                )
                total
            } else {
                total + (total * vat / 100)
            }
        } else {
            total + vat
        }
    }

    private fun calculateDiscount(total: Double): Double {
        val discountText = binding.etDiscountAmount.text.toString()
        val discount = removeCommasAndReturnDouble(discountText)
        return if (isDiscountPercentage == getString(R.string.percentage_sign)) {
            if (discount > 100) {
                showAlertOnVScDis(
                    getString(R.string.discount),
                    getString(R.string.percentage_exceeded_alert_message),
                    discountField = binding.etDiscountAmount
                )
                total
            } else {
                total - (total * discount / 100)
            }
        } else {
            if (discount > total) {
                showAlertOnVScDis(
                    getString(R.string.discount),
                    getString(R.string.discount_exceeded_alert_message),
                    discountField = binding.etDiscountAmount
                )
                total
            } else {
                total - discount
            }
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

    private fun addNameChip(chipGroup: ChipGroup, checkedNameList: List<String>, cardIndex: Int) {
        chipGroup.removeAllViews()
        checkedNameList.forEach { name ->
            val existingChip = chipGroup.findViewWithTag<Chip>(name)

            if (existingChip == null) {
                val chip = Chip(context).apply {
                    text = name
                    tag = name
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

        val scPercentage = isServiceChargePercentage
        val vatPercentage = isVatPercentage
        val dcPercentage = isDiscountPercentage

        return Bundle().apply {
            //sc vat dc data
            putString("scAmount", scAmount)
            putString("vatAmount", vatAmount)
            putString("dcAmount", dcAmount)

            //sc vat dc % or not
            putString("scPercentage", scPercentage)
            putString("vatPercentage", vatPercentage)
            putString("dcPercentage", dcPercentage)

        }
    }

    private fun convertAmountToPercentage() {

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
        private const val SERVICE_CHARGE = "sc"
        private const val DISCOUNT = "dis"
        private const val VAT = "vat"
        private val REGEX = Regex("^[A-Za-z0-9ก-๏ ]*$")
        private val NUM_REGEX = Regex("[0-9]*$")
        private const val DECIMAL_PATTERN = "#,###.##"
        private const val MAX_FOOD_CARD = 50
    }
}