package com.example.anibey_codex_tfg.ui.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.data.local.datastore.SessionDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    fun setGuestMode(onSuccess: () -> Unit) {
        viewModelScope.launch {
            sessionDataStore.setGuestMode()
            onSuccess()
        }
    }
}