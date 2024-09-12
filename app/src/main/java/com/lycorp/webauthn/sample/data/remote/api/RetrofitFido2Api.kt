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

package com.lycorp.webauthn.sample.data.remote.api

import com.lycorp.webauthn.sample.data.remote.model.GetAuthenticationChallengeInput
import com.lycorp.webauthn.sample.data.remote.model.GetAuthenticationChallengeResult
import com.lycorp.webauthn.sample.data.remote.model.GetRegistrationChallengeInput
import com.lycorp.webauthn.sample.data.remote.model.GetRegistrationChallengeResult
import com.lycorp.webauthn.sample.data.remote.model.SendAuthenticationResponseInput
import com.lycorp.webauthn.sample.data.remote.model.SendRegistrationResponseInput
import com.lycorp.webauthn.sample.data.remote.model.SendResponseResult
import com.lycorp.webauthn.sample.domain.remote.Fido2Api
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitFido2Api : Fido2Api {
    @POST("assertion/options")
    override suspend fun getAuthenticationChallenge(
        @Body body: GetAuthenticationChallengeInput,
    ): GetAuthenticationChallengeResult

    @POST("assertion/result")
    override suspend fun sendAuthenticationResponse(
        @Body body: SendAuthenticationResponseInput,
    ): SendResponseResult

    @POST("attestation/options")
    override suspend fun getRegistrationChallenge(
        @Body body: GetRegistrationChallengeInput,
    ): GetRegistrationChallengeResult

    @POST("attestation/result")
    override suspend fun sendRegistrationResponse(
        @Body body: SendRegistrationResponseInput,
    ): SendResponseResult
}
