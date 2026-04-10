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
import com.example.anibey_codex_tfg.ui.screens.home.HomeScreen
import com.example.anibey_codex_tfg.ui.screens.profile.ProfileScreen
import com.example.anibey_codex_tfg.ui.screens.register.RegisterScreen
import com.example.anibey_codex_tfg.ui.welcome.ui.WelcomeScreen

@Composable
fun AnimaNavHost(
    modifier : Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome,
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
        composable<Screen.Welcome> {
            WelcomeScreen(
                onLoginSelected = { navController.navigate(Screen.Login) },
                onGuestSelected = { 
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Welcome> { inclusive = true }
                    }
                },
                onRegisterSelected = { navController.navigate(Screen.Register) },
                modifier = modifier
            )
        }

        composable<Screen.Login> {
            LoginScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Welcome> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = modifier
            )
        }

        composable<Screen.Register> {
            RegisterScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Welcome> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = modifier
            )
        }

        composable<Screen.Home> {
            HomeScreen(
                viewModel = hiltViewModel(),
                onNavigateToProfile = { navController.navigate(Screen.Profile) },
                onNavigateToLogin = { navController.navigate(Screen.Login) },
                onNavigateToRegister = { navController.navigate(Screen.Register) },
                onLogout = {
                    navController.navigate(Screen.Welcome) {
                        popUpTo<Screen.Home> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Screen.Profile> {
            ProfileScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}