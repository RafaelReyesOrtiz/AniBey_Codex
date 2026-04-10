package com.example.anibey_codex_tfg.ui.screens.register

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.data.local.datastore.SessionDataStore
import com.example.anibey_codex_tfg.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    var state by mutableStateOf(RegisterState())
        private set

    fun onEmailChange(newValue: String) {
        state = state.copy(email = newValue, emailError = null)
    }

    fun onUsernameChange(newValue: String) {
        state = state.copy(username = newValue, usernameError = null)
    }

    fun onPasswordChange(newValue: String) {
        state = state.copy(password = newValue, passwordError = null)
    }

    fun sendVerificationEmail() {
        val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
        if (!emailValid) {
            state = state.copy(emailError = "El e-mail no es reconocido por Gaia")
            return
        }

        state = state.copy(isLoading = true)

        auth.createUserWithEmailAndPassword(state.email, "temp_pass_anima_123")
            .addOnSuccessListener { result ->
                sendEmailAndProgress(result.user)
            }
            .addOnFailureListener { exception ->
                if (exception is FirebaseAuthUserCollisionException) {
                    auth.signInWithEmailAndPassword(state.email, "temp_pass_anima_123")
                        .addOnSuccessListener { result ->
                            val user = result.user
                            if (user != null && !user.isEmailVerified) {
                                sendEmailAndProgress(user)
                            } else {
                                state = state.copy(
                                    isLoading = false,
                                    emailError = "Este alma ya está vinculada. Ve al inicio de sesión."
                                )
                            }
                        }
                        .addOnFailureListener {
                            state = state.copy(
                                isLoading = false,
                                emailError = "Este alma ya pertenece a otro viajero."
                            )
                        }
                } else {
                    state = state.copy(isLoading = false, emailError = "Error en el vínculo: ${exception.localizedMessage}")
                }
            }
    }

    private fun sendEmailAndProgress(user: com.google.firebase.auth.FirebaseUser?) {
        if (user == null) {
            state = state.copy(isLoading = false, emailError = "Error: Usuario no encontrado.")
            return
        }

        user.sendEmailVerification()
            .addOnSuccessListener {
                state = state.copy(
                    isLoading = false,
                    currentStep = RegisterStep.VERIFY_PENDING,
                    emailError = null
                )
                startResendTimer()
            }
            .addOnFailureListener { exception ->
                state = state.copy(
                    isLoading = false,
                    emailError = "No se pudo enviar el correo: ${exception.localizedMessage}"
                )
            }
    }

    fun checkVerificationStatus() {
        val user = auth.currentUser
        if (user == null) {
            state = state.copy(emailError = "Sesión perdida. Reintenta.")
            return
        }

        state = state.copy(isLoading = true)

        user.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (user.isEmailVerified) {
                    state = state.copy(
                        isLoading = false,
                        isVerified = true,
                        currentStep = RegisterStep.FINALIZE_PACT,
                        emailError = null
                    )
                } else {
                    state = state.copy(
                        isLoading = false,
                        emailError = "El lazo aún no ha sido confirmado en tu correo."
                    )
                }
            } else {
                state = state.copy(isLoading = false, emailError = "Error al conectar con el vacío.")
            }
        }
    }

    private var timerJob: Job? = null

    fun startResendTimer() {
        state = state.copy(canResend = false, resendCountdown = 60)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (state.resendCountdown > 0) {
                delay(1000)
                state = state.copy(resendCountdown = state.resendCountdown - 1)
            }
            state = state.copy(canResend = true)
        }
    }

    fun editEmail() {
        auth.currentUser?.delete()?.addOnCompleteListener {
            state = state.copy(
                currentStep = RegisterStep.EMAIL_ENTRY,
                isVerified = false,
                resendCountdown = 0,
                canResend = true,
                emailError = null
            )
            timerJob?.cancel()
        }
    }

    fun finalizeRegistration(onSuccess: () -> Unit) {
        if (state.password.length < 6 || state.username.isBlank()) {
            state = state.copy(
                passwordError = if (state.password.length < 6) "La clave debe tener al menos 6 carácteres." else null,
                usernameError = if (state.username.isBlank()) "Todo viajero necesita un apodo." else null
            )
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val user = auth.currentUser

            if (user == null) {
                state = state.copy(isLoading = false, passwordError = "Sesión perdida. Reintenta.")
                return@launch
            }

            try {
                // 1. Actualizamos a la contraseña definitiva
                Log.d("REGISTER", "Actualizando contraseña...")
                user.updatePassword(state.password).await()

                // 2. Creamos el perfil en Firestore
                val profile = UserProfile(
                    email = state.email,
                    username = state.username,
                    photoUrl = null
                )
                
                Log.d("REGISTER", "Guardando en Firestore...")
                // ESTA LÍNEA ES LA QUE SE QUEDABA PILLADA
                db.collection("users").document(user.uid).set(profile).await()

                // 3. Guardar sesión local
                Log.d("REGISTER", "Guardando sesión local...")
                sessionDataStore.saveSession(profile)

                state = state.copy(isLoading = false)
                onSuccess()

            } catch (e: Exception) {
                Log.e("REGISTER", "Error fatal: ${e.localizedMessage}")
                state = state.copy(
                    isLoading = false,
                    passwordError = "Error: ${e.localizedMessage}. ¿Has activado Firestore en la Consola de Firebase?"
                )
            }
        }
    }
}
