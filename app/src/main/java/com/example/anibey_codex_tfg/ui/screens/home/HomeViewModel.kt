package com.example.anibey_codex_tfg.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.anibey_codex_tfg.data.local.datastore.SessionDataStore
import com.example.anibey_codex_tfg.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    val userProfile: Flow<UserProfile?> = sessionDataStore.userData
    val isGuest: Flow<Boolean> = sessionDataStore.isGuest
}
