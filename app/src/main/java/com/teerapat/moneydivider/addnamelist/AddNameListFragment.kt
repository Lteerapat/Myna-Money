package com.teerapat.moneydivider.addnamelist

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
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.addlist.AddNameModal
import com.teerapat.moneydivider.databinding.FragmentAddNameListBinding
import com.teerapat.moneydivider.databinding.NameListCardBinding

class AddNameListFragment : Fragment() {
    private lateinit var viewModel: AddNameListViewModel
    private var _binding: FragmentAddNameListBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AddNameListViewModel::class.java]
        observe()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNameListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpNameListCard()
        setUpNextButton()
    }

    private fun observe() {
    }


    private fun setUpNameListCard() {
//        for (nameModal in viewModel.nameList) {
        addNameListCard()
//        }

        binding.btnAddNameList.setOnClickListener {
            addNameListCard()
        }
    }

    private fun addNameListCard(name: String = ""): View {
        val inflater = LayoutInflater.from(requireContext())
        val nameListCardBinding = NameListCardBinding.inflate(inflater)
        val nameListCard = nameListCardBinding.root

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        nameListCard.layoutParams = layoutParams

        nameListCardBinding.ivDeleteNameList.setOnClickListener {
            val nameListText = nameListCardBinding.etNameList.text.toString()
            if (nameListText.isNotBlank()) {
                showDeleteItemConfirmationDialog(nameListCard)
            } else {
                (nameListCard.parent as? LinearLayout)?.removeView(nameListCard)
            }
        }

        if (binding.nameListContainer.childCount == MAX_NAME_CARD) {
            showAlertOverLimitItemCard()
        } else {
            binding.nameListContainer.addView(nameListCard)
//            nameListCardBinding.etNameList.setText(name)

        }

        return nameListCard
    }

    private fun showDeleteItemConfirmationDialog(view: View) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_delete_title))
            .setMessage(getString(R.string.confirm_delete_message))
            .setPositiveButton(getString(R.string.yes_btn)) { _, _ ->
                (view.parent as? LinearLayout)?.removeView(view)
            }
            .setNegativeButton(getString(R.string.no_btn), null)
            .show()
    }

    private fun setUpNextButton() {
        binding.btnNext.setOnClickListener {
            val incompleteCard = findFirstIncompleteCard()

            when {
                binding.nameListContainer.childCount <= 0 -> {
                    showAlertEmptyNameList()
                }

                incompleteCard != null -> {
                    showAlertOnIncompleteCard(incompleteCard)
                }

                hasDuplicateNames() -> {
                    showAlertDuplicateNames()
                }

                else -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.next_btn_alert_title))
                        .setPositiveButton(getString(R.string.yes_btn)) { _, _ ->
                            viewModel.saveNameList(getNameList())
                            findNavController().navigate(
                                R.id.action_addNameListFragment_to_addListFragment,
                                buildBundle()
                            )
                        }
                        .setNegativeButton(getString(R.string.no_btn), null)
                        .show()
                }
            }
        }
    }

    private fun buildBundle(): Bundle {
        val nameList = getNameList()

        return Bundle().apply {
            putParcelableArrayList("nameList", ArrayList(nameList))
        }
    }

    private fun showAlertDuplicateNames() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.duplicate_name_alert_title))
            .setMessage(getString(R.string.duplicate_name_alert_message))
            .setPositiveButton(getString(R.string.ok_btn), null)
            .show()
    }

    private fun hasDuplicateNames(): Boolean {
        val nameSet = mutableSetOf<String>()

        for (i in 0 until binding.nameListContainer.childCount) {
            val nameListCard = binding.nameListContainer.getChildAt(i)
            val nameListCardBinding = NameListCardBinding.bind(nameListCard)
            val name = nameListCardBinding.etNameList.text.toString().trim()

            if (name in nameSet) {
                return true
            } else {
                nameSet.add(name)
            }
        }

        return false
    }

    private fun getNameList(): List<AddNameModal> {
        val nameList = mutableListOf<AddNameModal>()

        for (i in 0 until binding.nameListContainer.childCount) {
            val nameListCard = binding.nameListContainer.getChildAt(i)
            val nameListCardBinding = NameListCardBinding.bind(nameListCard)
            val name = nameListCardBinding.etNameList.text.toString().trim()

            if (name.isNotBlank()) {
                nameList.add(AddNameModal(name, false))
            }
        }

        return nameList
    }

    private fun showAlertOnIncompleteCard(nameListCard: View) {
        val nameListCardBinding = NameListCardBinding.bind(nameListCard)
        val etNameList = nameListCardBinding.etNameList
        val message = when {
            !etNameList.text.toString().matches(REGEX) -> {
                getString(R.string.incomplete_letter_or_num_message_2)
            }

            etNameList.text.isBlank() -> {
                getString(R.string.incomplete_empty_card_message_2)
            }

            else -> {
                getString(R.string.incomplete_item)
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.incomplete_item))
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok_btn), null)
            .setOnDismissListener {
                focusOnCard(nameListCard)
            }
            .show()
    }

    private fun showAlertEmptyNameList() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.incomplete_item))
            .setMessage(getString(R.string.incomplete_card_at_least_1_message))
            .setPositiveButton(getString(R.string.ok_btn), null)
            .setOnDismissListener { focusOnCard(addNameListCard()) }
            .show()
    }

    private fun focusOnCard(nameListCard: View) {
        val nameListCardBinding = NameListCardBinding.bind(nameListCard)
        val etNameList = nameListCardBinding.etNameList
        etNameList.requestFocus()
        etNameList.text.clear()
        etNameList.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), R.color.red)
        )
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        etNameList.postDelayed({
            imm?.showSoftInput(etNameList, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
        setTextWatcherForEditText(etNameList)
    }

    private fun setTextWatcherForEditText(editText: EditText) {
        editText.addTextChangedListener {
            editText.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.teal_700))
        }
    }


    private fun findFirstIncompleteCard(): View? {
        for (i in 0 until binding.nameListContainer.childCount) {
            val nameListCard = binding.nameListContainer.getChildAt(i)
            val nameListCardBinding = NameListCardBinding.bind(nameListCard)
            val name = nameListCardBinding.etNameList.text.toString()

            if (name.isBlank()) return nameListCard
            if (!name.matches(REGEX)) return nameListCard
        }
        return null
    }

    private fun showAlertOverLimitItemCard() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.item_limit_exceeded_title))
            .setMessage(getString(R.string.item_limit_exceeded_message, MAX_NAME_CARD))
            .setPositiveButton(getString(R.string.ok_btn), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val REGEX = Regex("^[A-Za-z0-9ก-๏ ]*$")
        private const val MAX_NAME_CARD = 30
    }
}