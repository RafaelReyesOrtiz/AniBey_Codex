package com.example.anibey_codex_tfg.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.data.local.datastore.SessionDataStore
import com.example.anibey_codex_tfg.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.actionCodeSettings
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set

    fun setIsRegistering(value: Boolean) {
        state = state.copy(isRegistering = value)
    }

    fun onEmailChange(newValue: String) {
        state = state.copy(email = newValue, emailError = null)
    }

    fun onUsernameChange(newValue: String) {
        state = state.copy(username = newValue, usernameError = null)
    }

    private fun validateCurrentStep(): Boolean {
        return when (state.currentStep) {
            RegistrationStep.CREDENTIALS -> {
                val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
                state = state.copy(emailError = if (!emailValid) "Email no reconocido" else null)
                emailValid
            }
            RegistrationStep.CHARACTER_INFO -> {
                val userValid = state.username.isNotBlank()
                state = state.copy(usernameError = if (!userValid) "El nombre es obligatorio" else null)
                userValid
            }
            RegistrationStep.FINALIZING -> true
        }
    }

    fun onNextStep() {
        if (validateCurrentStep()) {
            val nextOrdinal = state.currentStep.ordinal + 1
            if (nextOrdinal < RegistrationStep.entries.size) {
                state = state.copy(currentStep = RegistrationStep.entries[nextOrdinal])
            }
        }
    }

    fun onBackStep() {
        val prevOrdinal = state.currentStep.ordinal - 1
        if (prevOrdinal >= 0) {
            state = state.copy(currentStep = RegistrationStep.entries[prevOrdinal])
        }
    }

    fun onLoginSubmit() {
        if (!validateCurrentStep()) return
        sendSpiritWhisper()
    }

    fun sendSpiritWhisper() {
        state = state.copy(isLoading = true)
        val actionCodeSettings = actionCodeSettings {
            url = "https://anibeycodex.page.link/finish_auth"
            handleCodeInApp = true
            setAndroidPackageName("com.example.anibey_codex_tfg", true, "24")
        }

        auth.sendSignInLinkToEmail(state.email, actionCodeSettings)
            .addOnSuccessListener {
                state = state.copy(isLoading = false, isCodeSent = true)
            }
            .addOnFailureListener {
                state = state.copy(isLoading = false, emailError = "Error: ${it.localizedMessage}")
            }
    }

    // Esta es la función clave que llamará el NavHost
    fun completeSpiritLink(emailLink: String, isRegister: Boolean, onComplete: () -> Unit) {
        if (auth.isSignInWithEmailLink(emailLink)) {
            state = state.copy(isLoading = true)
            auth.signInWithEmailLink(state.email, emailLink)
                .addOnSuccessListener { authResult ->
                    checkUserInFirestore(authResult.user, isRegister, onComplete)
                }
                .addOnFailureListener {
                    state = state.copy(isLoading = false, emailError = "Vínculo roto")
                }
        }
    }

    private fun checkUserInFirestore(user: FirebaseUser?, isRegister: Boolean, onComplete: () -> Unit) {
        val uid = user?.uid ?: return
        val email = user.email ?: ""

        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val profile = doc.toObject(UserProfile::class.java)
                profile?.let {
                    viewModelScope.launch {
                        sessionDataStore.saveSession(it)
                        state = state.copy(isLoading = false)
                        onComplete() // Irá a Home (Login)
                    }
                }
            } else if (isRegister) {
                val newProfile = UserProfile(email = email, username = state.username)
                db.collection("users").document(uid).set(newProfile).addOnSuccessListener {
                    state = state.copy(isLoading = false)
                    onComplete() // Irá a Welcome (Registro)
                }
            }
        }
    }

    fun resetSpiritWhisper() {
        state = state.copy(isCodeSent = false, isLoading = false)
    }
}