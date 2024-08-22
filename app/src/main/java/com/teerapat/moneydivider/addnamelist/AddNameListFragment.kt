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
        addNameListCard()
        setUpNextButton()
    }

    private fun observe() {
    }

    private fun addNameListCard() {
        binding.btnAddNameList.setOnClickListener {
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

            binding.nameListContainer.addView(nameListCard)
        }
    }

    private fun showDeleteItemConfirmationDialog(view: View) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { _, _ ->
                (view.parent as? LinearLayout)?.removeView(view)
            }
            .setNegativeButton("No", null)
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
                        .setTitle("Are you sure you want to continue?")
                        .setPositiveButton("Yes") { _, _ ->
                            findNavController().navigate(
                                R.id.action_addNameListFragment_to_addListFragment,
                                buildBundle()
                            )
                        }
                        .setNegativeButton("No", null)
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
            .setTitle("Duplicate Name")
            .setMessage("Please ensure all names are unique before continuing.")
            .setPositiveButton("OK", null)
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
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        val etNameList = nameListCardBinding.etNameList
        val message = when {
            !etNameList.text.toString().matches(Regex("^[A-Za-z0-9ก-๏ ]*$")) -> {
                "Please use only letters or numbers for the name."
            }

            etNameList.text.isBlank() -> {
                "Please fill in the name"
            }

            else -> {
                getString(R.string.incomplete_item)
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Incomplete Item")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->

                etNameList.requestFocus()
                etNameList.text.clear()
                etNameList.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.red)
                )
                etNameList.postDelayed({
                    imm?.showSoftInput(etNameList, InputMethodManager.SHOW_IMPLICIT)
                }, 100)

                setTextWatcherForEditText(etNameList)
            }
            .setCancelable(false)
            .show()
    }

    private fun setTextWatcherForEditText(editText: EditText) {
        editText.addTextChangedListener {
            editText.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.teal_700))
        }
    }


    private fun showAlertEmptyNameList() {
        AlertDialog.Builder(requireContext())
            .setTitle("Incomplete Item")
            .setMessage("Please have at least 1 item to continue!")
            .setPositiveButton("OK", null)
            .show()
    }


    private fun findFirstIncompleteCard(): View? {
        for (i in 0 until binding.nameListContainer.childCount) {
            val nameListCard = binding.nameListContainer.getChildAt(i)
            val nameListCardBinding = NameListCardBinding.bind(nameListCard)
            val name = nameListCardBinding.etNameList.text.toString()

            if (name.isBlank()) return nameListCard
            if (!name.matches(Regex("^[A-Za-z0-9ก-๏ ]*$"))) return nameListCard
        }
        return null
    }

}