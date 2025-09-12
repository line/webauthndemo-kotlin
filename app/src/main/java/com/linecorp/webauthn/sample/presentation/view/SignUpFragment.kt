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

package com.linecorp.webauthn.sample.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.linecorp.webauthn.sample.R
import com.linecorp.webauthn.sample.presentation.viewmodel.Fido2ViewModel

class SignUpFragment : Fragment() {
    private val viewModel: Fido2ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout without DataBinding
        val rootView = inflater.inflate(R.layout.fragment_sign_up, container, false)

        // Find RadioGroups by their IDs
        val rgAttestation: RadioGroup = rootView.findViewById(R.id.rgAttestation)
        val rgAuthenticatorAttachment: RadioGroup = rootView.findViewById(R.id.rgAuthenticatorAttachment)
        val rgUserVerification: RadioGroup = rootView.findViewById(R.id.rgUserVerification)

        // Set up listeners for each RadioGroup
        setupRadioGroupListener(rgAttestation, rgAuthenticatorAttachment, rgUserVerification)

        return rootView
    }

    private fun setupRadioGroupListener(
        rgAttestation: RadioGroup,
        rgAuthenticatorAttachment: RadioGroup,
        rgUserVerification: RadioGroup,
    ) {
        rgAttestation.check(R.id.rgAttestationOption1)
        rgAuthenticatorAttachment.check(R.id.rgAuthenticatorAttachmentOption1)
        rgUserVerification.check(R.id.rgUserVerificationOption1)

        rgAttestation.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            val selectedText = selectedRadioButton?.text.toString()

            viewModel.setAttestationOption(selectedText)
        }

        rgAuthenticatorAttachment.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            val selectedText = selectedRadioButton?.text.toString()

            viewModel.setAuthenticatorAttachmentOption(selectedText)
        }

        rgUserVerification.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            val selectedText = selectedRadioButton?.text.toString()

            viewModel.setUserVerificationOption(selectedText)
        }
    }
}
