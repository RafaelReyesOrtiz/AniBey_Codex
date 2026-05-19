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
        if (user == null) {
            state = state.copy(shouldNavigateToWelcome = true, isLoading = false)
            return
        }

        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                user.reload().await()
                val currentAuthEmail = user.email ?: ""

                val doc = db.collection("users").document(user.uid).get().await()
                val profile = doc.toObject(UserProfile::class.java)

                if (profile != null) {
                    if (currentAuthEmail.isNotEmpty() && !currentAuthEmail.equals(profile.email, ignoreCase = true)) {
                        syncFirestoreEmail(user.uid, currentAuthEmail)
                    }

                    originalUsername = profile.username
                    originalEmail = currentAuthEmail
                    originalPhotoUrl = profile.photoUrl

                    state = state.copy(
                        username = originalUsername,
                        email = originalEmail,
                        photoUrl = originalPhotoUrl,
                        isLoading = false,
                        hasUnsavedChanges = false
                    )
                } else {
                    state = state.copy(isLoading = false, generalError = "Datos no encontrados")
                }
            } catch (_: Exception) {
                state = state.copy(isLoading = false, generalError = "Error de conexión")
            }
        }
    }

    private fun checkChanges() {
        val emailChanged = !state.email.trim().equals(originalEmail, ignoreCase = true)
        val passwordChanged = state.password.isNotBlank()
        val usernameChanged = state.username != originalUsername
        val photoChanged = state.photoUrl != originalPhotoUrl
        
        state = state.copy(hasUnsavedChanges = emailChanged || passwordChanged || usernameChanged || photoChanged)
    }

    fun onUsernameChange(newValue: String) {
        state = state.copy(username = newValue, usernameError = null)
        checkChanges()
    }

    fun onEmailChange(newValue: String) {
        state = state.copy(email = newValue, emailError = null)
        checkChanges()
    }

    fun onPasswordChange(newValue: String) {
        state = state.copy(password = newValue, passwordError = null)
        checkChanges()
    }

    fun onCurrentPasswordChange(newValue: String) {
        state = state.copy(currentPassword = newValue, currentPasswordError = null)
        checkChanges()
    }

    fun onPhotoChange(newValue: String?) {
        state = state.copy(photoUrl = newValue)
        checkChanges()
    }
    
    fun onDismissError(){
        state = state.copy(generalError = null)
    }

    fun onDismissSuccess() {
        state = state.copy(updateSuccess = false)
    }

    fun uploadPhoto(imageUri: Uri) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                val base64Image = processImageToBase64(imageUri)
                state = state.copy(photoUrl = base64Image, isLoading = false)
                checkChanges()
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
        if (!state.hasUnsavedChanges) return

        val authEmail = auth.currentUser?.email ?: return
        val targetEmail = state.email.trim()
        val emailChanged = !targetEmail.equals(authEmail, ignoreCase = true)
        val passwordChanged = state.password.isNotBlank()

        if ((emailChanged || passwordChanged) && state.currentPassword.isBlank()) {
            state = state.copy(currentPasswordError = "Contraseña actual requerida")
            return
        }
        
        state = state.copy(isLoading = true, generalError = null, updateSuccess = false)

        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
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
                handleSaveError(e)
            }
        }
    }

    private suspend fun reauthenticateUser(user: FirebaseUser, authEmail: String): Boolean {
        return try {
            val credential = EmailAuthProvider.getCredential(authEmail, state.currentPassword)
            user.reauthenticate(credential).await()
            true
        } catch (e: Exception) {
            handleSaveError(e)
            false
        }
    }

    private suspend fun processEmailChange(user: FirebaseUser, targetEmail: String, onAutoLogout: () -> Unit) {
        try {
            if (!Patterns.EMAIL_ADDRESS.matcher(targetEmail).matches()) {
                state = state.copy(isLoading = false, generalError = "Email inválido")
                return
            }
            user.verifyBeforeUpdateEmail(targetEmail).await()
            state = state.copy(isLoading = false, isCheckingEmailVerification = true, generalError = null)
            startEmailVerificationPolling(user.uid, targetEmail, onAutoLogout)
        } catch (e: Exception) {
            handleSaveError(e)
        }
    }

    private suspend fun processPasswordChange(user: FirebaseUser) : Boolean {
        return try {
            if (state.password.length < 6) {
                state = state.copy(isLoading = false, generalError = "Mínimo 6 caracteres")
                false
            } else {
                user.updatePassword(state.password).await()
                true
            }
        } catch (e: Exception) {
            handleSaveError(e)
            false
        }
    }

    private fun handleSaveError(e: Exception) {
        val msg = when {
            e is FirebaseAuthUserCollisionException -> "El email ya existe"
            e.message?.contains("credential") == true || e.message?.contains("password") == true -> "Contraseña incorrecta"
            else -> "Error al actualizar perfil"
        }
        state = state.copy(isLoading = false, generalError = msg)
    }

    private fun startEmailVerificationPolling(uid: String, targetEmail: String, onVerified: () -> Unit) {
        verificationJob?.cancel()
        verificationJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val timeout = 30000L
            
            while (isActive) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    state = state.copy(
                        isCheckingEmailVerification = false,
                        generalError = "Tiempo agotado esperando la verificación del correo."
                    )
                    break
                }
                
                delay(3000)
                val user = auth.currentUser ?: break
                if (performPollingTick(user, uid, targetEmail, onVerified)) break
            }
        }
    }

    private suspend fun performPollingTick(user: FirebaseUser, uid: String, targetEmail: String, onVerified: () -> Unit): Boolean {
        return try {
            user.reload().await()
            if (user.email?.equals(targetEmail, ignoreCase = true) == true) {
                completeVerification(uid, targetEmail, onVerified)
                true
            } else false
        } catch (e: Exception) {
            handlePollingError(e, onVerified)
        }
    }

    private suspend fun completeVerification(uid: String, email: String, onVerified: () -> Unit) {
        withContext(NonCancellable) {
            persistAllChanges(uid, email)
            auth.signOut()
            sessionDataStore.clearSession()
        }
        state = state.copy(shouldNavigateToWelcome = true, isCheckingEmailVerification = false)
        onVerified()
    }

    private suspend fun handlePollingError(e: Exception, onVerified: () -> Unit): Boolean {
        if (e is FirebaseAuthInvalidUserException) {
            withContext(NonCancellable) {
                auth.signOut()
                sessionDataStore.clearSession()
            }
            state = state.copy(shouldNavigateToWelcome = true, isCheckingEmailVerification = false)
            onVerified()
            return true
        }
        return false
    }

    private fun isUsernameValid(): Boolean {
        return if (state.username.isBlank()) {
            state = state.copy(usernameError = "Campo obligatorio", generalError = "El nombre es obligatorio")
            false
        } else true
    }

    private suspend fun persistAllChanges(uid: String, emailToSave: String) {
        val docRef = db.collection("users").document(uid)
        try {
            val snapshot = docRef.get().await()
            val currentProfile = snapshot.toObject(UserProfile::class.java)
            val updatedProfile = currentProfile?.copy(
                email = emailToSave,
                username = state.username,
                photoUrl = state.photoUrl
            ) ?: UserProfile(email = emailToSave, username = state.username, photoUrl = state.photoUrl)
            docRef.set(updatedProfile).await()
            sessionDataStore.saveSession(updatedProfile)
        } catch (e: Exception) {
            Log.e("ProfileVM", "Error en Firestore", e)
        }
    }

    private fun finalizeSaveProcess() {
        originalUsername = state.username
        originalEmail = state.email
        originalPhotoUrl = state.photoUrl
        state = state.copy(
            isLoading = false, 
            updateSuccess = true, 
            currentPassword = "", 
            password = "",
            hasUnsavedChanges = false
        )
    }

    private suspend fun syncFirestoreEmail(uid: String, newEmail: String) {
        withContext(NonCancellable) {
            val docRef = db.collection("users").document(uid)
            val doc = docRef.get().await()
            val profile = doc.toObject(UserProfile::class.java)
            profile?.let {
                val updated = it.copy(email = newEmail)
                docRef.set(updated).await()
                sessionDataStore.saveSession(updated)
            }
        }
    }

    fun onBackRequested(onConfirmBack: () -> Unit) {
        if (state.hasUnsavedChanges) {
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
