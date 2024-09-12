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
import android.view.View
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lycorp.webauthn.db.CredentialSourceStorage
import com.lycorp.webauthn.publickeycredential.Biometric
import com.lycorp.webauthn.publickeycredential.DeviceCredential
import com.lycorp.webauthn.rp.RelyingParty
import com.lycorp.webauthn.sample.R
import com.lycorp.webauthn.sample.data.database.RoomCredentialSourceStorage
import com.lycorp.webauthn.sample.databinding.ActivityMainBinding
import com.lycorp.webauthn.sample.network.Fido2RelyingPartyImpl
import com.lycorp.webauthn.sample.presentation.viewmodel.Fido2ViewModel
import com.lycorp.webauthn.sample.presentation.viewmodel.Fido2ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity :
    AppCompatActivity(),
    FragmentInteractionListener {
    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<Fido2ViewModel> {
        val rpClient: RelyingParty = Fido2RelyingPartyImpl()
        val db: CredentialSourceStorage = RoomCredentialSourceStorage.build(this)
        val authType = intent.getStringExtra("auth_type")

        val publicKeyCredential =
            when (authType) {
                "biometric" -> {
                    Biometric(
                        rpClient = rpClient,
                        db = db,
                        activity = this,
                    )
                }
                "device_credential" -> {
                    DeviceCredential(
                        rpClient = rpClient,
                        db = db,
                        activity = this,
                    )
                }
                else -> {
                    throw IllegalArgumentException("Invalid authentication method")
                }
            }
        Fido2ViewModelFactory(publicKeyCredential)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

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
        binding.apply {
            showAccountButton.setOnClickListener {
                viewModel?.showAllAccounts()
                logWindowOpenButton.performClick()
            }

            deleteAccountButton.setOnClickListener {
                viewModel?.deleteAllAccounts()
                logWindowOpenButton.performClick()
            }
        }
    }

    private fun setFragment() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentFragment.collect { fragmentTag ->
                    when (fragmentTag) {
                        Fido2ViewModel.FRAGMENT_DEFAULT -> {
                            replaceFragment(com.lycorp.webauthn.sample.presentation.view.DefaultFragment(), Fido2ViewModel.FRAGMENT_DEFAULT)
                            updateButtonAreaForDefault()
                        }
                        Fido2ViewModel.FRAGMENT_SIGN_UP -> {
                            replaceFragment(com.lycorp.webauthn.sample.presentation.view.SignUpFragment(), Fido2ViewModel.FRAGMENT_SIGN_UP)
                            updateButtonAreaForSignUp()
                        }
                        Fido2ViewModel.FRAGMENT_SIGN_IN -> {
                            replaceFragment(com.lycorp.webauthn.sample.presentation.view.SignInFragment(), Fido2ViewModel.FRAGMENT_SIGN_IN)
                            updateButtonAreaForSignIn()
                        }
                    }
                }
            }
        }
    }

    private fun setNameBlock() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.name.collect { name ->
                    if (binding.nameEditText.text.toString() != name) {
                        binding.nameEditText.setText(name)
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.displayName.collect { displayName ->
                    if (binding.displayNameEditText.text.toString() != displayName) {
                        binding.displayNameEditText.setText(displayName)
                    }
                }
            }
        }

        binding.nameEditText.doAfterTextChanged { text ->
            viewModel.updateName(text.toString())
        }

        binding.displayNameEditText.doAfterTextChanged { text ->
            viewModel.updateDisplayName(text.toString())
        }
    }

    private fun setLogWindow() {
        binding.logWindowOpenButton.visibility = View.VISIBLE
        binding.logWindowCloseButton.visibility = View.GONE

        binding.logWindowOpenButton.setOnClickListener {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val logWindowFragment = com.lycorp.webauthn.sample.presentation.view.LogWindowFragment()

            fragmentTransaction.add(R.id.logWindowContainer, logWindowFragment)
            fragmentTransaction.commit()

            findViewById<FrameLayout>(R.id.logWindowContainer).visibility = View.VISIBLE
            binding.logWindowOpenButton.visibility = View.GONE
            binding.logWindowCloseButton.visibility = View.VISIBLE
        }

        binding.logWindowCloseButton.setOnClickListener {
            val logWindowFragment = supportFragmentManager.findFragmentById(R.id.logWindowContainer)
            if (logWindowFragment != null) {
                supportFragmentManager.beginTransaction().remove(logWindowFragment).commit()
            }

            findViewById<FrameLayout>(R.id.logWindowContainer).visibility = View.GONE

            binding.logWindowOpenButton.visibility = View.VISIBLE
            binding.logWindowCloseButton.visibility = View.GONE
        }

        binding.logWindowCloseButton.setOnClickListener {
            val logWindowFragment = supportFragmentManager.findFragmentById(R.id.logWindowContainer)
            if (logWindowFragment != null) {
                supportFragmentManager.beginTransaction().remove(logWindowFragment).commit()
            }

            findViewById<FrameLayout>(R.id.logWindowContainer).visibility = View.GONE

            binding.logWindowOpenButton.visibility = View.VISIBLE
            binding.logWindowCloseButton.visibility = View.GONE
        }
    }

    private fun replaceFragment(
        fragment: Fragment,
        tag: String,
    ) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.optionContainer, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    override fun updateButtonAreaForDefault() {
        binding.apply {
            signUpButton.visibility = View.GONE
            signInButton.visibility = View.GONE
            cancelButton.visibility = View.GONE
            defaultSignUpButton.visibility = View.VISIBLE
            defaultSignInButton.visibility = View.VISIBLE

            defaultSignUpButton.setOnClickListener {
                viewModel?.showSignUpFragment()
            }

            defaultSignInButton.setOnClickListener {
                viewModel?.showSignInFragment()
            }
        }
    }

    override fun updateButtonAreaForSignUp() {
        binding.apply {
            signUpButton.visibility = View.VISIBLE
            signInButton.visibility = View.GONE
            cancelButton.visibility = View.VISIBLE
            defaultSignUpButton.visibility = View.GONE
            defaultSignInButton.visibility = View.GONE

            signUpButton.setOnClickListener {
                viewModel?.signUp()
                logWindowOpenButton.performClick()
            }

            cancelButton.setOnClickListener {
                viewModel?.showDefaultFragment()
                logWindowCloseButton.performClick()
            }
        }
    }

    override fun updateButtonAreaForSignIn() {
        binding.apply {
            signUpButton.visibility = View.GONE
            signInButton.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
            defaultSignUpButton.visibility = View.GONE
            defaultSignInButton.visibility = View.GONE

            signInButton.setOnClickListener {
                viewModel?.signIn()
                logWindowOpenButton.performClick()
            }

            cancelButton.setOnClickListener {
                viewModel?.showDefaultFragment()
                logWindowCloseButton.performClick()
            }
        }
    }
}
