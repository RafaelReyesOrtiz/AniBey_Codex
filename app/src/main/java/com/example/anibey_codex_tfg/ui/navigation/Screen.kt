package com.example.anibey_codex_tfg.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Welcome : Screen

    @Serializable
    data object Login : Screen
}