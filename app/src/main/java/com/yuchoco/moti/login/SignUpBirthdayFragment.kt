package com.yuchoco.moti.login

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.yuchoco.moti.BaseFragment
import com.yuchoco.moti.R
import com.yuchoco.moti.databinding.FragmentSignupBirthdayBinding

class SignUpBirthdayFragment :
    BaseFragment<FragmentSignupBirthdayBinding>(R.layout.fragment_signup_birthday) {

    private val viewModel by viewModels<LoginViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        viewModel.signUpFragment.observe(
            viewLifecycleOwner, Observer<LoginViewModel.SignUpFragment> {
                if (it == LoginViewModel.SignUpFragment.Complete) {
                    (activity as LoginActivity?)?.changeFragment(SignUpCompleteFragment.newInstance())
                }
            })
        viewModel.popFragment.observe(viewLifecycleOwner, Observer<Unit> {
            (activity as LoginActivity?)?.popFragment()
        })
    }

    companion object {
        fun newInstance() = SignUpBirthdayFragment()
    }

}