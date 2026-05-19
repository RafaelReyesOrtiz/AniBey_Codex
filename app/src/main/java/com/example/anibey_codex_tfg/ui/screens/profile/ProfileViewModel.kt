package com.example.anibey_codex_tfg.ui.screens.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.data.local.datastore.SessionDataStore
import com.example.anibey_codex_tfg.domain.model.UserProfile
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import androidx.core.graphics.scale

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    var state by mutableStateOf(ProfileState())
        private set

    private var originalUsername = ""
    private var originalEmail = ""
    private var originalPhotoUrl: String? = null
    private var verificationJob: Job? = null

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        Log.d("ProfileVM", "Cargando perfil. Usuario actual: ${user?.uid}")
        if (user == null) {
            state = state.copy(shouldNavigateToWelcome = true, isLoading = false)
            return
        }

        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                user.reload().await()
                val currentAuthEmail = user.email ?: ""
                Log.d("ProfileVM", "Email en Authentication: $currentAuthEmail")

                val doc = db.collection("users").document(user.uid).get().await()
                val profile = doc.toObject(UserProfile::class.java)
                Log.d("ProfileVM", "Email en Firestore: ${profile?.email}")

                if (profile != null) {
                    if (currentAuthEmail.isNotEmpty() && !currentAuthEmail.equals(
                            profile.email,
                            ignoreCase = true
                        )
                    ) {
                        Log.d(
                            "ProfileVM",
                            "Desajuste detectado. Sincronizando Firestore con el nuevo correo verificado."
                        )
                        syncFirestoreEmail(user.uid, currentAuthEmail)
                    }

                    originalUsername = profile.username
                    originalEmail = currentAuthEmail
                    originalPhotoUrl = profile.photoUrl

                    state = state.copy(
                        username = originalUsername,
                        email = originalEmail,
                        photoUrl = originalPhotoUrl,
                        isLoading = false
                    )
                } else {
                    Log.e("ProfileVM", "No se encontró el perfil en Firestore")
                    state =
                        state.copy(isLoading = false, generalError = "Error: Datos no encontrados")
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "Error cargando perfil", e)
                state = state.copy(isLoading = false, generalError = "Error de red")
            }
        }
    }

    private suspend fun syncFirestoreEmail(uid: String, newEmail: String) {
        withContext(NonCancellable) {
            Log.d("ProfileVM", "Iniciando sincronización: $newEmail")
            val docRef = db.collection("users").document(uid)
            val doc = docRef.get().await()
            val profile = doc.toObject(UserProfile::class.java)
            profile?.let {
                val updated = it.copy(email = newEmail)
                docRef.set(updated).await()
                sessionDataStore.saveSession(updated)
                Log.d("ProfileVM", "Firestore actualizado con el nuevo correo")
            }
        }
    }

    fun onUsernameChange(newValue: String) {
        state = state.copy(username = newValue, usernameError = null)
    }

    fun onEmailChange(newValue: String) {
        state = state.copy(email = newValue, emailError = null)
    }

    fun onPasswordChange(newValue: String) {
        state = state.copy(password = newValue, passwordError = null)
    }

    fun onCurrentPasswordChange(newValue: String) {
        state = state.copy(currentPassword = newValue, currentPasswordError = null)
    }

    fun onPhotoChange(newValue: String?) {
        state = state.copy(photoUrl = newValue)
    }

    fun uploadPhoto(imageUri: Uri) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                val base64Image = processImageToBase64(imageUri)
                state = state.copy(photoUrl = base64Image, isLoading = false)
            } catch (_: Exception) {
                state = state.copy(isLoading = false, generalError = "Error procesando imagen")
            }
        }
    }

    private suspend fun processImageToBase64(uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream) ?: throw Exception()
        inputStream?.close()
        val resizedBitmap = bitmap.scale(300, 300)
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    fun saveProfile(onAutoLogout: () -> Unit) {
        if (!isUsernameValid()) return

        val authEmail = auth.currentUser?.email ?: ""
        val targetEmail = state.email.trim()
        val emailChanged = !targetEmail.equals(authEmail, ignoreCase = true)
        val passwordChanged = state.password.isNotBlank()

        if ((emailChanged || passwordChanged) && state.currentPassword.isBlank()) {
            state = state.copy(
                currentPasswordError = "Contraseña actual requerida"
            )
            return
        }
        state = state.copy(isLoading = true, generalError = null, updateSuccess = false)

        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
                Log.d(
                    "ProfileVM",
                    "Iniciando guardado. Email actual: $authEmail, Objetivo: $targetEmail"
                )

                user.reload().await()

                if (emailChanged || passwordChanged) {
                    if(!reauthenticateUser(user, authEmail)) return@launch
                }

                if (emailChanged) {
                    processEmailChange(user, targetEmail, onAutoLogout)
                    return@launch
                }

                if (passwordChanged) {
                    if (!processPasswordChange(user)) return@launch
                }

                persistAllChanges(user.uid, authEmail)
                finalizeSaveProcess()

            } catch (e: Exception) {
                Log.e("ProfileVM", "Error general en saveProfile", e)
                handleSaveError(e)
            }
        }
    }

    private suspend fun reauthenticateUser(user: FirebaseUser, authEmail: String): Boolean {
        return try {
            Log.d("ProfileVM", "Reautenticando usuario...")
            val credential = EmailAuthProvider.getCredential(authEmail, state.currentPassword)
            user.reauthenticate(credential).await()
            Log.d("ProfileVM", "Reautenticación exitosa")
            true
        } catch (e: Exception) {
            Log.e("ProfileVM", "Error en reautenticación", e)
            state = state.copy(isLoading = false)
            handleSaveError(e)
            false
        }
    }

    private suspend fun processEmailChange(
        user: FirebaseUser,
        targetEmail: String,
        onAutoLogout: () -> Unit
    ) {
        try {
            if (!Patterns.EMAIL_ADDRESS.matcher(targetEmail).matches()) {
                state = state.copy(
                    isLoading = false,
                    emailError = "Email inválido"
                )
                return
            }

            Log.d("ProfileVM", "Enviando verificación a $targetEmail")

            user.verifyBeforeUpdateEmail(targetEmail).await()

            state = state.copy(
                isLoading = false,
                isCheckingEmailVerification = true
            )

            startEmailVerificationPolling(user.uid, targetEmail, onAutoLogout)
        } catch (e: Exception) {
            Log.e("ProfileVM", "Error en cambio de email", e)
            state = state.copy(isLoading = false)
            handleSaveError(e)
        }
    }

    private suspend fun processPasswordChange(user: FirebaseUser) : Boolean {
        return try {
            if (state.password.length < 6) {
                state = state.copy(isLoading = false, passwordError = "Mínimo 6 caracteres")
                false
            } else{
                user.updatePassword(state.password).await()
                true
            }
        } catch (e: Exception) {
            Log.e("ProfileVM", "Error en cambio de contraseña", e)
            state = state.copy(isLoading = false)
            handleSaveError(e)
            false
        }
    }

    private fun handleSaveError(e: Exception) {
        Log.e("ProfileVM", "Error al guardar perfil", e)
        val msg = when {
            e is FirebaseAuthUserCollisionException -> "El email ya existe"
            e.message?.contains("credential") == true -> "Contraseña incorrecta"
            else -> "Error al guardar perfil"
        }
        state = state.copy(isLoading = false, generalError = msg)
    }

    private fun startEmailVerificationPolling(
        uid: String,
        targetEmail: String,
        onVerified: () -> Unit
    ) {
        Log.d("ProfileVM", "Esperando verificación de: $targetEmail")
        verificationJob?.cancel()
        verificationJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                val user = auth.currentUser
                if (user == null) {
                    Log.d("ProfileVM", "Usuario es null. Deteniendo polling.")
                    break
                }
                if (performPollingTick(user, uid, targetEmail, onVerified)) break
            }
        }
    }

    private suspend fun performPollingTick(
        user: FirebaseUser,
        uid: String,
        targetEmail: String,
        onVerified: () -> Unit
    ): Boolean {
        return try {
            user.reload().await()
            val currentAuthEmail = user.email ?: ""
            Log.d(
                "ProfileVM",
                "Revisando Auth... Actual: $currentAuthEmail, Objetivo: $targetEmail"
            )

            if (currentAuthEmail.equals(targetEmail, ignoreCase = true)) {
                completeVerification(uid, currentAuthEmail, onVerified)
                true
            } else false
        } catch (e: Exception) {
            handlePollingError(e, onVerified)
        }
    }

    private suspend fun completeVerification(uid: String, email: String, onVerified: () -> Unit) {
        Log.d("ProfileVM", "¡Confirmado! Sincronizando y cerrando sesión.")
        withContext(NonCancellable) {
            persistAllChanges(uid, email)
            auth.signOut()
            sessionDataStore.clearSession()
        }
        state = state.copy(shouldNavigateToWelcome = true, isCheckingEmailVerification = false)
        onVerified()
    }

    private suspend fun handlePollingError(e: Exception, onVerified: () -> Unit): Boolean {
        Log.e("ProfileVM", "Error en el polling de verificación", e)
        return if (e is FirebaseAuthInvalidUserException) {
            Log.d("ProfileVM", "Sesión invalidada tras verificación. Redirigiendo a Login.")
            withContext(NonCancellable) {
                auth.signOut()
                sessionDataStore.clearSession()
            }
            state = state.copy(shouldNavigateToWelcome = true, isCheckingEmailVerification = false)
            onVerified()
            true
        } else false
    }

    private fun isUsernameValid(): Boolean {
        return if (state.username.isBlank()) {
            state = state.copy(usernameError = "Campo obligatorio")
            false
        } else true
    }

    private suspend fun persistAllChanges(uid: String, emailToSave: String) {
        Log.d("ProfileVM", "Persistiendo cambios en Firestore para el email: $emailToSave")
        val docRef = db.collection("users").document(uid)
        try {
            val snapshot = docRef.get().await()
            val currentProfile = snapshot.toObject(UserProfile::class.java)

            val updatedProfile = currentProfile?.copy(
                email = emailToSave,
                username = state.username,
                photoUrl = state.photoUrl
            ) ?: UserProfile(
                email = emailToSave,
                username = state.username,
                photoUrl = state.photoUrl
            )

            docRef.set(updatedProfile).await()
            sessionDataStore.saveSession(updatedProfile)
            Log.d("ProfileVM", "Cambios guardados con éxito en base de datos")
        } catch (e: Exception) {
            Log.e("ProfileVM", "Error persistiendo cambios: ${e.message}")
        }
    }

    private fun finalizeSaveProcess() {
        originalUsername = state.username
        originalEmail = state.email
        originalPhotoUrl = state.photoUrl
        state =
            state.copy(isLoading = false, updateSuccess = true, currentPassword = "", password = "")
    }

    fun onBackRequested(onConfirmBack: () -> Unit) {
        if (state.username != originalUsername || state.email != originalEmail ||
            state.photoUrl != originalPhotoUrl || state.password.isNotEmpty()
        ) {
            state = state.copy(isDiscardDialogOpen = true)
        } else onConfirmBack()
    }

    fun onDismissDiscardDialog() {
        state = state.copy(isDiscardDialogOpen = false)
    }

    override fun onCleared() {
        super.onCleared()
        verificationJob?.cancel()
    }
}
