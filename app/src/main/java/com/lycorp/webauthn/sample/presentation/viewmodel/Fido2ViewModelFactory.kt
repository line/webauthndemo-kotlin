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

package com.lycorp.webauthn.sample.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lycorp.webauthn.model.Fido2PromptInfo
import com.lycorp.webauthn.publickeycredential.PublicKeyCredential

class Fido2ViewModelFactory(
    private val publicKeyCredential: PublicKeyCredential,
    private val fido2PromptInfo: Fido2PromptInfo? = null,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(Fido2ViewModel::class.java)) {
            return Fido2ViewModel(publicKeyCredential, fido2PromptInfo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
