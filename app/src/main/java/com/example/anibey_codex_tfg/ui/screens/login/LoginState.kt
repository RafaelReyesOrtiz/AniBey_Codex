package com.example.anibey_codex_tfg.ui.screens.login

enum class RegistrationStep {
    CREDENTIALS,
    CHARACTER_INFO,
    FINALIZING
}

data class LoginState(
    val email: String = "",
    val emailError: String? = null,
    val username: String = "",
    val usernameError: String? = null,
    val isCodeSent: Boolean = false,
    val isRegistering: Boolean = false, // Para saber en qué flujo estamos al volver del mail
    val currentStep: RegistrationStep = RegistrationStep.CREDENTIALS,
    val isLoading: Boolean = false
)

data class LoginActions(
    val onEmailChange: (String) -> Unit = {},
    val onUsernameChange: (String) -> Unit = {},
    val onNextStep: () -> Unit = {},
    val onBackStep: () -> Unit = {},
    val onLoginSubmit: () -> Unit = {},
    val onGoogleClick: () -> Unit = {},
    val onResetSpiritWhisper: () -> Unit = {}
)