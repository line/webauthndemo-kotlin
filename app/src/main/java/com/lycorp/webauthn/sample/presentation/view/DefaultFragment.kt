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

import android.content.Context
import androidx.fragment.app.Fragment

class DefaultFragment : Fragment() {
    private var interactionListener: FragmentInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentInteractionListener) {
            interactionListener = context
        } else {
            throw RuntimeException("$context must implement FragmentInteractionListener")
        }
    }

    override fun onResume() {
        super.onResume()
        interactionListener?.updateButtonAreaForDefault()
    }
}
