package com.example.anibey_codex_tfg.ui.screens.login
enum class RegistrationStep {
    CREDENTIALS, // Email y Password
    CHARACTER_INFO, // Nombre de usuario / Apodo
    FINALIZING // Confirmación
}

data class LoginActions(
    val onEmailChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onUsernameChange: (String) -> Unit = {},
    val onNextStep: () -> Unit = {},
    val onBackStep: () -> Unit = {},
    val onLoginSubmit: () -> Unit = {}
)
data class LoginState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val currentStep: RegistrationStep = RegistrationStep.CREDENTIALS,
    val isLoading: Boolean = false
)