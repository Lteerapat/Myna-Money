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
        val initialList = listOf(NameInfo(name1), NameInfo(name1))
        val newList = listOf(NameInfo(name2), NameInfo(name3))

        viewModel.saveNameList(initialList)
        viewModel.saveNameList(newList)

        assertEquals(2, viewModel.nameList.size)
        assertEquals(name2, viewModel.nameList[0].name)
        assertEquals(name3, viewModel.nameList[1].name)
    }

    @Test
    fun `saveNameList should add all new items to empty list`() {
        val newList = listOf(NameInfo(name2), NameInfo(name3))

        viewModel.saveNameList(newList)

        assertEquals(2, viewModel.nameList.size)
        assertEquals(name2, viewModel.nameList[0].name)
        assertEquals(name3, viewModel.nameList[1].name)
    }

    @Test
    fun `saveNameList should handle empty input list`() {
        val emptyList = listOf<NameInfo>()

        viewModel.saveNameList(emptyList)

        assertEquals(0, viewModel.nameList.size)
    }

    companion object {
        private const val name1 = "John Doe"
        private const val name2 = "John"
        private const val name3 = "Doe"
    }
}