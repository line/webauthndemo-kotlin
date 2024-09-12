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

package com.lycorp.webauthn.sample.network

import com.lycorp.webauthn.sample.data.remote.api.RetrofitFido2Api
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    private val BASE_URL_FIDO2 = "https://example.com"

    private fun createOkHttpClient(): OkHttpClient {
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val bypassTrustManager: X509TrustManager =
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?,
                ) {}

                override fun checkServerTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?,
                ) {}

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(bypassTrustManager), SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, bypassTrustManager)
            .hostnameVerifier { _hostname, _session -> true }
            .cookieJar(JavaNetCookieJar(cookieManager))
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        val client = createOkHttpClient()
        return Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }

    fun createFido2Api(): RetrofitFido2Api {
        val apiClass = RetrofitFido2Api::class.java
        val retrofit = createRetrofit(baseUrl = BASE_URL_FIDO2)
        return retrofit.create(apiClass)
    }
}
