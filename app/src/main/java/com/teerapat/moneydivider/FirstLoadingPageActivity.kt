package com.teerapat.moneydivider

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import com.teerapat.moneydivider.databinding.ActivityFirstLoadingPageBinding

class FirstLoadingPageActivity : BaseViewBindingActivity<ActivityFirstLoadingPageBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }

    override fun createBinding(inflater: LayoutInflater): ActivityFirstLoadingPageBinding =
        ActivityFirstLoadingPageBinding.inflate(inflater)
}