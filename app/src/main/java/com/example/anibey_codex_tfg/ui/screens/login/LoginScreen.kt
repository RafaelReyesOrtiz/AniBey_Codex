package com.example.anibey_codex_tfg.ui.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
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
import com.example.anibey_codex_tfg.ui.common.component.RegistrationProgressBar
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed
import com.example.anibey_codex_tfg.ui.screens.login.LoginActions
import com.example.anibey_codex_tfg.ui.screens.login.LoginState
import com.example.anibey_codex_tfg.ui.screens.login.LoginViewModel
import com.example.anibey_codex_tfg.ui.screens.login.RegistrationStep

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = viewModel.state

    val actions = LoginActions(
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onUsernameChange = viewModel::onUsernameChange,
        onNextStep = viewModel::onNextStep,
        onBackStep = {
            if (state.currentStep.ordinal == 0) {
                onNavigateBack()
            } else {
                viewModel.onBackStep()
            }
        },
        onLoginSubmit = {
            viewModel.onLoginSubmit()
            onLoginSuccess()
        }
    )

    LoginScreenContent(
        state = state,
        actions = actions,
        modifier = modifier
    )
}

@Composable
fun LoginScreenContent(
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
            LoginHeader(step = state.currentStep)

            RegistrationProgressBar(currentStep = state.currentStep)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                LoginStepFields(state = state, actions = actions)
            }

            LoginNavigationButtons(
                currentStep = state.currentStep,
                actions = actions
            )
        }
    }
}

@Composable
private fun LoginHeader(step: RegistrationStep) {
    val title = when (step) {
        RegistrationStep.CREDENTIALS -> "VINCULACIÓN"
        RegistrationStep.CHARACTER_INFO -> "IDENTIDAD"
        RegistrationStep.FINALIZING -> "EL PACTO"
    }

    Text(
        text = title,
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 6.sp,
            shadow = Shadow(
                color = Color.White.copy(alpha = 0.7f),
                offset = Offset(0f, 0f),
                blurRadius = 12f
            )
        ),
        color = Color(0xFF1A1A1A),
        modifier = Modifier.padding(top = 80.dp)
    )
}

@Composable
private fun LoginStepFields(state: LoginState, actions: LoginActions) {
    when (state.currentStep) {
        RegistrationStep.CREDENTIALS -> {
            AnimaTextField(state.email, actions.onEmailChange, "Email de contacto")
            Spacer(modifier = Modifier.height(24.dp))
            AnimaTextField(state.password, actions.onPasswordChange, "Contraseña mística", isPassword = true)
        }
        RegistrationStep.CHARACTER_INFO -> {
            AnimaTextField(state.username, actions.onUsernameChange, "Nombre del viajero")
        }
        RegistrationStep.FINALIZING -> {
            Text(
                text = "Tu alma ha sido vinculada al Códice de Gaia.\n¿Deseas sellar el pacto?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif
                ),
                color = Color.Black
            )
        }
    }
}

@Composable
private fun LoginNavigationButtons(currentStep: RegistrationStep, actions: LoginActions) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep != RegistrationStep.CREDENTIALS) {
            TextButton(onClick = actions.onBackStep) {
                Text(
                    text = "VOLVER",
                    color = Color.Black.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        } else {
            Spacer(modifier = Modifier.width(10.dp))
        }

        Button(
            onClick = if (currentStep == RegistrationStep.FINALIZING) actions.onLoginSubmit else actions.onNextStep,
            modifier = Modifier
                .height(50.dp)
                .width(140.dp),
            shape = RoundedCornerShape(2.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
        ) {
            Text(
                text = if (currentStep == RegistrationStep.FINALIZING) "SELLAR" else "SEGUIR",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenContentPreview() {
    LoginScreenContent(
        state = LoginState(),
        actions = LoginActions(),
        Modifier
    )
}