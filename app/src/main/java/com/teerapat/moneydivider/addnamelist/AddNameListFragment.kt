package com.teerapat.moneydivider.addnamelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.teerapat.moneydivider.R
import com.teerapat.moneydivider.databinding.FragmentAddNameListBinding
import com.teerapat.moneydivider.databinding.NameListCardBinding
import com.teerapat.moneydivider.utils.focusOnCard
import com.teerapat.moneydivider.utils.showAlertDuplicateNames
import com.teerapat.moneydivider.utils.showAlertOnIncompleteCard
import com.teerapat.moneydivider.utils.showAlertOverLimitItemCard
import com.teerapat.moneydivider.utils.showAlertZeroCardList
import com.teerapat.moneydivider.utils.showContinueDialog
import com.teerapat.moneydivider.utils.showDeleteItemConfirmationDialog

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
        addNameListCard()

        for (nameModal in viewModel.nameList) {
            addNameListCard()
        }

        binding.btnAddNameList.setOnClickListener {
            focusOnCard(addNameListCard().findViewById(R.id.etNameList), isIncompleteCard = false)
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

        nameListCardBinding.etNameList.setText(name)

        nameListCardBinding.ivDeleteNameList.setOnClickListener {
            nameListCardBinding.ivDeleteNameList.isEnabled = false
            val nameListText = nameListCardBinding.etNameList.text.toString()
            if (nameListText.isNotBlank()) {
                showDeleteItemConfirmationDialog(nameListCardBinding.ivDeleteNameList) {
                    (nameListCard.parent as? LinearLayout)?.removeView(nameListCard)
                }
            } else {
                (nameListCard.parent as? LinearLayout)?.removeView(nameListCard)
            }
        }

        if (binding.nameListContainer.childCount == MAX_NAME_CARD) {
            showAlertOverLimitItemCard(MAX_NAME_CARD)
        } else {
            binding.nameListContainer.addView(nameListCard)

        }

        return nameListCard
    }

    private fun setUpNextButton() {
        binding.btnNext.setOnClickListener {
            val incompleteCard = findFirstIncompleteCard()

            when {
                binding.nameListContainer.childCount <= 0 -> {
                    showAlertZeroCardList {
                        focusOnCard(addNameListCard()) { cardView ->
                            NameListCardBinding.bind(cardView).etNameList
                        }
                    }
                }

                incompleteCard != null -> {
                    val nameListCardBinding = NameListCardBinding.bind(incompleteCard)
                    val etNameList = nameListCardBinding.etNameList
                    val message = when {
                        !etNameList.text.toString().matches(REGEX) -> {
                            getString(R.string.incomplete_letter_or_num_message_2)
                        }

                        etNameList.text.isBlank() -> {
                            getString(R.string.incomplete_empty_card_message_2)
                        }

                        etNameList.text.toString().matches(NUM_REGEX) -> {
                            getString(R.string.incomplete_card_num_only_message_2)
                        }

                        else -> {
                            getString(R.string.incomplete_item)
                        }
                    }

                    showAlertOnIncompleteCard(message) {
                        focusOnCard(incompleteCard) { _ ->
                            etNameList
                        }
                    }
                }

                hasDuplicateNames() -> {
                    showAlertDuplicateNames()
                }

                else -> {
                    binding.btnNext.isEnabled = false
                    showContinueDialog(binding.btnNext) {
                        viewModel.saveNameList(getNameList())
                        findNavController().navigate(
                            R.id.action_addNameListFragment_to_addFoodListFragment,
                            buildBundle()
                        )
                    }
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

    private fun findFirstIncompleteCard(): View? {
        for (i in 0 until binding.nameListContainer.childCount) {
            val nameListCard = binding.nameListContainer.getChildAt(i)
            val nameListCardBinding = NameListCardBinding.bind(nameListCard)
            val name = nameListCardBinding.etNameList.text.toString()

            if (name.isBlank()) return nameListCard
            if (!name.matches(REGEX)) return nameListCard
            if (name.matches(NUM_REGEX)) return nameListCard
        }

        return null
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveNameList(getNameList())
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveNameList(getNameList())
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }

    companion object {
        private val REGEX = Regex("^[A-Za-z0-9ก-๏ ]*$")
        private val NUM_REGEX = Regex("[0-9]*$")
        private const val MAX_NAME_CARD = 30
    }
}