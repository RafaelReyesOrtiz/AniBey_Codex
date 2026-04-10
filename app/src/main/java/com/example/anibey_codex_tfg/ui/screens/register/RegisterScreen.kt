package com.example.anibey_codex_tfg.ui.screens.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anibey_codex_tfg.R
import com.example.anibey_codex_tfg.ui.common.component.AnimaTextField
import com.example.anibey_codex_tfg.ui.common.theme.AniBey_Codex_TFGTheme
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = viewModel.state
    val actions = RegisterActions(
        onEmailChange = viewModel::onEmailChange,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSendVerification = viewModel::sendVerificationEmail,
        onCheckVerification = viewModel::checkVerificationStatus,
        onFinalizePact = viewModel::finalizeRegistration,
        onRegisterSuccess = onRegisterSuccess,
        onBack = {
            if (state.currentStep == RegisterStep.EMAIL_ENTRY) onNavigateBack()
            else viewModel.editEmail()
        }
    )

    RegisterContent(state = state, actions = actions, modifier = modifier)
}

@Composable
fun RegisterContent(
    state: RegisterState,
    actions: RegisterActions,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.fondo_login),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.20f), BlendMode.Darken)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera que cambia según el paso
            RegisterHeader(step = state.currentStep)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state.currentStep) {
                    RegisterStep.EMAIL_ENTRY -> EmailEntryStep(state, actions)
                    RegisterStep.VERIFY_PENDING -> VerifyWaitStep(state, actions)
                    RegisterStep.FINALIZE_PACT -> FinalizePactStep(state, actions)
                }
            }

            // Botonera Inferior
            RegisterBottomBar(state = state, actions = actions)
        }
    }
}

@Composable
private fun EmailEntryStep(state: RegisterState, actions: RegisterActions) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Para iniciar el vínculo, necesitamos conocer tu rastro en el mundo mortal.",
            style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
            color = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        AnimaTextField(
            value = state.email,
            onValueChange = actions.onEmailChange,
            label = "E-mail de contacto",
            errorMessage = state.emailError
        )
    }
}

@Composable
private fun VerifyWaitStep(state: RegisterState, actions: RegisterActions) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RitualIcon(state.canResend, state.resendCountdown)

        Text(
            text = "EL SUSURRO HA SIDO LANZADO",
            style = MaterialTheme.typography.titleLarge.copy(
                color = PrimaryRed,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                shadow = Shadow(color = PrimaryRed.copy(alpha = 0.4f), blurRadius = 10f)
            ),
            textAlign = TextAlign.Center
        )

        EmailInfoCard(state.email)

        Box(
            modifier = Modifier
                .height(40.dp) // Altura suficiente para el icono y el texto
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val visibilityAlpha = if (!state.canResend) 1f else 0f
            Box(modifier = Modifier.graphicsLayer(alpha = visibilityAlpha)) {
                ResendCountdown(state.resendCountdown)
            }
        }

        // --- BOTONES ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = actions.onSendVerification,
                enabled = state.canResend,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (state.canResend) Color.White else Color.White.copy(alpha = 0.4f)
                )
            ) {
                Text("REENVIAR SUSURRO", style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
                onClick = actions.onBack,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.height(45.dp)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CORREGIR E-MAIL", style = MaterialTheme.typography.labelMedium)
            }
        }

        state.emailError?.let {
            Text(
                text = it,
                color = PrimaryRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 20.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RitualIcon(canResend: Boolean, countdown: Int) {
    // Usamos un Box con tamaño fijo para que actúe como "ancla" y no empuje el layout
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(130.dp)
            .padding(8.dp) // Espacio interno para que el grosor no se corte
    ) {
        CircularProgressIndicator(
            progress = { if (!canResend) (60 - countdown) / 60f else 1f },
            modifier = Modifier.fillMaxSize(),
            color = if (!canResend) Color(0xFFE0C060) else PrimaryRed,
            strokeWidth = 4.dp,
            trackColor = Color.White.copy(alpha = 0.1f),
            strokeCap = StrokeCap.Round,
        )

        Image(
            painter = painterResource(id = R.drawable.ic_whisper_sent),
            contentDescription = null,
            modifier = Modifier.size(60.dp)
        )
    }
}

@Composable
private fun EmailInfoCard(email: String) {
    Surface(
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Un lazo místico espera en tu bandeja de entrada:\n$email",
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun ResendCountdown(countdown: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth() // Quitamos el padding vertical para controlar la altura desde el padre
    ) {
        Icon(
            imageVector = Icons.Default.LockClock,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "PODRÁS REINTENTAR EN ${countdown}s",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                shadow = Shadow(color = Color.Black, blurRadius = 4f)
            ),
            color = Color(0xFFFFD700)
        )
    }
}

@Composable
private fun FinalizePactStep(state: RegisterState, actions: RegisterActions) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Vínculo confirmado. Ahora, elige tu identidad y protege tu esencia.",
            style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        AnimaTextField(
            value = state.username,
            onValueChange = actions.onUsernameChange,
            label = "Apodo del Viajero",
            errorMessage = state.usernameError
        )
        AnimaTextField(
            value = state.password,
            onValueChange = actions.onPasswordChange,
            label = "Contraseña de Alma",
            isPassword = true,
            errorMessage = state.passwordError
        )
    }
}

@Composable
private fun RegisterBottomBar(state: RegisterState, actions: RegisterActions) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = actions.onBack, enabled = !state.isLoading) {
            Text("VOLVER", color = Color.Black.copy(alpha = 0.5f))
        }

        Button(
            onClick = {
                when (state.currentStep) {
                    RegisterStep.EMAIL_ENTRY -> actions.onSendVerification()
                    RegisterStep.VERIFY_PENDING -> actions.onCheckVerification()
                    RegisterStep.FINALIZE_PACT -> actions.onFinalizePact(actions.onRegisterSuccess)
                }
            },
            modifier = Modifier
                .height(50.dp)
                .width(170.dp),
            enabled = !state.isLoading,
            shape = RoundedCornerShape(2.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
                val btnText = when (state.currentStep) {
                    RegisterStep.EMAIL_ENTRY -> "ENVIAR"
                    RegisterStep.VERIFY_PENDING -> "CONFIRMAR"
                    RegisterStep.FINALIZE_PACT -> "SELLAR"
                }
                Text(btnText)
            }
        }
    }
}

@Composable
private fun RegisterHeader(step: RegisterStep) {
    val title = when (step) {
        RegisterStep.EMAIL_ENTRY -> "VINCULACIÓN"
        RegisterStep.VERIFY_PENDING -> "EL SUSURRO"
        RegisterStep.FINALIZE_PACT -> "EL PACTO"
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

@Preview(showSystemUi = true)
@Composable
fun RegisterContentPreview() {
    AniBey_Codex_TFGTheme {
        RegisterContent(
            state = RegisterState(
                currentStep = RegisterStep.VERIFY_PENDING,
                resendCountdown = 50,
                canResend = false
            ),
            actions = RegisterActions(),
            modifier = Modifier
        )
    }
}