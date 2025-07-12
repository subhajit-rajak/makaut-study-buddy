package com.subhajitrajak.makautstudybuddy.data.auth

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)