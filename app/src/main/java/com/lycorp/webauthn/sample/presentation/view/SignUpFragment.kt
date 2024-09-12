/*
 * Copyright 2024 LY Corporation
 *
 * LY Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.lycorp.webauthn.sample.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.lycorp.webauthn.sample.R
import com.lycorp.webauthn.sample.databinding.FragmentSignUpBinding
import com.lycorp.webauthn.sample.presentation.viewmodel.Fido2ViewModel

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: Fido2ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)
        setupRadioGroupListener()
        return binding.apply {
            viewModel = this@SignUpFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRadioGroupListener() {
        binding.rgAttestation.check(R.id.rgAttestationOption1)
        binding.rgAuthenticatorAttachment.check(R.id.rgAuthenticatorAttachmentOption1)
        binding.rgUserVerification.check(R.id.rgUserVerificationOption1)

        binding.rgAttestation.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            val selectedText = selectedRadioButton?.text.toString()

            viewModel.setAttestationOption(selectedText)
        }
        binding.rgAuthenticatorAttachment.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            val selectedText = selectedRadioButton?.text.toString()

            viewModel.setAuthenticatorAttachmentOption(selectedText)
        }
        binding.rgUserVerification.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            val selectedText = selectedRadioButton?.text.toString()

            viewModel.setUserVerificationOption(selectedText)
        }
    }
}
