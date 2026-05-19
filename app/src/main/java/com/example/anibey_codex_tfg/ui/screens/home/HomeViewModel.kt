package com.example.anibey_codex_tfg.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.data.local.datastore.SessionDataStore
import com.example.anibey_codex_tfg.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userProfile: UserProfile? = null,
    val isGuest: Boolean = false,
    val toastMessage: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            launch {
                sessionDataStore.isGuest.collect { isGuest ->
                    _uiState.update { it.copy(isGuest = isGuest) }
                    if (isGuest) {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }

            launch {
                val user = auth.currentUser
                if (user != null) {
                    val docRef = db.collection("users").document(user.uid)
                    docRef.addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            _uiState.update { it.copy(isLoading = false) }
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            val profile = snapshot.toObject(UserProfile::class.java)
                            _uiState.update { it.copy(userProfile = profile, isLoading = false) }
                            profile?.let {
                                viewModelScope.launch { sessionDataStore.saveSession(it) }
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                } else {
                    sessionDataStore.userData.collectLatest { profile ->
                        _uiState.update { it.copy(userProfile = profile) }
                        if (profile != null) {
                            _uiState.update { it.copy(isLoading = false) }
                        } else {
                            delay(500)
                            if (_uiState.value.userProfile == null && !_uiState.value.isGuest) {
                                _uiState.update { it.copy(isLoading = false) }
                            }
                        }
                    }
                }
            }
        }
    }

    fun onBlockedFeatureClick(message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(toastMessage = message) }
            delay(3000)
            if (_uiState.value.toastMessage == message) {
                _uiState.update { it.copy(toastMessage = null) }
            }
        }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            auth.signOut()
            sessionDataStore.clearSession()
            onSuccess()
        }
    }
}
