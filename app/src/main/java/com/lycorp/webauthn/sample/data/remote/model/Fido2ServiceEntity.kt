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

package com.lycorp.webauthn.sample.data.remote.model

import com.lycorp.webauthn.model.AuthenticatorSelectionCriteria
import com.lycorp.webauthn.model.ClientExtensionInput
import com.lycorp.webauthn.model.ClientExtensionsOutput
import com.lycorp.webauthn.model.CredentialProtection
import com.lycorp.webauthn.model.PublicKeyCredentialDescriptor

// assertion/request
data class GetAuthenticationChallengeInput(
    var userVerification: String?,
    var username: String?,
)

data class GetAuthenticationChallengeResult(
    var allowCredentials: List<PublicKeyCredentialDescriptor>?,
    var challenge: String,
    var credentialId: String,
    var errorMessage: String,
    var extensions: ClientExtensionInput,
    var rpId: String,
    var sessionId: String,
    var status: String,
    var timeout: ULong,
    var userVerification: String,
    var cookie: String,
)

data class SendResponseResult(
    var credentialId: String,
    var status: String,
    var errorMessage: String,
    var sessionId: String,
)

// assertion/result
data class SendAuthenticationResponseInput(
    var extensions: ClientExtensionsOutput?,
    var id: String,
    var rawId: String,
    var response: ResponseInAuthenticationResult,
    var type: String,
)

data class ResponseInAuthenticationResult(
    var authenticatorData: String,
    var clientDataJSON: String,
    var signature: String,
    var userHandle: String?,
)

// attestation/options
data class GetRegistrationChallengeInput(
    var attestation: String?,
    var authenticatorSelection: AuthenticatorSelectionCriteria?,
    var credProtect: CredentialProtection?,
    var displayName: String,
    var username: String,
)

data class GetRegistrationChallengeResult(
    var attestation: String,
    var authenticatorSelection: AuthenticatorSelectionCriteria?,
    var challenge: String,
    var credentialId: String,
    var errorMessage: String,
    var excludeCredentials: List<PublicKeyCredentialDescriptor>?,
    var extensions: ClientExtensionInput,
    var pubKeyCredParams: List<PubKeyCredParams>,
    var rp: RP,
    var sessionId: String,
    var status: String,
    var timeout: ULong,
    var user: User,
)

data class PubKeyCredParams(
    var alg: String,
    var type: String,
)

data class RP(
    var icon: String,
    var id: String,
    var name: String,
)

data class User(
    var displayName: String,
    var icon: String,
    var id: String,
    var name: String,
)

data class SendRegistrationResponseInput(
    var extensions: ClientExtensionsOutput?,
    var id: String,
    var rawId: String,
    var response: ResponseInRegistrationResult,
    var type: String,
)

data class ResponseInRegistrationResult(
    var attestationObject: String,
    var clientDataJSON: String,
    var transports: List<String>?,
)
