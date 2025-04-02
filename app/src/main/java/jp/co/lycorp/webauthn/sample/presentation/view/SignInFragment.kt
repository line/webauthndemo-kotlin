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

package jp.co.lycorp.webauthn.sample.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import jp.co.lycorp.webauthn.sample.R
import jp.co.lycorp.webauthn.sample.presentation.viewmodel.Fido2ViewModel

class SignInFragment : Fragment() {
    private val viewModel: Fido2ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_sign_in, container, false)
        val radioGroup = rootView.findViewById<RadioGroup>(R.id.rgUserVerification)
        setupRadioGroupListener(radioGroup)

        return rootView
    }

    private fun setupRadioGroupListener(radioGroup: RadioGroup) {
        radioGroup.check(R.id.rgUserVerificationOption1)

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            val selectedText = selectedRadioButton?.text.toString()

            viewModel.setUserVerificationOption(selectedText)
        }
    }
}
