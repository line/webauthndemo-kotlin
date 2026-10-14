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

package com.linecorp.webauthn.sample.network

import com.linecorp.webauthn.model.COSEAlgorithmIdentifier
import com.linecorp.webauthn.model.PublicKeyCredentialCreateResult
import com.linecorp.webauthn.model.PublicKeyCredentialGetResult
import com.linecorp.webauthn.model.PublicKeyCredentialParams
import com.linecorp.webauthn.model.PublicKeyCredentialRpEntity
import com.linecorp.webauthn.model.PublicKeyCredentialType
import com.linecorp.webauthn.model.PublicKeyCredentialUserEntity
import com.linecorp.webauthn.rp.AuthenticationData
import com.linecorp.webauthn.rp.AuthenticationOptions
import com.linecorp.webauthn.rp.RegistrationData
import com.linecorp.webauthn.rp.RegistrationOptions
import com.linecorp.webauthn.rp.RelyingParty
import com.linecorp.webauthn.util.toBase64url
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicReference

/**
 * A [RelyingParty] that runs inside the app, for trying the SDK without a FIDO2 server.
 *
 * It issues a random challenge for each ceremony and checks that the client data returned for it
 * carries that challenge and the expected type. It does not verify the attestation, the signature or
 * the origin, so it shows the calls the SDK makes to a relying party and must not stand in for a
 * server.
 */
class FakeRelyingParty : RelyingParty {
    private val random = SecureRandom()
    private val pendingChallenge = AtomicReference<String?>(null)

    override suspend fun getRegistrationData(options: RegistrationOptions): RegistrationData =
        RegistrationData(
            attestation = options.attestation,
            authenticatorSelection = options.authenticatorSelection,
            challenge = issueChallenge(),
            excludeCredentials = null,
            extensions = null,
            pubKeyCredParams =
                listOf(
                    PublicKeyCredentialParams(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256),
                ),
            rp = PublicKeyCredentialRpEntity(id = RP_ID, name = RP_NAME),
            user =
                PublicKeyCredentialUserEntity(
                    id = userHandle(options.username),
                    name = options.username,
                    displayName = options.displayName,
                ),
        )

    override suspend fun verifyRegistration(result: PublicKeyCredentialCreateResult) {
        verifyClientData(result.authenticatorAttestationResponse.clientDataJSON, expectedType = "webauthn.create")
    }

    override suspend fun getAuthenticationData(options: AuthenticationOptions): AuthenticationData =
        AuthenticationData(
            allowCredentials = null,
            challenge = issueChallenge(),
            extensions = null,
            rpId = RP_ID,
            userVerification = options.userVerification,
        )

    override suspend fun verifyAuthentication(result: PublicKeyCredentialGetResult) {
        verifyClientData(result.authenticatorAssertionResponse.clientDataJSON, expectedType = "webauthn.get")
    }

    private fun issueChallenge(): String {
        val challenge = ByteArray(CHALLENGE_SIZE).also { random.nextBytes(it) }.toBase64url()
        pendingChallenge.set(challenge)
        return challenge
    }

    private fun verifyClientData(
        clientDataJSON: ByteArray,
        expectedType: String,
    ) {
        val issuedChallenge = checkNotNull(pendingChallenge.getAndSet(null)) { "No challenge is pending." }
        val clientData = Json.parseToJsonElement(clientDataJSON.decodeToString()).jsonObject
        val type = clientData["type"]?.jsonPrimitive?.content
        check(type == expectedType) { "Expected client data of type $expectedType, got $type." }
        check(clientData["challenge"]?.jsonPrimitive?.content == issuedChallenge) {
            "The client data does not carry the issued challenge."
        }
    }

    // The same user name always maps to the same user handle.
    private fun userHandle(username: String): String = MessageDigest.getInstance("SHA-256").digest(username.toByteArray()).toBase64url()

    private companion object {
        const val RP_ID = "example.com"
        const val RP_NAME = "Fake Relying Party"
        const val CHALLENGE_SIZE = 32
    }
}
