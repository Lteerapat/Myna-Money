package com.teerapat.moneydivider

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.teerapat.moneydivider.databinding.ActivityMainBinding

class MainActivity : BaseViewBindingActivity<ActivityMainBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = binding.root.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            val isKeyboardVisible = keypadHeight > screenHeight * 0.15

            binding.toolBar.isVisible = !isKeyboardVisible
        }
    }

    override fun createBinding(inflater: LayoutInflater): ActivityMainBinding =
        ActivityMainBinding.inflate(inflater)
}



