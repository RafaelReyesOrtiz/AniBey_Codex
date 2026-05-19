package com.example.anibey_codex_tfg.ui.welcome.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.anibey_codex_tfg.R
import com.example.anibey_codex_tfg.ui.common.theme.*
import com.example.anibey_codex_tfg.ui.screens.welcome.WelcomeViewModel

data class WelcomeActions(
    val onLoginClick: () -> Unit = {},
    val onGuestClick: () -> Unit = {},
    val onRegisterClick: () -> Unit = {}
)

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    viewModel: WelcomeViewModel = hiltViewModel(),
    onLoginSelected: () -> Unit,
    onGuestSelected: () -> Unit,
    onRegisterSelected: () -> Unit
) {
    val actions = WelcomeActions(
        onLoginClick = onLoginSelected,
        onGuestClick = {
            viewModel.setGuestMode(onSuccess = onGuestSelected)
        },
        onRegisterClick = onRegisterSelected
    )
    WelcomeScreenContent(modifier = modifier, actions = actions)
}

@Composable
fun WelcomeScreenContent(
    modifier: Modifier = Modifier,
    actions: WelcomeActions
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.fondo_login),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.1f), BlendMode.Darken)
            ),
        containerColor = Color.Transparent,
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Eres nuevo en Gaia? ",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    )
                    Text(
                        text = "Regístrate",
                        color = PrimaryRed,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { actions.onRegisterClick() }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_anima_tfg),
                contentDescription = "Anima Codex Logo",
                modifier = Modifier
                    .align(Alignment.TopCenter) // Se mantiene centrado inicialmente
                    .padding(top = 100.dp)
                    .fillMaxWidth()
                    .scale(1.7f)
                    // Aplicamos un desplazamiento negativo en X para mover a la izquierda.
                    // Prueba valores como -40.dp, -60.dp, etc. hasta que te guste.
                    .offset(x = (-15).dp),
                contentScale = ContentScale.FillWidth
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom // Alineamos al fondo
            ) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 50.dp) // Espacio de seguridad inferior
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Spacer(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Text(
                            text = "EL CÓDICE DE GAIA",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 8.sp,
                                shadow = Shadow(
                                    color = Color.White.copy(alpha = 0.8f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 15f
                                )
                            ),
                            color = PrimaryRed,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { actions.onLoginClick() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
                    ) {
                        Text(
                            "INICIAR SESIÓN",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
                        )
                    }

                    OutlinedButton(
                        onClick = { actions.onGuestClick() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(2.dp),
                        border = BorderStroke(1.5.dp, PrimaryRed),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            "MODO INVITADO",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            color = PrimaryRed
                        )
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun WelcomePreview() {
    AniBey_Codex_TFGTheme {
        WelcomeScreenContent(actions = WelcomeActions())
    }
}