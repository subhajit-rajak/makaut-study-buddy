package com.subhajitrajak.makautstudybuddy.data.repository.userLogin

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)