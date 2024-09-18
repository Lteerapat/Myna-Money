package com.teerapat.moneydivider.addnamelist

import com.teerapat.moneydivider.data.NameInfo
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test

class AddNameListViewModelTest {
    private lateinit var viewModel: AddNameListViewModel

    @Before
    fun setUp() {
        viewModel = AddNameListViewModel()
    }

    @Test
    fun `saveNameList should clear old list and add new list`() {
        val initialList = listOf(NameInfo(NAME_1), NameInfo(NAME_1))
        val newList = listOf(NameInfo(NAME_2), NameInfo(NAME_3))

        viewModel.saveNameList(initialList)
        viewModel.saveNameList(newList)

        assertEquals(2, viewModel.nameList.size)
        assertEquals(NAME_2, viewModel.nameList[0].name)
        assertEquals(NAME_3, viewModel.nameList[1].name)
    }

    @Test
    fun `saveNameList should add all new items to empty list`() {
        val newList = listOf(NameInfo(NAME_2), NameInfo(NAME_3))

        viewModel.saveNameList(newList)

        assertEquals(2, viewModel.nameList.size)
        assertEquals(NAME_2, viewModel.nameList[0].name)
        assertEquals(NAME_3, viewModel.nameList[1].name)
    }

    @Test
    fun `saveNameList should handle empty input list`() {
        val emptyList = listOf<NameInfo>()

        viewModel.saveNameList(emptyList)

        assertEquals(0, viewModel.nameList.size)
    }

    companion object {
        private const val NAME_1 = "John Doe"
        private const val NAME_2 = "John"
        private const val NAME_3 = "Doe"
    }
}