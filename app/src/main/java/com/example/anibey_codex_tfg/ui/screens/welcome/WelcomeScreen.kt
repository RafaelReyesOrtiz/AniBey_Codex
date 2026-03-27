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
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anibey_codex_tfg.R
import com.example.anibey_codex_tfg.ui.common.theme.*

data class WelcomeActions(
    val onLoginClick: () -> Unit = {},
    val onGuestClick: () -> Unit = {},
    val onRegisterClick: () -> Unit = {}
)

// Composable principal con Hoisting
@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onLoginSelected: () -> Unit,
    onGuestSelected: () -> Unit,
    onRegisterSelected: () -> Unit
) {
    val actions = WelcomeActions(
        onLoginClick = onLoginSelected,
        onGuestClick = onGuestSelected,
        onRegisterClick = onRegisterSelected
    )
    WelcomeScreenContent(modifier = modifier, actions = actions)
}

// Composable de contenido (Pura UI)
@Composable
fun WelcomeScreenContent(
    modifier: Modifier = Modifier,
    actions: WelcomeActions
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.fondo_login),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.1f), BlendMode.Darken)
            )
    ) {
        // --- CAPA 1: EL LOGO ---
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

        // --- CAPA 2: CONTENIDO INTERACTIVO ---
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
                // Usamos una Box para meter el "Efecto Quemado" detrás del texto
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // CAPA DE FONDO: Una mancha oscura muy suave para que el rojo resalte
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
                        style = MaterialTheme.typography.displayLarge.copy( // Usamos displayLarge que es más imponente
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 8.sp,
                            shadow = Shadow(
                                color = Color.White.copy(alpha = 0.8f), // ¡Sombra BLANCA para separar del fondo!
                                offset = Offset(0f, 0f),
                                blurRadius = 15f
                            )
                        ),
                        color = PrimaryRed,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // BOTÓN INICIAR SESIÓN
                Button(
                    onClick = { actions.onLoginClick() },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
                ) {
                    Text("INICIAR SESIÓN", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp))
                }

                // BOTÓN INVITADO
                OutlinedButton(
                    onClick = { actions.onGuestClick() },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(1.5.dp, PrimaryRed),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.4f)
                    )
                ) {
                    Text("MODO INVITADO", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp), color = PrimaryRed)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // REGISTRO
                Text(
                    text = buildAnnotatedString {
                        append("¿Eres nuevo en Gaia? ")
                        withStyle(style = SpanStyle(
                            color = PrimaryRed,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        )) {
                            append("Regístrate")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    modifier = Modifier.clickable { actions.onRegisterClick() }
                )
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