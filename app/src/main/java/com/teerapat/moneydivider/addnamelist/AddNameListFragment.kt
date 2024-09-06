package com.teerapat.moneydivider.addnamelist

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.adapter.NameListAdapter
import com.teerapat.moneydivider.data.NameInfo
import com.teerapat.moneydivider.databinding.FragmentAddNameListBinding
import com.teerapat.moneydivider.utils.showAlertDuplicateNames
import com.teerapat.moneydivider.utils.showAlertOnIncompleteCard
import com.teerapat.moneydivider.utils.showAlertOverLimitItemCard
import com.teerapat.moneydivider.utils.showAlertZeroCardList
import com.teerapat.moneydivider.utils.showContinueDialog

class AddNameListFragment : Fragment() {
    private lateinit var viewModel: AddNameListViewModel
    private var _binding: FragmentAddNameListBinding? = null
    private val binding get() = _binding!!
    private lateinit var nameListAdapter: NameListAdapter

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

        setupNameListRecyclerView()
        setUpAddButton()
        setUpNextButton()
        loadInitialData()
    }

    private fun observe() {
    }

    private fun setupNameListRecyclerView() {
        nameListAdapter = NameListAdapter(requireContext())
        binding.rvNameList.adapter = nameListAdapter
    }

    private fun setUpAddButton() {
        binding.btnAddNameList.setOnClickListener {
            if (nameListAdapter.itemCount >= MAX_NAME_CARD) {
                showAlertOverLimitItemCard(MAX_NAME_CARD)
                return@setOnClickListener
            }
            nameListAdapter.addItem(NameInfo())
            focusOnCard(position = nameListAdapter.itemCount - 1, incompleteField = ET_NAME_LIST)
        }
    }

    private fun setUpNextButton() {
        binding.btnNext.setOnClickListener {
            val btnNext = binding.btnNext
            btnNext.isEnabled = false
            val nameList = nameListAdapter.getNameList()
            val incompleteCard = findFirstIncompleteCard(nameList)

            when {
                nameList.isEmpty() -> {
                    showAlertZeroCardList {
                        nameListAdapter.addItem(NameInfo())
                        focusOnCard(
                            position = 0,
                            isIncompleteCard = true,
                            incompleteField = ET_NAME_LIST
                        )
                        btnNext.isEnabled = true
                    }
                }

                incompleteCard != null -> {
                    showAlertOnIncompleteCard(incompleteCard.message) {
                        focusOnCard(
                            incompleteCard.position,
                            isIncompleteCard = true,
                            incompleteField = incompleteCard.incompleteField
                        )
                        btnNext.isEnabled = true
                    }
                }

                hasDuplicateNames(nameList) -> {
                    showAlertDuplicateNames {
                        btnNext.isEnabled = true
                    }
                }

                else -> {
                    showContinueDialog(binding.btnNext) {
                        viewModel.saveNameList(nameListAdapter.getNameList())
                        findNavController().navigate(
                            R.id.action_addNameListFragment_to_addFoodListFragment,
                            buildBundle()
                        )
                    }
                }
            }
        }
    }

    private fun loadInitialData() {
        val existingNameList = viewModel.nameList
        if (existingNameList.isNotEmpty()) {
            nameListAdapter.setItems(existingNameList)
        } else {
            nameListAdapter.addItem(NameInfo())
        }
    }

    private fun buildBundle(): Bundle {
        val nameList = viewModel.nameList
        return Bundle().apply {
            putParcelableArrayList("nameList", ArrayList(nameList))
        }
    }

    private fun hasDuplicateNames(nameList: List<NameInfo>): Boolean {
        val names = nameList.map { it.name.trim() }
        return names.size != names.toSet().size
    }

    private fun findFirstIncompleteCard(nameList: List<NameInfo>): IncompleteCard? {
        nameList.forEachIndexed { index, nameInfo ->
            val name = nameInfo.name.trim()

            when {
                name.isBlank() -> {
                    nameList[index].isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = getString(R.string.incomplete_empty_card_message_2),
                        incompleteField = ET_NAME_LIST
                    )
                }

                !name.matches(REGEX) -> {
                    nameList[index].isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = getString(R.string.incomplete_letter_or_num_message_2),
                        incompleteField = ET_NAME_LIST
                    )
                }

                name.matches(NUM_REGEX) -> {
                    nameList[index].isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = getString(R.string.incomplete_card_num_only_message_2),
                        incompleteField = ET_NAME_LIST
                    )
                }
            }
        }
        return null
    }

    private fun focusOnCard(
        position: Int,
        isIncompleteCard: Boolean = false,
        incompleteField: String
    ) {
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        binding.rvNameList.scrollToPosition(position)
        binding.rvNameList.post {
            val viewHolder =
                binding.rvNameList.findViewHolderForAdapterPosition(position) as? NameListAdapter.NameListViewHolder
            val etNameList = viewHolder?.binding?.etNameList

            when (incompleteField) {
                ET_NAME_LIST -> {
                    etNameList?.requestFocus()
                    etNameList?.text?.clear()
                    etNameList?.postDelayed({
                        imm?.showSoftInput(etNameList, InputMethodManager.SHOW_IMPLICIT)
                    }, 100)

                    if (isIncompleteCard) {
                        etNameList?.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.red)
                        )
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveNameList(nameListAdapter.getNameList())
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveNameList(nameListAdapter.getNameList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val REGEX = Regex("^[A-Za-z0-9ก-๏ ]*$")
        private val NUM_REGEX = Regex("[0-9]*$")
        private const val MAX_NAME_CARD = 30
        private const val ET_NAME_LIST = "etNameList"
    }
}