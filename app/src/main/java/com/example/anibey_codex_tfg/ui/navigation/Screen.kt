package com.example.anibey_codex_tfg.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Welcome : Screen

    @Serializable data class Login(val isRegister: Boolean) : Screen
    @Serializable
    data object Home : Screen
}