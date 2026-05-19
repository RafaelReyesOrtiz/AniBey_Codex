package com.example.anibey_codex_tfg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.anibey_codex_tfg.ui.common.component.LoadingScreen
import com.example.anibey_codex_tfg.ui.common.theme.AniBey_Codex_TFGTheme
import com.example.anibey_codex_tfg.ui.navigation.AnimaNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val startDestination by viewModel.startDestination.collectAsState()

            AniBey_Codex_TFGTheme {
                if (startDestination == null) {
                    LoadingScreen(message = "Invocando el Codex...")
                } else {
                    AnimaNavHost(
                        modifier = Modifier.padding(),
                        startDestination = startDestination!!
                    )
                }
            }
        }
    }
}
