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

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.lycorp.webauthn.sample.R

class AuthenticatorSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticator_selection)

        val biometricButton: Button = findViewById(R.id.btn_biometric)
        val deviceCredentialButton: Button = findViewById(R.id.btn_device_credential)

        biometricButton.setOnClickListener {
            navigateToMainScreen("biometric")
        }

        deviceCredentialButton.setOnClickListener {
            navigateToMainScreen("device_credential")
        }
    }

    private fun navigateToMainScreen(authType: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("authenticator_setup", true)
            apply()
        }

        val intent =
            Intent(this, MainActivity::class.java).apply {
                putExtra("auth_type", authType)
            }
        startActivity(intent)
        finish()
    }
}
