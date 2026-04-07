package com.example.anibey_codex_tfg.ui.login.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anibey_codex_tfg.R
import com.example.anibey_codex_tfg.ui.common.component.AnimaTextField
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed
import com.example.anibey_codex_tfg.ui.screens.login.LoginActions
import com.example.anibey_codex_tfg.ui.screens.login.LoginState
import com.example.anibey_codex_tfg.ui.screens.login.LoginViewModel
import com.example.anibey_codex_tfg.ui.screens.login.RegistrationStep

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit, // Se ejecutará tras el link
    isRegister: Boolean,
    modifier: Modifier = Modifier
) {
    val state = viewModel.state

    val actions = LoginActions(
        onEmailChange = viewModel::onEmailChange,
        onUsernameChange = viewModel::onUsernameChange,
        onNextStep = {
            val isFinalStep = if (isRegister) state.currentStep == RegistrationStep.FINALIZING
            else state.currentStep == RegistrationStep.CREDENTIALS
            if (isFinalStep) viewModel.onLoginSubmit() else viewModel.onNextStep()
        },
        onBackStep = {
            if (state.isCodeSent) viewModel.resetSpiritWhisper()
            else if (state.currentStep.ordinal == 0) onNavigateBack()
            else viewModel.onBackStep()
        },
        onLoginSubmit = { viewModel.onLoginSubmit() },
        onGoogleClick = { /* Google Logic */ },
        onResetSpiritWhisper = { viewModel.resetSpiritWhisper() }
    )

    Box(modifier = modifier.fillMaxSize()) {
        LoginScreenContent(
            isRegister = isRegister,
            state = state,
            actions = actions,
            modifier = modifier
        )
    }
}

@Composable
fun LoginScreenContent(
    isRegister: Boolean,
    state: LoginState,
    actions: LoginActions,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.fondo_login),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.15f), BlendMode.Darken)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginHeader(step = state.currentStep, isRegister = isRegister)

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                if (state.isCodeSent) {
                    WhisperWaitView(email = state.email)
                } else {
                    LoginStepFields(state = state, actions = actions, isRegister = isRegister)
                    if (state.currentStep == RegistrationStep.CREDENTIALS) {
                        GoogleSignInSection(onGoogleClick = actions.onGoogleClick)
                    }
                }
            }

            // Botones de Navegación (Solo se ven si no se ha enviado el mail)
            if (!state.isCodeSent) {
                LoginNavigationButtons(
                    currentStep = state.currentStep,
                    isRegister = isRegister,
                    actions = actions,
                    state = state
                )
            } else {
                // Botón para volver si el mail no llega
                TextButton(
                    onClick = actions.onBackStep,
                    modifier = Modifier.padding(bottom = 60.dp)
                ) {
                    Text("EL VACÍO NO RESPONDE (REINTENTAR)", color = PrimaryRed)
                }
            }
        }
    }
}

@Composable
private fun GoogleSignInSection(onGoogleClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "— O VINCÚLATE MEDIANTE —",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 2.sp,
                color = Color.Black.copy(alpha = 0.3f)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Un botón más estilizado, menos "pesado"
        OutlinedButton(
            onClick = onGoogleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(4.dp), // Casi cuadrado, muy Anima
            border = BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.2f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EL PACTO DEL SOL",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp)
                )
            }
        }
    }
}

@Composable
private fun LoginHeader(step: RegistrationStep, isRegister: Boolean) {
    val title = when (step) {
        RegistrationStep.CREDENTIALS -> if (isRegister) "VINCULACIÓN" else "ESENCIA"
        RegistrationStep.CHARACTER_INFO -> "IDENTIDAD"
        RegistrationStep.FINALIZING -> "EL PACTO"
    }

    Text(
        text = title,
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 6.sp,
            shadow = Shadow(color = Color.White.copy(alpha = 0.7f), blurRadius = 12f)
        ),
        color = Color(0xFF1A1A1A),
        modifier = Modifier.padding(top = 80.dp)
    )
}

@Composable
private fun LoginStepFields(
    state: LoginState,
    actions: LoginActions,
    isRegister: Boolean
) {
    if (state.isCodeSent) {
        WhisperWaitView(email = state.email)
    } else {
        when (state.currentStep) {
            RegistrationStep.CREDENTIALS -> {
                AnimaTextField(
                    value = state.email,
                    onValueChange = actions.onEmailChange,
                    label = "E-mail de contacto",
                    errorMessage = state.emailError
                )
            }

            RegistrationStep.CHARACTER_INFO -> {
                // Solo debería verse en Registro
                AnimaTextField(
                    value = state.username,
                    onValueChange = actions.onUsernameChange,
                    label = "Apodo del Viajero",
                    errorMessage = state.usernameError
                )
            }

            RegistrationStep.FINALIZING -> {
                // Mensaje final personalizado
                val title = if (isRegister) "SELLAR EL PACTO" else "RECUPERAR ESENCIA"
                val subtitle = if (isRegister)
                    "Tu alma será vinculada al Códice de Gaia."
                else "Tus recuerdos volverán a fluir desde el vacío."

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$subtitle\n¿Deseas lanzar el susurro?",
                        style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WhisperWaitView(email: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "EL SUSURRO HA SIDO ENVIADO",
            style = MaterialTheme.typography.titleMedium.copy(
                color = PrimaryRed,
                letterSpacing = 2.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Un lazo místico espera en tu bandeja de entrada ($email).\nPulsa el vínculo para despertar tu esencia.",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            ),
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun LoginNavigationButtons(
    currentStep: RegistrationStep,
    isRegister: Boolean,
    state: LoginState,
    actions: LoginActions
) {
    // Determinamos el estado del flujo
    val isFirstStep = currentStep == RegistrationStep.CREDENTIALS
    val isLastStep = if (isRegister) {
        currentStep == RegistrationStep.FINALIZING
    } else {
        isFirstStep // En Login, el primer paso es el último
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Botón de retroceso (Desistir o Volver)
        SecondaryActionButton(
            isFirstStep = isFirstStep,
            isLoading = state.isLoading,
            onClick = actions.onBackStep
        )

        // 2. Botón de acción principal (Sellar, Despertar o Seguir)
        PrimaryActionButton(
            isLastStep = isLastStep,
            isRegister = isRegister,
            isLoading = state.isLoading,
            onClick = if (isLastStep) actions.onLoginSubmit else actions.onNextStep
        )
    }
}

@Composable
private fun SecondaryActionButton(
    isFirstStep: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = !isLoading
    ) {
        Text(
            text = if (isFirstStep) "DESISTIR" else "VOLVER",
            color = if (isLoading) Color.Gray else Color.Black.copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = if (isFirstStep) 2.sp else 0.sp
            )
        )
    }
}

@Composable
private fun PrimaryActionButton(
    isLastStep: Boolean,
    isRegister: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(50.dp)
            .width(160.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryRed,
            disabledContainerColor = PrimaryRed.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            val buttonText = when {
                isRegister && isLastStep -> "SELLAR"
                !isRegister -> "DESPERTAR"
                else -> "SEGUIR"
            }
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenContentPreview() {
    LoginScreenContent(
        isRegister = true,
        state = LoginState(),
        actions = LoginActions(),
        Modifier
    )
}