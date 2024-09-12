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
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.lycorp.webauthn.exceptions.WebAuthnException
import com.lycorp.webauthn.model.AttestationConveyancePreference
import com.lycorp.webauthn.model.AuthenticatorSelectionCriteria
import com.lycorp.webauthn.model.Fido2PromptInfo
import com.lycorp.webauthn.model.UserVerificationRequirement
import com.lycorp.webauthn.publickeycredential.PublicKeyCredential
import com.lycorp.webauthn.rp.AuthenticationOptions
import com.lycorp.webauthn.rp.RegistrationOptions
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.PrintWriter
import java.io.StringWriter

class Fido2ViewModel(
    private val publicKeyCredential: PublicKeyCredential,
    private val fido2PromptInfo: Fido2PromptInfo? = null,
) : ViewModel() {
    private val _messageStateFlow = MutableStateFlow("")
    val messageStateFlow: StateFlow<String> = _messageStateFlow.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _attestation = MutableStateFlow<String>("none")
    val attestation: StateFlow<String> = _attestation.asStateFlow()

    private val _authenticatorAttachment = MutableStateFlow<String>("platform")
    val authenticatorAttachment: StateFlow<String> = _authenticatorAttachment.asStateFlow()

    private val _userVerification = MutableStateFlow<String>("required")
    val userVerification: StateFlow<String> = _userVerification.asStateFlow()

    private val _currentFragment = MutableStateFlow(FRAGMENT_DEFAULT)
    val currentFragment: StateFlow<String> = _currentFragment.asStateFlow()

    companion object {
        const val FRAGMENT_DEFAULT = "FRAGMENT_DEFAULT"
        const val FRAGMENT_SIGN_UP = "FRAGMENT_SIGN_UP"
        const val FRAGMENT_SIGN_IN = "FRAGMENT_SIGN_IN"
    }

    private val exceptionHandler =
        CoroutineExceptionHandler { _, exception ->
            val errorMessage =
                when (exception) {
                    is WebAuthnException.RpException -> {
                        val cause = exception.cause
                        if (cause is HttpException) {
                            val httpErrorCode = cause.response()?.code()
                            val errorBody = cause.response()?.errorBody()?.string()
                            val formattedErrorBody = formatJson(errorBody)

                            "HTTP code: $httpErrorCode\nError Body: $formattedErrorBody"
                        } else {
                            cause?.message ?: ""
                        }
                    }

                    else -> {
                        val sw = StringWriter()
                        val pw = PrintWriter(sw)
                        exception.printStackTrace(pw)
                        sw.toString()
                    }
                }
            val errorMessages =
                "---------------ERROR---------------\n" +
                    "${exception::class.java.simpleName}:\n$errorMessage"
            _messageStateFlow.value =
                if (_messageStateFlow.value.isEmpty()) {
                    "$errorMessages\n"
                } else {
                    "${_messageStateFlow.value}\n$errorMessages\n"
                }
        }

    fun signUp() =
        viewModelScope.launch(Dispatchers.Default + exceptionHandler) {
            var username = name.value
            if (username.isEmpty()) {
                username = "Gildong Hong"
            }

            var displayUsername = displayName.value
            if (displayUsername.isEmpty()) {
                displayUsername = "Hong"
            }

            val registrationOptions =
                RegistrationOptions(
                    attestation = AttestationConveyancePreference.fromValue(attestation.value)!!,
                    authenticatorSelection =
                        AuthenticatorSelectionCriteria(
                            authenticatorAttachment = authenticatorAttachment.value,
                            userVerification = userVerification.value,
                        ),
                    credProtect = null,
                    displayName = displayUsername,
                    username = username,
                )
            val result =
                publicKeyCredential.create(
                    options = registrationOptions,
                    fido2PromptInfo = fido2PromptInfo,
                )

            if (result.isSuccess) {
                _messageStateFlow.value += "Register success!\n"
            } else {
                val exception = result.exceptionOrNull()!!
                val errorMessage =
                    when (exception) {
                        is WebAuthnException.RpException -> {
                            val cause = exception.cause
                            if (cause is HttpException) {
                                val httpErrorCode = cause.response()?.code()
                                val errorBody = cause.response()?.errorBody()?.string()
                                val formattedErrorBody = formatJson(errorBody)

                                "HTTP code: $httpErrorCode\nError Body: $formattedErrorBody"
                            } else {
                                cause?.message ?: ""
                            }
                        }

                        else -> {
                            val sw = StringWriter()
                            val pw = PrintWriter(sw)
                            exception.printStackTrace(pw)
                            sw.toString()
                        }
                    }
                val errorMessages =
                    "---------------ERROR---------------\n" +
                        "${exception::class.java.simpleName}:\n$errorMessage"
                _messageStateFlow.value =
                    if (_messageStateFlow.value.isEmpty()) {
                        "$errorMessages\n"
                    } else {
                        "${_messageStateFlow.value}\n$errorMessages\n"
                    }
            }
        }

    fun signIn() =
        viewModelScope.launch(Dispatchers.Default + exceptionHandler) {
            var username = name.value
            if (username.isEmpty()) {
                username = "Gildong Hong"
            }

            val authenticationOptions =
                AuthenticationOptions(
                    userVerification = UserVerificationRequirement.fromValue(userVerification.value)!!,
                    username = username,
                )
            val result =
                publicKeyCredential.get(
                    options = authenticationOptions,
                    fido2PromptInfo = fido2PromptInfo,
                )

            if (result.isSuccess) {
                _messageStateFlow.value += "Authenticate success!\n"
            } else {
                val exception = result.exceptionOrNull()!!
                val errorMessage =
                    when (exception) {
                        is WebAuthnException.RpException -> {
                            val cause = exception.cause
                            if (cause is HttpException) {
                                val httpErrorCode = cause.response()?.code()
                                val errorBody = cause.response()?.errorBody()?.string()
                                val formattedErrorBody = formatJson(errorBody)

                                "HTTP code: $httpErrorCode\nError Body: $formattedErrorBody"
                            } else {
                                cause?.message ?: ""
                            }
                        }

                        else -> {
                            val sw = StringWriter()
                            val pw = PrintWriter(sw)
                            exception.printStackTrace(pw)
                            sw.toString()
                        }
                    }
                val errorMessages =
                    "---------------ERROR---------------\n" +
                        "${exception::class.java.simpleName}:\n$errorMessage"
                _messageStateFlow.value =
                    if (_messageStateFlow.value.isNullOrEmpty()) {
                        "$errorMessages\n"
                    } else {
                        "${_messageStateFlow.value}\n$errorMessages\n"
                    }
            }
        }

    fun clearMessage() {
        _messageStateFlow.value = ""
    }

    fun showAllAccounts() {
        viewModelScope.launch(Dispatchers.Default + exceptionHandler) {
            val publicKeyCredential = publicKeyCredential.getAllAccounts()
            var credentialLogs = ""
            if (publicKeyCredential.isNotEmpty()) {
                credentialLogs = "--------------------------------\n"
            }

            publicKeyCredential.forEach {
                credentialLogs += "aaguid: {${it.aaguid}\ncredId: ${it.id}\nrpId: ${it.rpId}\n"
                if (it.userHandle != null) {
                    credentialLogs += "userId: ${it.userHandle}\n"
                }
                credentialLogs += "--------------------------------\n"
            }
            _messageStateFlow.value += credentialLogs
        }
    }

    fun deleteAllAccounts() {
        viewModelScope.launch(Dispatchers.Default + exceptionHandler) {
            publicKeyCredential.deleteAllAccounts()
            _messageStateFlow.value += "All accounts are deleted.\n"
        }
    }

    fun updateName(newName: String) {
        _name.value = newName
    }

    fun updateDisplayName(newDisplayName: String) {
        _displayName.value = newDisplayName
    }

    fun setAttestationOption(option: String) {
        _attestation.value = option
    }

    fun setAuthenticatorAttachmentOption(option: String) {
        _authenticatorAttachment.value = option
    }

    fun setUserVerificationOption(option: String) {
        _userVerification.value = option
    }

    fun showDefaultFragment() {
        _currentFragment.value = FRAGMENT_DEFAULT
    }

    fun showSignUpFragment() {
        _currentFragment.value = FRAGMENT_SIGN_UP
    }

    fun showSignInFragment() {
        _currentFragment.value = FRAGMENT_SIGN_IN
    }

    private fun formatJson(json: String?): String {
        return try {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonElement = Gson().fromJson(json, Any::class.java)
            gson.toJson(jsonElement)
        } catch (e: Exception) {
            json ?: ""
        }
    }
}
