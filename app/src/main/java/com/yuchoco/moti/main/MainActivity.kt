package com.yuchoco.moti.main

import android.os.Bundle
import androidx.activity.viewModels
import com.yuchoco.moti.BaseActivity
import com.yuchoco.moti.R
import com.yuchoco.moti.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMainDataBinding()
    }

    private fun initMainDataBinding() {
        binding.mainVM = mainViewModel
    }
}