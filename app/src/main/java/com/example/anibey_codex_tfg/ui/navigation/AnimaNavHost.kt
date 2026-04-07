package com.example.anibey_codex_tfg.ui.navigation

import android.app.Activity
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.anibey_codex_tfg.ui.login.ui.LoginScreen
import com.example.anibey_codex_tfg.ui.screens.home.HomeScreen
import com.example.anibey_codex_tfg.ui.screens.login.LoginViewModel
import com.example.anibey_codex_tfg.ui.welcome.ui.WelcomeScreen

@Composable
fun AnimaNavHost(
    modifier : Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val activity = context as? Activity
        val intent = activity?.intent
        val emailLink = intent?.data?.toString()

        if (emailLink != null && viewModel.auth.isSignInWithEmailLink(emailLink)) {
            val currentlyRegistering = viewModel.state.isRegistering

            viewModel.completeSpiritLink(emailLink, isRegister = currentlyRegistering) {
                if (currentlyRegistering) {
                    navController.navigate(Screen.Welcome) { popUpTo(0) }
                } else {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Welcome) { inclusive = true }
                    }
                }
            }
        }
    }

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
                onLoginSelected = { navController.navigate(Screen.Login(isRegister = false)) },
                onGuestSelected = { /* Próximamente: Screen.Home */ },
                onRegisterSelected = { navController.navigate(Screen.Login(isRegister = true)) },
                modifier = modifier
            )
        }

        // RUTA: LOGIN
        composable<Screen.Login> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.Login>()
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                isRegister = args.isRegister,
                viewModel = loginViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    // Aquí navegarías a la pantalla principal tras el "Pacto"
                },
                modifier = modifier
            )
        }
        composable<Screen.Home> {
            HomeScreen()
        }
    }
}