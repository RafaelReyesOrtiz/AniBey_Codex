package com.example.anibey_codex_tfg.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anibey_codex_tfg.R
import com.example.anibey_codex_tfg.ui.common.component.AnimaTextField
import com.example.anibey_codex_tfg.ui.common.component.AnimaToast
import com.example.anibey_codex_tfg.ui.common.component.ProfilePhotoSelector
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed

data class ProfileActions(
    val onUsernameChange: (String) -> Unit = {},
    val onEmailChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onCurrentPasswordChange: (String) -> Unit = {},
    val onPhotoChange: (String?) -> Unit = {},
    val onSave: () -> Unit = {},
    val onBack: () -> Unit = {},
    val uploadPhoto: (Uri) -> Unit = {},
    val onDeletePhoto: () -> Unit = {},
    val onDismissDiscardDialog: () -> Unit = {},
    val onConfirmDiscard: () -> Unit = {},
    val onDismissError: () -> Unit = {},
    val onDismissSuccess: () -> Unit = {}
)

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = viewModel.state

    LaunchedEffect(state.shouldNavigateToWelcome) {
        if (state.shouldNavigateToWelcome) {
            onLogout()
        }
    }

    BackHandler {
        viewModel.onBackRequested(onNavigateBack)
    }

    val actions = ProfileActions(
        onUsernameChange = viewModel::onUsernameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onCurrentPasswordChange = viewModel::onCurrentPasswordChange,
        onPhotoChange = viewModel::onPhotoChange,
        onSave = { viewModel.saveProfile(onAutoLogout = onLogout) },
        onBack = { viewModel.onBackRequested(onNavigateBack) },
        uploadPhoto = viewModel::uploadPhoto,
        onDeletePhoto = { viewModel.onPhotoChange(null) },
        onDismissDiscardDialog = viewModel::onDismissDiscardDialog,
        onConfirmDiscard = {
            viewModel.onDismissDiscardDialog()
            onNavigateBack()
        },
        onDismissError = viewModel::onDismissError,
        onDismissSuccess = viewModel::onDismissSuccess
    )

    ProfileContent(
        state = state,
        actions = actions,
        modifier = modifier
    )
}

@Composable
fun ProfileContent(
    state: ProfileState,
    actions: ProfileActions,
    modifier: Modifier
) {
    val scrollState = rememberScrollState()
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            actions.uploadPhoto(uri)
        }
    }

    DiscardChangesDialog(state, actions)
    
    if (state.isCheckingEmailVerification) {
        VerificationWaitingDialog()
    }

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
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(actions)
            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(32.dp))

            ProfilePhotoSelector(
                photoUrl = state.photoUrl,
                onSelectClick = { imageLauncher.launch("image/*") },
                onDeleteClick = actions.onDeletePhoto,
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileFormFields(state, actions)
            }

            SaveButton(state, actions)
        }

        // Toast de Error
        AnimaToast(
            show = state.generalError != null,
            message = state.generalError ?: "",
            onDismiss = actions.onDismissError,
            isSuccess = false
        )

        // Toast de Éxito
        AnimaToast(
            show = state.updateSuccess,
            message = "¡Esencia actualizada con éxito!",
            onDismiss = actions.onDismissSuccess,
            isSuccess = true
        )
    }
}

@Composable
private fun VerificationWaitingDialog() {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("VERIFICACIÓN EN CURSO", color = PrimaryRed) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Pulsa el enlace de tu correo.\nEsta pantalla se cerrará sola al confirmar.",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = PrimaryRed)
            }
        },
        confirmButton = {},
        containerColor = Color.Black.copy(alpha = 0.95f)
    )
}

@Composable
private fun DiscardChangesDialog(state: ProfileState, actions: ProfileActions) {
    if (state.isDiscardDialogOpen) {
        AlertDialog(
            onDismissRequest = actions.onDismissDiscardDialog,
            title = { Text("¿DESCARTAR CAMBIOS?") },
            text = { Text("Si sales ahora, perderás las modificaciones no guardadas.") },
            confirmButton = {
                TextButton(onClick = actions.onConfirmDiscard) {
                    Text("DESCARTAR", color = PrimaryRed)
                }
            },
            dismissButton = {
                TextButton(onClick = actions.onDismissDiscardDialog) {
                    Text("CANCELAR", color = Color.Gray)
                }
            },
            containerColor = Color.DarkGray,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

@Composable
private fun ProfileHeader(actions: ProfileActions) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = actions.onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Mi Perfil",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                shadow = Shadow(color = Color.Black, blurRadius = 4f)
            ),
            color = Color.White
        )
    }
}

@Composable
private fun ProfileFormFields(state: ProfileState, actions: ProfileActions) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AnimaTextField(
            value = state.username,
            onValueChange = actions.onUsernameChange,
            label = "Apodo",
            errorMessage = state.usernameError
        )

        AnimaTextField(
            value = state.email,
            onValueChange = actions.onEmailChange,
            label = "Correo Electrónico",
            errorMessage = state.emailError
        )

        AnimaTextField(
            value = state.password,
            onValueChange = actions.onPasswordChange,
            label = "Nueva Contraseña (opcional)",
            isPassword = true,
            errorMessage = state.passwordError
        )

        AnimaTextField(
            value = state.currentPassword,
            onValueChange = actions.onCurrentPasswordChange,
            label = "Contraseña Actual (requerida)",
            isPassword = true,
            errorMessage = state.currentPasswordError
        )
    }
}

@Composable
private fun SaveButton(state: ProfileState, actions: ProfileActions) {
    // Calculamos si hay cambios aquí para habilitar o deshabilitar el botón
    // Nota: Para una precisión absoluta, el ViewModel debería exponer este booleano.
    // Pero por consistencia con el código actual, lo dejamos reactivo al estado de carga.
    
    Button(
        onClick = actions.onSave,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(50.dp),
        enabled = !state.isLoading && !state.isCheckingEmailVerification,
        shape = RoundedCornerShape(2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryRed,
            disabledContainerColor = PrimaryRed.copy(alpha = 0.5f)
        )
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
        } else {
            Text("GUARDAR CAMBIOS", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
