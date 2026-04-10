package com.example.anibey_codex_tfg.ui.screens.register

enum class RegisterStep {
    EMAIL_ENTRY,      // Fase 1: Introducir email
    VERIFY_PENDING,   // Fase 2: Bloqueo (esperando link de verificación)
    FINALIZE_PACT     // Fase 3: Introducir apodo y contraseña final
}

data class RegisterState(
    val resendCountdown: Int = 0,
    val canResend: Boolean = true,
    val email: String = "",
    val emailError: String? = null,
    val username: String = "",
    val usernameError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val currentStep: RegisterStep = RegisterStep.EMAIL_ENTRY,
    val isLoading: Boolean = false,
    val isVerified: Boolean = false
)

data class RegisterActions(
    val onEmailChange: (String) -> Unit = {},
    val onUsernameChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onSendVerification: () -> Unit = {},
    val onCheckVerification: () -> Unit = {},
    val onFinalizePact: (onSuccess: () -> Unit) -> Unit = { _ -> },
    val onBack: () -> Unit = {},
    val onRegisterSuccess : () -> Unit = {}
)