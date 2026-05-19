package com.example.anibey_codex_tfg.ui.screens.home

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.anibey_codex_tfg.R
import com.example.anibey_codex_tfg.domain.model.UserProfile
import com.example.anibey_codex_tfg.ui.common.component.AnimaToast
import com.example.anibey_codex_tfg.ui.common.component.LoadingScreen
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed
import com.example.anibey_codex_tfg.ui.common.theme.SoftGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HomeActions(
    val onNavigateToProfile: () -> Unit = {},
    val onNavigateToLogin: () -> Unit = {},
    val onNavigateToRegister: () -> Unit = {},
    val onNavigateToLugares: () -> Unit = {},
    val onNavigateToBestiario: () -> Unit = {},
    val onNavigateToGrimorio: () -> Unit = {},
    val onLogout: () -> Unit = {},
    val onBlockedFeatureClick: (String) -> Unit = {},
    val onDismissToast: () -> Unit = {}
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToLugares: () -> Unit = {},
    onNavigateToBestiario: () -> Unit = {},
    onNavigateToGrimorio: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val actions = remember(viewModel) {
        HomeActions(
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToRegister = onNavigateToRegister,
            onNavigateToLugares = onNavigateToLugares,
            onNavigateToBestiario = onNavigateToBestiario,
            onNavigateToGrimorio = onNavigateToGrimorio,
            onLogout = { viewModel.logout(onLogout) },
            onBlockedFeatureClick = viewModel::onBlockedFeatureClick,
            onDismissToast = viewModel::dismissToast
        )
    }

    if (uiState.isLoading) {
        LoadingScreen(message = "Sincronizando el Nexo...")
    } else {
        HomeContent(uiState = uiState, actions = actions)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: HomeUiState,
    actions: HomeActions
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Box raíz para asegurar que el Toast siempre esté en la capa superior
    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                HomeDrawerContent(
                    uiState = uiState,
                    actions = actions,
                    closeDrawer = { scope.launch { drawerState.close() } }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    HomeTopBar(onMenuClick = { scope.launch { drawerState.open() } })
                }
            ) { padding ->
                MainBackgroundWrapper(padding) {
                    if (uiState.isGuest || uiState.userProfile == null) {
                        GuestHomeContent(
                            onNavigateToLogin = actions.onNavigateToLogin,
                            onNavigateToRegister = actions.onNavigateToRegister
                        )
                    } else {
                        AuthenticatedHomeContent(
                            userProfile = uiState.userProfile,
                            onNavigateToProfile = actions.onNavigateToProfile,
                            onNavigateToGrimorio = actions.onNavigateToGrimorio
                        )
                    }
                }
            }
        }

        AnimaToast(
            show = uiState.toastMessage != null,
            message = uiState.toastMessage ?: "",
            onDismiss = actions.onDismissToast
        )
    }
}

@Composable
fun HomeDrawerContent(
    uiState: HomeUiState,
    actions: HomeActions,
    closeDrawer: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "ANIBEY CODEX",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryRed,
                    letterSpacing = 4.sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            DrawerItem("LUGARES", Icons.Default.Place, onClick = {
                closeDrawer()
                actions.onNavigateToLugares()
            })

            DrawerItem("BESTIARIO", Icons.Default.Pets, onClick = {
                closeDrawer()
                actions.onNavigateToBestiario()
            })

            DrawerItem(
                label = "PERFIL DE ALMA",
                icon = Icons.Default.Person,
                enabled = !uiState.isGuest,
                onClick = {
                    if (uiState.isGuest) {
                        actions.onBlockedFeatureClick("Debes iniciar sesión para acceder al Perfil de Alma.")
                    } else {
                        closeDrawer()
                        actions.onNavigateToProfile()
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            DrawerItem(
                label = "GRIMORIO",
                icon = Icons.Default.AutoStories,
                enabled = !uiState.isGuest,
                onClick = {
                    if (uiState.isGuest) {
                        actions.onBlockedFeatureClick("El Grimorio solo se revela ante usuarios registrados.")
                    } else {
                        closeDrawer()
                        actions.onNavigateToGrimorio()
                    }
                }
            )

            DrawerItem(
                label = if (uiState.isGuest) "VOLVER AL INICIO" else "DESVANECER (LOGOUT)",
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                color = PrimaryRed,
                onClick = {
                    closeDrawer()
                    actions.onLogout()
                }
            )
        }
    }
}

@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector? = null,
    color: Color = SoftGray,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val displayColor = if (enabled) color else color.copy(alpha = 0.3f)

    NavigationDrawerItem(
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, color = displayColor, fontWeight = FontWeight.Medium)
                if (!enabled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Bloqueado",
                        tint = displayColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        icon = icon?.let { { Icon(it, contentDescription = null, tint = displayColor) } },
        selected = false,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                "CRÓNICAS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    color = Color.White
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black.copy(alpha = 0.85f)
        )
    )
}

@Composable
fun MainBackgroundWrapper(padding: PaddingValues, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .paint(
                painter = painterResource(id = R.drawable.fondo_login),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.5f), BlendMode.Darken)
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun GuestHomeContent(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VISITANTE DEL VACÍO",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        shadow = Shadow(color = PrimaryRed, blurRadius = 8f)
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tu esencia aún no ha sido registrada. El acceso a las crónicas profundas permanece sellado.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text("FORJAR VÍNCULO", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "INICIAR SESIÓN",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedHomeContent(
    userProfile: UserProfile?,
    onNavigateToProfile: () -> Unit,
    onNavigateToGrimorio: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        ProfileImageHeader(
            photoUrl = userProfile?.photoUrl,
            onProfileClick = onNavigateToProfile
        )

        Spacer(modifier = Modifier.height(24.dp))

        BienvenidaSeccion(username = userProfile?.username)

        Spacer(modifier = Modifier.height(32.dp))

        GrimorioCard(
            hechizosCount = userProfile?.grimorio?.size ?: 0,
            onClick = onNavigateToGrimorio
        )
    }
}

// Si la foto es un link de internet, lo deja pasar.
// Si es un código en Base64 porque viene de la galería, lo convierte en imagen real.
// Así el componente no falla y acepta cualquier tipo de foto que le llegue.
@Composable
private fun ProfileImageHeader(
    photoUrl: String?,
    onProfileClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val imageData = remember(photoUrl) {
        if (photoUrl != null && !photoUrl.startsWith("http")) {
            try {
                val decodedString = Base64.decode(photoUrl, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            } catch (_: Exception) { null }
        } else photoUrl
    }

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(170.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(PrimaryRed.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        var isAnimating by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (isAnimating) 1.3f else 1f,
            animationSpec = tween(durationMillis = 500)
        )

        Image(
            painter = rememberAsyncImagePainter(
                model = imageData ?: R.drawable.default_avatar,
                placeholder = painterResource(R.drawable.default_avatar),
                error = painterResource(R.drawable.default_avatar)
            ),
            contentDescription = "Foto de perfil",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .border(2.dp, PrimaryRed.copy(alpha = 0.6f), CircleShape)
                .scale(scale)
                .clickable {
                    isAnimating = true
                    scope.launch {
                        delay(300)
                        onProfileClick()
                        isAnimating = false
                    }
                }
        )
    }
}

@Composable
private fun BienvenidaSeccion(username: String?) {
    Text(
        text = "SALUDOS, ${username?.uppercase() ?: "VIAJERO"}",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            shadow = Shadow(color = Color.Black, blurRadius = 8f)
        ),
        color = Color.White,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun GrimorioCard(
    hechizosCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PrimaryRed.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = null,
                tint = PrimaryRed,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "TU GRIMORIO",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = when (hechizosCount) {
                        0 -> ""
                        1 -> "1 hechizo imbuido"
                        else -> "$hechizosCount hechizos imbuidos"
                    },
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier
                    .size(20.dp)
                    .scale(-1f, 1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreview() {
    HomeContent(
        uiState = HomeUiState(isGuest = true, toastMessage = "Inicia sesión para ver tu perfil."),
        actions = HomeActions()
    )
}
