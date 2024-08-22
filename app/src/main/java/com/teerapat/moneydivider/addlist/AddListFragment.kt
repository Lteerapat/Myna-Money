package com.teerapat.moneydivider.addlist

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.databinding.FoodListCardBinding
import com.teerapat.moneydivider.databinding.FragmentAddListBinding
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
        addFoodListCard()
        setUpToggleListeners()
        setUpAmountOfVScDis()
        setUpNextButton()
    }

    private fun addFoodListCard() {
        binding.btnAddFoodList.setOnClickListener {
            val inflater = LayoutInflater.from(requireContext())
            val foodListCardBinding = FoodListCardBinding.inflate(inflater)
            val foodListCard = foodListCardBinding.root

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            foodListCard.layoutParams = layoutParams

            foodListCardBinding.ivDeleteFoodList.setOnClickListener {
                val foodListText = foodListCardBinding.etFoodList.text.toString()
                val foodPriceText = foodListCardBinding.etFoodPrice.text.toString()
                if (foodListText.isNotBlank() || foodPriceText.isNotBlank()) {
                    showDeleteItemConfirmationDialog(foodListCard)
                } else {
                    (foodListCard.parent as? LinearLayout)?.removeView(foodListCard)
                    calculateTotalAmount()
                }
            }

            foodListCardBinding.ivAddNameList.setOnClickListener {
                val nameList = arguments?.getParcelableArrayList<AddNameModal>("nameList")
                val onlyNameList = nameList?.map { it.name }?.toTypedArray()
                val onlyIsCheckedList = nameList?.map { it.isChecked }?.toBooleanArray()

                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.add_name_for_food_list_card_title))
                    .setMultiChoiceItems(
                        onlyNameList,
                        onlyIsCheckedList
                    ) { _, position, isChecked ->
                        nameList?.get(position)?.isChecked = isChecked
                    }
                    .setPositiveButton(getString(R.string.ok_btn)) { _, _ ->
                        val unCheckedNameList = nameList?.filter { !it.isChecked }?.map { it.name }
                        val checkedNameList = nameList?.filter { it.isChecked }?.map { it.name }
                        if (checkedNameList != null) {
                            addNameChip(checkedNameList)
                        }
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }

            foodListCardBinding.etFoodList.addTextChangedListener { calculateTotalAmount() }
            foodListCardBinding.etFoodPrice.addTextChangedListener { calculateTotalAmount() }

            binding.foodListContainer.addView(foodListCard)
        }
    }

    private fun setUpToggleListeners() {
        val toggleMap = mapOf(
            binding.ivServiceChargeToggle to SERVICE_CHARGE,
            binding.ivDiscountToggle to DISCOUNT,
            binding.ivVatToggle to VAT
        )

        toggleMap.forEach { (toggleView, type) ->
            toggleView.setOnClickListener {
                showTogglePercentageAmountDialog(type)
            }
        }
    }

    private fun showTogglePercentageAmountDialog(type: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.percentage_or_amount_toggle_title))
            .setPositiveButton(getString(R.string.percentage_message)) { _, _ ->
                updateTextViewOfVScDis(type, getString(R.string.percentage_sign))
                isServiceChargePercentage = getString(R.string.percentage_sign)
            }
            .setNegativeButton(getString(R.string.amount_message)) { _, _ ->
                updateTextViewOfVScDis(
                    type,
                    getString(R.string.baht_sign)
                )
                isServiceChargePercentage = getString(R.string.baht_sign)
            }
            .show()
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

        totalAmount = calculateServiceCharge(totalAmount)
        totalAmount = calculateVat(totalAmount)
        totalAmount = calculateDiscount(totalAmount)

        binding.tvTotalAmount.text = thousandSeparator(totalAmount)
    }

    private fun calculateServiceCharge(total: Double): Double {
        val serviceChargeText = binding.etServiceChargeAmount.text.toString()
        val serviceCharge = removeCommasAndReturnDouble(serviceChargeText)
        return if (isServiceChargePercentage == getString(R.string.percentage_sign)) {
            if (serviceCharge > 100) {
                showAlertOnVScDis(
                    getString(R.string.service_charge),
                    getString(R.string.percentage_exceeded_alert_message)
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
                    getString(R.string.percentage_exceeded_alert_message)
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
                    getString(R.string.percentage_exceeded_alert_message)
                )
                total
            } else {
                total - (total * discount / 100)
            }
        } else {
            if (discount > total) {
                showAlertOnVScDis(
                    getString(R.string.discount),
                    getString(R.string.discount_exceeded_alert_message)
                )
                total
            } else {
                total - discount
            }
        }
    }

    private fun showAlertOnVScDis(title: String, message: String) {
        when (title) {
            getString(R.string.service_charge) -> binding.etServiceChargeAmount.text?.clear()
            getString(R.string.vat) -> binding.etVatAmount.text?.clear()
            getString(R.string.discount) -> binding.etDiscountAmount.text?.clear()
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok_btn), null)
            .show()

    }

    private fun setUpNextButton() {
        binding.btnNext.setOnClickListener {
            val incompleteCard = findFirstIncompleteCard()

            if (binding.foodListContainer.childCount <= 0) {
                showAlertEmptyFoodList()
            } else if (incompleteCard != null) {
                showAlertOnIncompleteCard(incompleteCard)
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.next_btn_alert_title))
                    .setPositiveButton(getString(R.string.yes_btn)) { _, _ ->
                        findNavController().navigate(
                            R.id.action_addListFragment_to_summaryFragment,
                            buildBundle()
                        )
                    }
                    .setNegativeButton(getString(R.string.no_btn), null)
                    .show()
            }
        }
    }

    private fun addNameChip(checkedNameList: List<String>) {
        val chipGroup = binding.foodListContainer.findViewById<ChipGroup>(R.id.nameChipContainer)

        checkedNameList.forEach { name ->
            val existingChip = chipGroup.findViewWithTag<Chip>(name)

            if (existingChip == null) {
                val chip = Chip(context).apply {
                    text = name
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        chipGroup.removeView(this)
                    }
                }
                chipGroup.addView(chip)
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

    private fun showAlertOnIncompleteCard(foodListCard: View) {
        val foodListCardBinding = FoodListCardBinding.bind(foodListCard)
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)

        val etFoodList = foodListCardBinding.etFoodList
        val etFoodPrice = foodListCardBinding.etFoodPrice

        val message = when {
            !etFoodList.text.toString().matches(REGEX) -> {
                getString(R.string.incomplete_card_letter_or_num_message)
            }

            etFoodList.text.isBlank() || etFoodPrice.text!!.isBlank() -> {
                getString(R.string.incomplete_card_empty_message)
            }

            etFoodPrice.text.toString().toDoubleOrNull() == 0.0 -> {
                getString(R.string.incomplete_card_zero_message)
            }

            else -> {
                getString(R.string.incomplete_item)
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.incomplete_item))
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok_btn)) { _, _ ->
                val targetEditText = when {
                    !etFoodList.text.toString().matches(REGEX) -> etFoodList
                    etFoodList.text.isBlank() -> etFoodList
                    etFoodPrice.text!!.isBlank() -> etFoodPrice
                    etFoodPrice.text.toString().toDoubleOrNull() == 0.0 -> etFoodPrice
                    else -> etFoodList
                }

                targetEditText.requestFocus()
                targetEditText.text.clear()
                targetEditText.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.red)
                )
                targetEditText.postDelayed({
                    imm?.showSoftInput(targetEditText, InputMethodManager.SHOW_IMPLICIT)
                }, 100)

                setTextWatcherForEditText(targetEditText)
            }
            .setCancelable(false)
            .show()
    }


    private fun showAlertEmptyFoodList() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.incomplete_item))
            .setMessage(getString(R.string.incomplete_card_zero_message))
            .setPositiveButton(getString(R.string.ok_btn), null)
            .show()
    }

    private fun setTextWatcherForEditText(editText: EditText) {
        editText.addTextChangedListener {
            editText.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.teal_700))
        }
    }

    private fun findFirstIncompleteCard(): View? {
        for (i in 0 until binding.foodListContainer.childCount) {
            val foodListCard = binding.foodListContainer.getChildAt(i)
            val foodListCardBinding = FoodListCardBinding.bind(foodListCard)
            val foodName = foodListCardBinding.etFoodList.text.toString()
            val foodPrice = foodListCardBinding.etFoodPrice.text.toString()

            if (!foodName.matches(REGEX)) return foodListCard

            if (foodName.isNotBlank() && foodPrice.isBlank()) {
                return foodListCard
            } else if (foodName.isBlank() && foodPrice.isNotBlank()) {
                return foodListCard
            } else if (foodName.isBlank() && foodPrice.isBlank()) {
                return foodListCard
            } else if (foodName.isNotBlank() && removeCommasAndReturnDouble(foodPrice) == 0.0) {
                return foodListCard
            }
        }

        return null
    }

    private fun showDeleteItemConfirmationDialog(view: View) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_delete_title))
            .setMessage(getString(R.string.confirm_delete_message))
            .setPositiveButton(getString(R.string.yes_btn)) { _, _ ->
                (view.parent as? LinearLayout)?.removeView(view)
                calculateTotalAmount()
            }
            .setNegativeButton(getString(R.string.no_btn), null)
            .show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val SERVICE_CHARGE = "sc"
        private const val DISCOUNT = "dis"
        private const val VAT = "vat"
        private val REGEX = Regex("^[A-Za-z0-9ก-๏ ]*$")
        private const val DECIMAL_PATTERN = "#,###.##"
    }
}