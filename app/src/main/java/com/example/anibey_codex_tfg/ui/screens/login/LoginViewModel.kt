package com.example.anibey_codex_tfg.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set

    fun onEmailChange(newValue: String) {
        state = state.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        state = state.copy(password = newValue)
    }

    fun onUsernameChange(newValue: String) {
        state = state.copy(username = newValue)
    }

    fun onNextStep() {
        val nextOrdinal = state.currentStep.ordinal + 1
        if (nextOrdinal < RegistrationStep.entries.size) {
            state = state.copy(currentStep = RegistrationStep.entries[nextOrdinal])
        }
    }

    fun onBackStep() {
        val prevOrdinal = state.currentStep.ordinal - 1
        if (prevOrdinal >= 0) {
            state = state.copy(currentStep = RegistrationStep.entries[prevOrdinal])
        }
    }

    fun onLoginSubmit() {
        // Aquí iría la llamada a Firebase o tu repositorio
        state = state.copy(isLoading = true)

        // Simulación de éxito
        println("Registrando a ${state.username} con el email ${state.email}")
    }
}