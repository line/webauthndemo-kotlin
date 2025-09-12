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

package com.linecorp.webauthn.sample.presentation.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.linecorp.webauthn.db.CredentialSourceStorage
import com.linecorp.webauthn.model.AttestationStatementFormat
import com.linecorp.webauthn.model.AuthenticationMethod
import com.linecorp.webauthn.model.Fido2PromptInfo
import com.linecorp.webauthn.publickeycredential.PublicKeyCredential
import com.linecorp.webauthn.rp.RelyingParty
import com.linecorp.webauthn.sample.R
import com.linecorp.webauthn.sample.data.database.RoomCredentialSourceStorage
import com.linecorp.webauthn.sample.network.Fido2RelyingPartyImpl
import com.linecorp.webauthn.sample.presentation.viewmodel.Fido2ViewModel
import com.linecorp.webauthn.sample.presentation.viewmodel.Fido2ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity :
    AppCompatActivity(),
    FragmentInteractionListener {
    private val viewModel by viewModels<Fido2ViewModel> {
        val rpClient: RelyingParty = Fido2RelyingPartyImpl()
        val db: CredentialSourceStorage = RoomCredentialSourceStorage.build(this)
        val authType = intent.getStringExtra("auth_type")
        val authenticationMethod: AuthenticationMethod
        val attestationStatement: AttestationStatementFormat

        when (authType) {
            "biometric_none" -> {
                authenticationMethod = AuthenticationMethod.Biometric
                attestationStatement = AttestationStatementFormat.NONE
            }
            "biometric_androidkey" -> {
                authenticationMethod = AuthenticationMethod.Biometric
                attestationStatement = AttestationStatementFormat.ANDROID_KEY
            }
            "device_credential_none" -> {
                authenticationMethod = AuthenticationMethod.DeviceCredential
                attestationStatement = AttestationStatementFormat.NONE
            }
            "device_credential_androidkey" -> {
                authenticationMethod = AuthenticationMethod.DeviceCredential
                attestationStatement = AttestationStatementFormat.ANDROID_KEY
            }
            else -> {
                throw IllegalArgumentException("Invalid authentication method")
            }
        }

        val publicKeyCredential =
            PublicKeyCredential(
                rpClient = rpClient,
                db = db,
                authenticationMethod = authenticationMethod,
                attestationStatement = attestationStatement,
            )

        val fido2PromptInfo =
            Fido2PromptInfo(
                title = "User Authentication",
                description = "Authenticate yourself using fingerprint or device credential.",
            )

        Fido2ViewModelFactory(publicKeyCredential, fido2PromptInfo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            replaceFragment(DefaultFragment(), Fido2ViewModel.FRAGMENT_DEFAULT)
            updateButtonAreaForDefault()
        }

        setAccountButtons()
        setNameBlock()
        setFragment()
        setLogWindow()
    }

    override fun onBackPressed() {
        val intent = Intent(this, AuthenticatorSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setAccountButtons() {
        findViewById<Button>(R.id.showAccountButton).setOnClickListener {
            viewModel.showAllAccounts()
            findViewById<Button>(R.id.logWindowOpenButton).performClick()
        }

        findViewById<Button>(R.id.deleteAccountButton).setOnClickListener {
            viewModel.deleteAllAccounts()
            findViewById<Button>(R.id.logWindowOpenButton).performClick()
        }
    }

    private fun setFragment() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentFragment.collect { fragmentTag ->
                    when (fragmentTag) {
                        Fido2ViewModel.FRAGMENT_DEFAULT -> {
                            replaceFragment(DefaultFragment(), Fido2ViewModel.FRAGMENT_DEFAULT)
                            updateButtonAreaForDefault()
                        }
                        Fido2ViewModel.FRAGMENT_SIGN_UP -> {
                            replaceFragment(SignUpFragment(), Fido2ViewModel.FRAGMENT_SIGN_UP)
                            updateButtonAreaForSignUp()
                        }
                        Fido2ViewModel.FRAGMENT_SIGN_IN -> {
                            replaceFragment(SignInFragment(), Fido2ViewModel.FRAGMENT_SIGN_IN)
                            updateButtonAreaForSignIn()
                        }
                    }
                }
            }
        }
    }

    private fun setNameBlock() {
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val displayNameEditText = findViewById<EditText>(R.id.displayNameEditText)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.name.collect { name ->
                    if (nameEditText.text.toString() != name) {
                        nameEditText.setText(name)
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.displayName.collect { displayName ->
                    if (displayNameEditText.text.toString() != displayName) {
                        displayNameEditText.setText(displayName)
                    }
                }
            }
        }

        nameEditText.doAfterTextChanged { text ->
            viewModel.updateName(text.toString())
        }

        displayNameEditText.doAfterTextChanged { text ->
            viewModel.updateDisplayName(text.toString())
        }
    }

    private fun setLogWindow() {
        val logWindowOpenButton = findViewById<Button>(R.id.logWindowOpenButton)
        val logWindowCloseButton = findViewById<Button>(R.id.logWindowCloseButton)
        val logWindowContainer = findViewById<FrameLayout>(R.id.logWindowContainer)

        logWindowOpenButton.visibility = View.VISIBLE
        logWindowCloseButton.visibility = View.GONE

        logWindowOpenButton.setOnClickListener {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val logWindowFragment = LogWindowFragment()

            fragmentTransaction.add(R.id.logWindowContainer, logWindowFragment)
            fragmentTransaction.commit()

            logWindowContainer.visibility = View.VISIBLE
            logWindowOpenButton.visibility = View.GONE
            logWindowCloseButton.visibility = View.VISIBLE
        }

        logWindowCloseButton.setOnClickListener {
            val logWindowFragment = supportFragmentManager.findFragmentById(R.id.logWindowContainer)
            if (logWindowFragment != null) {
                supportFragmentManager.beginTransaction().remove(logWindowFragment).commit()
            }

            logWindowContainer.visibility = View.GONE
            logWindowOpenButton.visibility = View.VISIBLE
            logWindowCloseButton.visibility = View.GONE
        }
    }

    private fun replaceFragment(
        fragment: Fragment,
        tag: String,
    ) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.optionContainer, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    override fun updateButtonAreaForDefault() {
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val signInButton = findViewById<Button>(R.id.signInButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        val defaultSignUpButton = findViewById<Button>(R.id.defaultSignUpButton)
        val defaultSignInButton = findViewById<Button>(R.id.defaultSignInButton)

        signUpButton.visibility = View.GONE
        signInButton.visibility = View.GONE
        cancelButton.visibility = View.GONE
        defaultSignUpButton.visibility = View.VISIBLE
        defaultSignInButton.visibility = View.VISIBLE

        defaultSignUpButton.setOnClickListener {
            viewModel.showSignUpFragment()
        }

        defaultSignInButton.setOnClickListener {
            viewModel.showSignInFragment()
        }
    }

    override fun updateButtonAreaForSignUp() {
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val signInButton = findViewById<Button>(R.id.signInButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        val defaultSignUpButton = findViewById<Button>(R.id.defaultSignUpButton)
        val defaultSignInButton = findViewById<Button>(R.id.defaultSignInButton)

        signUpButton.visibility = View.VISIBLE
        signInButton.visibility = View.GONE
        cancelButton.visibility = View.VISIBLE
        defaultSignUpButton.visibility = View.GONE
        defaultSignInButton.visibility = View.GONE

        signUpButton.setOnClickListener {
            viewModel.signUp(this)
            findViewById<Button>(R.id.logWindowOpenButton).performClick()
        }

        cancelButton.setOnClickListener {
            viewModel.showDefaultFragment()
            findViewById<Button>(R.id.logWindowCloseButton).performClick()
        }
    }

    override fun updateButtonAreaForSignIn() {
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val signInButton = findViewById<Button>(R.id.signInButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        val defaultSignUpButton = findViewById<Button>(R.id.defaultSignUpButton)
        val defaultSignInButton = findViewById<Button>(R.id.defaultSignInButton)

        signUpButton.visibility = View.GONE
        signInButton.visibility = View.VISIBLE
        cancelButton.visibility = View.VISIBLE
        defaultSignUpButton.visibility = View.GONE
        defaultSignInButton.visibility = View.GONE

        signInButton.setOnClickListener {
            viewModel.signIn(this)
            findViewById<Button>(R.id.logWindowOpenButton).performClick()
        }

        cancelButton.setOnClickListener {
            viewModel.showDefaultFragment()
            findViewById<Button>(R.id.logWindowCloseButton).performClick()
        }
    }
}
