package com.teerapat.moneydivider.addnamelist

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.teerapat.moneydivider.BaseViewBinding
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.data.IncompleteCard
import com.teerapat.moneydivider.data.NameInfo
import com.teerapat.moneydivider.databinding.FragmentAddNameListBinding
import com.teerapat.moneydivider.utils.openSoftKeyboard

class AddNameListFragment : BaseViewBinding<FragmentAddNameListBinding>() {
    private lateinit var viewModel: AddNameListViewModel
    private lateinit var nameListAdapter: NameListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AddNameListViewModel::class.java]
        observe()
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAddNameListBinding {
        return FragmentAddNameListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNameListRecyclerView()
        setUpAddButton()
        setUpGroupAddButton()
        setUpDeleteAllNameButton()
        setUpNextButton()
        loadInitialData()
    }

    private fun observe() {
    }

    private fun setupNameListRecyclerView() {
        nameListAdapter =
            NameListAdapter()
                .setOnClickButtonDelete { pair ->
                    dialogAble.show(
                        title = R.string.confirm_delete_title,
                        description = R.string.confirm_delete_message,
                        onPositiveButtonClick = { nameListAdapter.removeItem(pair.second) }
                    )
                }
        binding.rvNameList.adapter = nameListAdapter
    }

    private fun setUpAddButton() {
        binding.btnAddNameList.setOnClickListener {
            if (nameListAdapter.itemCount >= MAX_NAME_CARD) {
                dialogAble.show(
                    title = R.string.item_limit_exceeded_title,
                    description = R.string.item_limit_exceeded_message,
                    descriptionArg = MAX_NAME_CARD,
                    isShowPositiveButton = false
                )
                return@setOnClickListener
            }
            nameListAdapter.addItem(NameInfo())
            focusOnCard(position = nameListAdapter.itemCount - 1, incompleteField = ET_NAME_LIST)
        }
    }

    private fun setUpGroupAddButton() {
        binding.btnGroupAddNameList.setOnClickListener {
            dialogAble.showSingleItemSelectionDialog { _, selectedOptionPosition ->
                val itemCountToAdd = when (selectedOptionPosition) {
                    0 -> 5
                    1 -> 10
                    2 -> 15
                    3 -> 20
                    else -> 0
                }

                val itemsToAdd = minOf(itemCountToAdd, MAX_NAME_CARD - nameListAdapter.itemCount)

                repeat(itemsToAdd) {
                    nameListAdapter.addItem(NameInfo())
                }
            }
        }
    }

    private fun setUpDeleteAllNameButton() {
        binding.tvDeleteAllNameList.setOnClickListener {
            if (nameListAdapter.itemCount > 0) {
                dialogAble.show(
                    title = R.string.confirm_delete_all_title,
                    description = R.string.confirm_delete_all_message,
                    onPositiveButtonClick = { nameListAdapter.removeAllItem() }
                )
            }
        }
    }

    private fun setUpNextButton() {
        binding.btnNext.setOnClickListener {
            val nameList = nameListAdapter.getNameList()
            val incompleteCard = findFirstIncompleteCard(nameList)

            when {
                nameList.isEmpty() -> {
                    dialogAble.show(
                        title = R.string.incomplete_item,
                        description = R.string.incomplete_card_at_least_1_message,
                        isShowPositiveButton = false,
                        onDismissListener = {
                            nameListAdapter.addItem(NameInfo(isIncomplete = true))
                            focusOnCard(
                                position = 0,
                                isIncompleteCard = true,
                                incompleteField = ET_NAME_LIST
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

                hasDuplicateNames(nameList) -> {
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
                            viewModel.saveNameList(nameListAdapter.getNameList())
                            next(
                                R.id.action_addNameListFragment_to_addFoodListFragment,
                                bundleOf(NAME_LIST_BUNDLE to viewModel.nameList)
                            )
                        }
                    )
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
                        message = R.string.incomplete_empty_card_message_2,
                        incompleteField = ET_NAME_LIST
                    )
                }

                !name.matches(REGEX) -> {
                    nameList[index].isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = R.string.incomplete_letter_or_num_message_2,
                        incompleteField = ET_NAME_LIST
                    )
                }

                name.matches(NUM_REGEX) -> {
                    nameList[index].isIncomplete = true
                    return IncompleteCard(
                        position = index,
                        message = R.string.incomplete_card_num_only_message_2,
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
        binding.rvNameList.scrollToPosition(position)
        binding.rvNameList.post {
            val viewHolder =
                binding.rvNameList.findViewHolderForAdapterPosition(position) as? NameListAdapter.NameListViewHolder
            val etNameList = viewHolder?.binding?.etNameList

            when (incompleteField) {
                ET_NAME_LIST -> {
                    etNameList?.openSoftKeyboard()
                    if (isIncompleteCard) {
                        etNameList?.let {
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

    companion object {
        private val REGEX = Regex("^[A-Za-z0-9ก-๏ ]*$")
        private val NUM_REGEX = Regex("[0-9]*$")
        private const val MAX_NAME_CARD = 20
        const val NAME_LIST_BUNDLE = "NAME_LIST_BUNDLE"
        private const val ET_NAME_LIST = "etNameList"
    }
}