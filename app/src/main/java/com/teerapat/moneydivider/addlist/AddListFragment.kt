package com.teerapat.moneydivider.addlist

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.databinding.FoodListCardBinding
import com.teerapat.moneydivider.databinding.FragmentAddListBinding
import com.teerapat.moneydivider.utils.DecimalDigitsInputFilter
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
            // Update UI with food list items
            // This method should refresh your food list cards
        }
        viewModel.serviceChargeAmount.observe(viewLifecycleOwner) { amount ->
            // Update UI with service charge amount
        }
        viewModel.vatAmount.observe(viewLifecycleOwner) { amount ->
            // Update UI with VAT amount
        }
        viewModel.discountAmount.observe(viewLifecycleOwner) { amount ->
            // Update UI with discount amount
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

            foodListCardBinding.etFoodPrice.filters = arrayOf(DecimalDigitsInputFilter(7, 2))
            thousandSeparatorForEt(foodListCardBinding.etFoodPrice)

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

            foodListCardBinding.etFoodList.addTextChangedListener { calculateTotalAmount() }
            foodListCardBinding.etFoodPrice.addTextChangedListener { calculateTotalAmount() }

            binding.foodListContainer.addView(foodListCard)
        }
    }

    private fun setUpToggleListeners() {
        val toggleMap = mapOf(
            binding.ivServiceChargeToggle to "sc",
            binding.ivDiscountToggle to "dc",
            binding.ivVatToggle to "vat"
        )

        toggleMap.forEach { (toggleView, type) ->
            toggleView.setOnClickListener {
                showTogglePercentageAmountDialog(type)
            }
        }
    }

    private fun showTogglePercentageAmountDialog(type: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Percentage or Amount")
            .setPositiveButton("Percentage") { _, _ ->
                updateTextViewOfVScDis(type, "%")
                isServiceChargePercentage = "%"
            }
            .setNegativeButton("Amount") { _, _ ->
                updateTextViewOfVScDis(
                    type,
                    "฿"
                )
                isServiceChargePercentage = "฿"
            }
            .show()
    }

    private fun updateTextViewOfVScDis(type: String, symbol: String) {
        when (type) {
            "sc" -> {
                binding.tvServiceChargePercentage.text = symbol
                isServiceChargePercentage = symbol
                binding.etServiceChargeAmount.text.clear()
            }

            "dc" -> {
                binding.tvDiscountPercentage.text = symbol
                isDiscountPercentage = symbol
                binding.etDiscountAmount.text.clear()
            }

            "vat" -> {
                binding.tvVatPercentage.text = symbol
                isVatPercentage = symbol
                binding.etVatAmount.text.clear()
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
            editText.filters = arrayOf(DecimalDigitsInputFilter(7, 2))
            thousandSeparatorForEt(editText)
            editText.addTextChangedListener { calculateTotalAmount() }
        }

    }

    private fun calculateTotalAmount() {
        var totalAmount = 0.0

        for (i in 0 until binding.foodListContainer.childCount) {
            val foodListCard = binding.foodListContainer.getChildAt(i)
            val foodListCardBinding = FoodListCardBinding.bind(foodListCard)
            val priceText = foodListCardBinding.etFoodPrice.text.toString()
            totalAmount += removeCommas(priceText)
        }

        totalAmount = calculateServiceCharge(totalAmount)
        totalAmount = calculateVat(totalAmount)
        totalAmount = calculateDiscount(totalAmount)

        binding.tvTotalAmount.text = thousandSeparator(totalAmount)
    }

    private fun calculateServiceCharge(total: Double): Double {
        val serviceChargeText = binding.etServiceChargeAmount.text.toString()
        val serviceCharge = removeCommas(serviceChargeText)
        return if (isServiceChargePercentage == "%") {
            if (serviceCharge > 100) {
                showAlertOnVScDis("Service Charge", "Percentage cannot exceed 100%")
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
        val vat = removeCommas(vatText)
        return if (isVatPercentage == "%") {
            if (vat > 100) {
                showAlertOnVScDis("VAT", "Percentage cannot exceed 100%")
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
        val discount = removeCommas(discountText)
        return if (isDiscountPercentage == "%") {
            if (discount > 100) {
                showAlertOnVScDis("Discount", "Percentage cannot exceed 100%")
                total
            } else {
                total - (total * discount / 100)
            }
        } else {
            if (discount > total) {
                showAlertOnVScDis("Discount", "Discount cannot exceed total amount")
                total
            } else {
                total - discount
            }
        }
    }

    private fun showAlertOnVScDis(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                when (title) {
                    "Service Charge" -> binding.etServiceChargeAmount.text.clear()
                    "VAT" -> binding.etVatAmount.text.clear()
                    "Discount" -> binding.etDiscountAmount.text.clear()
                }
            }
            .setCancelable(false)
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
                    .setTitle("Are you sure you want to continue?")
                    .setPositiveButton("Yes") { _, _ ->
                        findNavController().navigate(
                            R.id.action_addListFragment_to_summaryFragment,
                            buildBundle()
                        )
                    }
                    .setNegativeButton("No", null)
                    .show()
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
            !etFoodList.text.toString().matches(Regex("^[A-Za-z]*$")) -> {
                "Please use only letters for the food name."
            }
            etFoodList.text.isBlank() || etFoodPrice.text.isBlank() -> {
                "Please fill in both name and price for all items."
            }
            etFoodPrice.text.toString().toDoubleOrNull() == 0.0 -> {
                "Price cannot be zero."
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
                    !etFoodList.text.toString().matches(Regex("^[A-Za-z]*$")) -> etFoodList
                    etFoodList.text.isBlank() -> etFoodList
                    etFoodPrice.text.isBlank() -> etFoodPrice
                    etFoodPrice.text.toString().toDoubleOrNull() == 0.0 -> etFoodPrice
                    else -> etFoodList // default case
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
            .setTitle("Incomplete Item")
            .setMessage("Please have at least 1 item to continue!")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setTextWatcherForEditText(editText: EditText) {
        editText.addTextChangedListener {
            editText.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.teal_700))
        }
    }

    private fun findFirstIncompleteCard(): View? {
        val regex = Regex("^[A-Za-z]*$")

        for (i in 0 until binding.foodListContainer.childCount) {
            val foodListCard = binding.foodListContainer.getChildAt(i)
            val foodListCardBinding = FoodListCardBinding.bind(foodListCard)
            val foodName = foodListCardBinding.etFoodList.text.toString()
            val foodPrice = foodListCardBinding.etFoodPrice.text.toString()

            if (!foodName.matches(regex)) return foodListCard

            if (foodName.isNotBlank() && foodPrice.isBlank()) {
                return foodListCard
            } else if (foodName.isBlank() && foodPrice.isNotBlank()) {
                return foodListCard
            } else if (foodName.isBlank() && foodPrice.isBlank()) {
                return foodListCard
            } else if (foodName.isNotBlank() && foodPrice.toDouble() == 0.0) {
                return foodListCard
            }
        }

        return null
    }

    private fun showDeleteItemConfirmationDialog(view: View) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { _, _ ->
                (view.parent as? LinearLayout)?.removeView(view)
                calculateTotalAmount()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun thousandSeparatorForEt(editText: EditText) {
        val decimalFormat = DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.US))
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Do nothing
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun thousandSeparator(amount: Double): String {
        val decimalFormat = DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.US))

        return "${decimalFormat.format(amount)} ฿"
    }

    private fun removeCommas(amount: String): Double {
        return amount.replace(",", "").toDoubleOrNull() ?: 0.0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
}