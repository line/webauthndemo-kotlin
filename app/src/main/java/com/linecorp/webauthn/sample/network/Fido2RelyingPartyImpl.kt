package com.linecorp.webauthn.sample.network

import com.linecorp.webauthn.model.AttestationConveyancePreference
import com.linecorp.webauthn.model.COSEAlgorithmIdentifier
import com.linecorp.webauthn.model.ClientExtensionInput
import com.linecorp.webauthn.model.PublicKeyCredentialCreateResult
import com.linecorp.webauthn.model.PublicKeyCredentialGetResult
import com.linecorp.webauthn.model.PublicKeyCredentialParams
import com.linecorp.webauthn.model.PublicKeyCredentialRpEntity
import com.linecorp.webauthn.model.PublicKeyCredentialType
import com.linecorp.webauthn.model.PublicKeyCredentialUserEntity
import com.linecorp.webauthn.model.UserVerificationRequirement
import com.linecorp.webauthn.rp.AuthenticationData
import com.linecorp.webauthn.rp.AuthenticationOptions
import com.linecorp.webauthn.rp.RegistrationData
import com.linecorp.webauthn.rp.RegistrationOptions
import com.linecorp.webauthn.rp.RelyingParty
import com.linecorp.webauthn.sample.data.remote.model.GetAuthenticationChallengeInput
import com.linecorp.webauthn.sample.data.remote.model.GetAuthenticationChallengeResult
import com.linecorp.webauthn.sample.data.remote.model.GetRegistrationChallengeInput
import com.linecorp.webauthn.sample.data.remote.model.GetRegistrationChallengeResult
import com.linecorp.webauthn.sample.data.remote.model.ResponseInAuthenticationResult
import com.linecorp.webauthn.sample.data.remote.model.ResponseInRegistrationResult
import com.linecorp.webauthn.sample.data.remote.model.SendAuthenticationResponseInput
import com.linecorp.webauthn.sample.data.remote.model.SendRegistrationResponseInput
import com.linecorp.webauthn.sample.domain.remote.Fido2Api
import com.linecorp.webauthn.util.toBase64url

class Fido2RelyingPartyImpl(
    private val api: Fido2Api = RetrofitClient.createFido2Api(),
) : RelyingParty {
    override suspend fun getAuthenticationData(options: AuthenticationOptions): AuthenticationData {
        val data =
            GetAuthenticationChallengeInput(
                userVerification = options.userVerification.value,
                username = options.username,
            )
        val response: GetAuthenticationChallengeResult =
            api.getAuthenticationChallenge(
                data,
            )

        return AuthenticationData(
            allowCredentials = response.allowCredentials,
            challenge = response.challenge,
            extensions = response.extensions,
            rpId = response.rpId,
            userVerification = UserVerificationRequirement.fromValue(response.userVerification)!!,
        )
    }

    override suspend fun getRegistrationData(options: RegistrationOptions): RegistrationData {
        val data =
            GetRegistrationChallengeInput(
                attestation = options.attestation.value,
                authenticatorSelection = options.authenticatorSelection,
                credProtect = options.credProtect,
                displayName = options.displayName,
                username = options.username,
            )
        val response: GetRegistrationChallengeResult =
            api.getRegistrationChallenge(
                data,
            )

        return RegistrationData(
            attestation = AttestationConveyancePreference.fromValue(response.attestation)!!,
            authenticatorSelection = response.authenticatorSelection,
            challenge = response.challenge,
            excludeCredentials = response.excludeCredentials,
            extensions =
                ClientExtensionInput(),
            pubKeyCredParams =
                response.pubKeyCredParams.map {
                    PublicKeyCredentialParams(
                        PublicKeyCredentialType.fromValue(it.type)!!,
                        COSEAlgorithmIdentifier.fromValue(it.alg.toLong())!!,
                    )
                },
            rp =
                PublicKeyCredentialRpEntity(
                    id = response.rp.id,
                    name = response.rp.name,
                ),
            user =
                PublicKeyCredentialUserEntity(
                    id = response.user.id,
                    name = response.user.name,
                    displayName = response.user.displayName,
                ),
        )
    }

    override suspend fun verifyAuthentication(result: PublicKeyCredentialGetResult) {
        val response = result.authenticatorAssertionResponse
        api.sendAuthenticationResponse(
            SendAuthenticationResponseInput(
                id = result.id,
                rawId = result.id,
                extensions = result.clientExtensionsOutput,
                response =
                    ResponseInAuthenticationResult(
                        authenticatorData = response.authenticatorData.toBase64url(),
                        clientDataJSON = response.clientDataJSON.toBase64url(),
                        signature = response.signature.toBase64url(),
                        userHandle = response.userHandle?.toBase64url(),
                    ),
                type = result.type,
            ),
        )
    }

    override suspend fun verifyRegistration(result: PublicKeyCredentialCreateResult) {
        api.sendRegistrationResponse(
            SendRegistrationResponseInput(
                id = result.id,
                rawId = result.id,
                extensions = result.clientExtensionsOutput,
                response =
                    ResponseInRegistrationResult(
                        attestationObject =
                            result.authenticatorAttestationResponse.attestationObject.toBase64url(),
                        clientDataJSON =
                            result.authenticatorAttestationResponse.clientDataJSON.toBase64url(),
                        transports = null,
                    ),
                type = result.type,
            ),
        )
    }
}
