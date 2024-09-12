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
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lycorp.webauthn.sample.R
import com.lycorp.webauthn.sample.databinding.FragmentLogWindowBinding
import com.lycorp.webauthn.sample.presentation.viewmodel.Fido2ViewModel
import kotlinx.coroutines.launch

class LogWindowFragment : Fragment() {
    private var _binding: FragmentLogWindowBinding? = null
    private val binding get() = _binding!!

    private val viewModel: Fido2ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLogWindowBinding.inflate(inflater, container, false)

        binding.logWindowClearButton.setOnClickListener {
            viewModel.clearMessage()
        }

        binding.logWindowXButton.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this@LogWindowFragment).commit()
            activity?.findViewById<FrameLayout>(R.id.logWindowContainer)?.visibility = View.GONE
            activity?.findViewById<View>(R.id.logWindowOpenButton)?.visibility = View.VISIBLE
            activity?.findViewById<View>(R.id.logWindowCloseButton)?.visibility = View.GONE
        }

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messageStateFlow.collect { message ->
                    binding.tvMessage.text = message
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
