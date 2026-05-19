package com.example.anibey_codex_tfg.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.anibey_codex_tfg.ui.login.ui.LoginScreen
import com.example.anibey_codex_tfg.ui.screens.home.HomeScreen
import com.example.anibey_codex_tfg.ui.screens.profile.ProfileScreen
import com.example.anibey_codex_tfg.ui.screens.register.RegisterScreen
import com.example.anibey_codex_tfg.ui.screens.lugares.LugaresScreen
import com.example.anibey_codex_tfg.ui.screens.lugares.lugares_detail.LugarDetailScreen
import com.example.anibey_codex_tfg.ui.screens.bestiario.BestiarioScreen
import com.example.anibey_codex_tfg.ui.screens.bestiario.monstruo_detail.MonstruoDetailScreen
import com.example.anibey_codex_tfg.ui.screens.grimorio.SpellDetailScreen
import com.example.anibey_codex_tfg.ui.screens.grimorio.SpellListScreen
import com.example.anibey_codex_tfg.ui.welcome.ui.WelcomeScreen

// RESUMED significa que la pantalla está visible y activa.
// Si el estado cambia porque ya se está navegando, el "if" bloquea pulsaciones extras.
private fun NavController.navigateSafe(
    route: Screen,
    navOptionsBuilder: NavOptionsBuilder.() -> Unit = {}
) {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        navigate(route, navOptionsBuilder)
    }
}

// Extensión para retroceder de forma segura evitando que salga la pantalla en blanco.
private fun NavController.popBackStackSafe() {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    }
}


@Composable
fun AnimaNavHost(
    modifier: Modifier,
    startDestination: Screen = Screen.Welcome
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Welcome> {
            WelcomeScreen(
                onLoginSelected = {
                    navController.navigateSafe(Screen.Login) {
                        launchSingleTop = true
                    }
                },
                onGuestSelected = {
                    navController.navigateSafe(Screen.Home) {
                        launchSingleTop = true
                    }
                },
                onRegisterSelected = {
                    navController.navigateSafe(Screen.Register) {
                        launchSingleTop = true
                    }
                },
                modifier = modifier
            )
        }

        composable<Screen.Login> {
            LoginScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStackSafe() },
                onLoginSuccess = {
                    navController.navigateSafe(Screen.Home) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = modifier
            )
        }

        composable<Screen.Register> {
            RegisterScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStackSafe() },
                onRegisterSuccess = {
                    navController.navigateSafe(Screen.Home) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = modifier
            )
        }

        composable<Screen.Home> {
            HomeScreen(
                viewModel = hiltViewModel(),
                onNavigateToProfile = {
                    navController.navigateSafe(Screen.Profile) {
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigateSafe(Screen.Login) {
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigateSafe(Screen.Register) {
                        launchSingleTop = true
                    }
                },
                onNavigateToLugares = {
                    navController.navigateSafe(Screen.Lugares) {
                        launchSingleTop = true
                    }
                },
                onNavigateToBestiario = {
                    navController.navigateSafe(Screen.Bestiario) {
                        launchSingleTop = true
                    }
                },
                onNavigateToGrimorio = {
                    navController.navigateSafe(Screen.Grimorio) {
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    navController.navigateSafe(Screen.Welcome) {
                        popUpTo(Screen.Home) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Screen.Profile> {
            ProfileScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStackSafe() },
                onLogout = {
                    navController.navigateSafe(Screen.Welcome) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Screen.Lugares> {
            LugaresScreen(
                viewModel = hiltViewModel(),
                onBackClick = { navController.popBackStackSafe() },
                onLugarClick = { id ->
                    navController.navigateSafe(Screen.LugarDetail(id)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Screen.Bestiario> {
            BestiarioScreen(
                viewModel = hiltViewModel(),
                onBackClick = { navController.popBackStackSafe() },
                onMonstruoClick = { id ->
                    navController.navigateSafe(Screen.MonstruoDetail(id)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Screen.Grimorio> {
            SpellListScreen(
                onNavigateToDetail = { id ->
                    navController.navigateSafe(Screen.HechizoDetail(id)) {
                        launchSingleTop = true
                    }
                },
                viewModel = hiltViewModel(),
                onBackClick = { navController.popBackStackSafe() }
            )
        }

        composable<Screen.LugarDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<Screen.LugarDetail>()
            LugarDetailScreen(
                lugarId = detail.lugarId,
                viewModel = hiltViewModel(),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.MonstruoDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<Screen.MonstruoDetail>()
            MonstruoDetailScreen(
                monstruoId = detail.monstruoId,
                viewModel = hiltViewModel(),
                onBackClick = { navController.popBackStackSafe() }
            )
        }

        composable<Screen.HechizoDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<Screen.HechizoDetail>()
            SpellDetailScreen(
                spellId = detail.spellId,
                onBackClick = { navController.popBackStackSafe() }
            )
        }
    }
}