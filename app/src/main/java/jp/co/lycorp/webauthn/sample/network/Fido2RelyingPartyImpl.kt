package jp.co.lycorp.webauthn.sample.network

import jp.co.lycorp.webauthn.model.AttestationConveyancePreference
import jp.co.lycorp.webauthn.model.COSEAlgorithmIdentifier
import jp.co.lycorp.webauthn.model.ClientExtensionInput
import jp.co.lycorp.webauthn.model.PublicKeyCredentialCreateResult
import jp.co.lycorp.webauthn.model.PublicKeyCredentialGetResult
import jp.co.lycorp.webauthn.model.PublicKeyCredentialParams
import jp.co.lycorp.webauthn.model.PublicKeyCredentialRpEntity
import jp.co.lycorp.webauthn.model.PublicKeyCredentialType
import jp.co.lycorp.webauthn.model.PublicKeyCredentialUserEntity
import jp.co.lycorp.webauthn.model.UserVerificationRequirement
import jp.co.lycorp.webauthn.rp.AuthenticationData
import jp.co.lycorp.webauthn.rp.AuthenticationOptions
import jp.co.lycorp.webauthn.rp.RegistrationData
import jp.co.lycorp.webauthn.rp.RegistrationOptions
import jp.co.lycorp.webauthn.rp.RelyingParty
import jp.co.lycorp.webauthn.sample.data.remote.model.GetAuthenticationChallengeInput
import jp.co.lycorp.webauthn.sample.data.remote.model.GetAuthenticationChallengeResult
import jp.co.lycorp.webauthn.sample.data.remote.model.GetRegistrationChallengeInput
import jp.co.lycorp.webauthn.sample.data.remote.model.GetRegistrationChallengeResult
import jp.co.lycorp.webauthn.sample.data.remote.model.ResponseInAuthenticationResult
import jp.co.lycorp.webauthn.sample.data.remote.model.ResponseInRegistrationResult
import jp.co.lycorp.webauthn.sample.data.remote.model.SendAuthenticationResponseInput
import jp.co.lycorp.webauthn.sample.data.remote.model.SendRegistrationResponseInput
import jp.co.lycorp.webauthn.sample.domain.remote.Fido2Api
import jp.co.lycorp.webauthn.util.toBase64url

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
