package com.example.anibey_codex_tfg.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Welcome : Screen

    @Serializable data object Login: Screen
    @Serializable data object Register: Screen
    @Serializable
    data object Home : Screen
    @Serializable
    data object Profile : Screen
}