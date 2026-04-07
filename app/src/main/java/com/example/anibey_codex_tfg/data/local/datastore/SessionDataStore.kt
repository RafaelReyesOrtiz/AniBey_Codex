package com.example.anibey_codex_tfg.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.anibey_codex_tfg.domain.model.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "user_session")

class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
    }
    val userData: Flow<UserProfile?> = context.dataStore.data.map { prefs ->
        UserProfile(
            email = prefs[USER_EMAIL] ?: "",
            username = prefs[USER_NAME] ?: "Viajero",
        )
    }
    suspend fun saveSession(user: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[USER_EMAIL] = user.email
            prefs[USER_NAME] = user.username
        }
    }
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}