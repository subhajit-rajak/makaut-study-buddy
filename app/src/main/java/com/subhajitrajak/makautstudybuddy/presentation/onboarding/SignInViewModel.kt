package com.subhajitrajak.makautstudybuddy.presentation.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.subhajitrajak.makautstudybuddy.data.auth.SignInResult
import com.subhajitrajak.makautstudybuddy.data.auth.SignInState

class SignInViewModel : ViewModel() {

    private val _state = MutableLiveData(SignInState())
    val state: LiveData<SignInState> = _state

    fun onSignInResult(result: SignInResult) {
        _state.value = SignInState(
            isSignInSuccessful = result.data != null,
            signInError = result.errorMessage
        )
    }

    fun resetState() {
        _state.value = SignInState()
    }
}