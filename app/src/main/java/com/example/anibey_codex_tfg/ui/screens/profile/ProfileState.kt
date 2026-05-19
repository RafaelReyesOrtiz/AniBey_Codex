package com.example.anibey_codex_tfg.ui.screens.profile

data class ProfileState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val currentPassword: String = "",
    val photoUrl: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val currentPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = true,
    val updateSuccess: Boolean = false,
    val isDiscardDialogOpen: Boolean = false,
    val isCheckingEmailVerification: Boolean = false,
    val shouldNavigateToWelcome: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)