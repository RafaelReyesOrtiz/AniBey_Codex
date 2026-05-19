package com.example.anibey_codex_tfg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.data.local.datastore.SessionDataStore
import com.example.anibey_codex_tfg.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _startDestination = MutableStateFlow<Screen?>(null)
    val startDestination: StateFlow<Screen?> = _startDestination

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val userProfile = sessionDataStore.userData.first()
            val isGuest = sessionDataStore.isGuest.first()

            if (userProfile != null) {
                _startDestination.value = Screen.Home
            } else if (isGuest) {
                _startDestination.value = Screen.Home
            } else {
                _startDestination.value = Screen.Welcome
            }
        }
    }
}