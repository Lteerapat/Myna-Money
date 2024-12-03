package com.teerapat.moneydivider.addnamelist

import androidx.lifecycle.LiveData
import com.hadilq.liveevent.LiveEvent
import com.teerapat.moneydivider.BaseViewModel
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.data.IncompleteCard
import com.teerapat.moneydivider.data.NameInfo
import com.teerapat.moneydivider.utils.action

class AddNameListViewModel : BaseViewModel() {
    var nameList: MutableList<NameInfo> = mutableListOf()
    private val _showDialogIncompleteItem = LiveEvent<IncompleteCard>()
    val showDialogIncompleteItem: LiveData<IncompleteCard>
        get() = _showDialogIncompleteItem

    private val _showDialogEmptyNameList = LiveEvent<Unit>()
    val showDialogEmptyNameList: LiveData<Unit>
        get() = _showDialogEmptyNameList

    private val _showDialogDuplicateName = LiveEvent<Unit>()
    val showDialogDuplicateName: LiveData<Unit>
        get() = _showDialogDuplicateName

    private val _showDialogConfirmNavigate = LiveEvent<Unit>()
    val showDialogConfirmNavigate: LiveData<Unit>
        get() = _showDialogConfirmNavigate

    fun saveNameList(list: List<NameInfo>) {
        nameList.clear()
        nameList.addAll(list)
    }

    fun executeNextButton(nameList: List<NameInfo>) {
        when {
            nameList.isEmpty() -> {
                _showDialogEmptyNameList.action()
            }

            findFirstIncompleteCard(nameList)?.let {
                _showDialogIncompleteItem.action(it)
                true
            } == true -> {}

            hasDuplicateNames(nameList) -> {
                _showDialogDuplicateName.action()
            }

            else -> {
                _showDialogConfirmNavigate.action()
            }
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

    companion object {
        private val REGEX = Regex("^[A-Za-z0-9ก-๏ ]*$")
        private val NUM_REGEX = Regex("[0-9]*$")
        private const val ET_NAME_LIST = "ET_NAME_LIST"
    }
}