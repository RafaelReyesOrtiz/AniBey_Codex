package com.example.anibey_codex_tfg.ui.navigation

import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.anibey_codex_tfg.ui.login.ui.LoginScreen
import com.example.anibey_codex_tfg.ui.screens.login.LoginViewModel
import com.example.anibey_codex_tfg.ui.welcome.ui.WelcomeScreen

@Composable
fun AnimaNavHost(modifier : Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome,
        // CONFIGURACIÓN DE ANIMACIONES GLOBALES
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(600, easing = EaseInOutQuart)
            ) + fadeIn(animationSpec = tween(600))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(600, easing = EaseInOutQuart)
            ) + fadeOut(animationSpec = tween(600))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(600, easing = EaseInOutQuart)
            ) + fadeIn(animationSpec = tween(600))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(600, easing = EaseInOutQuart)
            ) + fadeOut(animationSpec = tween(600))
        }
    ) {
        // RUTA: WELCOME
        composable<Screen.Welcome> {
            WelcomeScreen(
                onLoginSelected = { navController.navigate(Screen.Login) },
                onGuestSelected = { /* Próximamente: Screen.Home */ },
                onRegisterSelected = { navController.navigate(Screen.Login) },
                modifier = modifier
            )
        }

        // RUTA: LOGIN (CON VIEWMODEL INYECTADO)
        composable<Screen.Login> {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    // Aquí navegarías a la pantalla principal tras el "Pacto"
                },
                modifier = modifier
            )
        }
    }
}