package com.example.anibey_codex_tfg.ui.screens.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.anibey_codex_tfg.R
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed
import com.example.anibey_codex_tfg.ui.common.theme.SoftGray
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsState(initial = null)
    val isGuest by viewModel.isGuest.collectAsState(initial = false)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // MODULARIZADO: Contenido del Drawer
            HomeDrawerContent(
                isGuest = isGuest,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToRegister = onNavigateToRegister,
                onLogout = {
                    onLogout()
                },
                closeDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                // MODULARIZADO: Barra superior
                HomeTopBar(onMenuClick = { scope.launch { drawerState.open() } })
            }
        ) { padding ->
            // MODULARIZADO: Fondo y contenido central
            MainBackgroundWrapper(padding) {
                if (isGuest) {
                    GuestHomeContent(onNavigateToLogin, onNavigateToRegister)
                } else {
                    AuthenticatedHomeContent(userProfile, onNavigateToProfile)
                }
            }
        }
    }
}

@Composable
fun HomeDrawerContent(
    isGuest: Boolean,
    onNavigateToProfile: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLogout: () -> Unit,
    closeDrawer: () -> Unit
) {
    ModalDrawerSheet{
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp)
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

            if (!isGuest) {
                DrawerItem("PERFIL DE ALMA", Icons.Default.Person, onClick = {
                    closeDrawer()
                    onNavigateToProfile()
                })
            } else {
                DrawerItem("VINCULAR ESENCIA", Icons.Default.Lock, onClick = {
                    closeDrawer()
                    onNavigateToLogin()
                })
                DrawerItem("CREAR VÍNCULO", Icons.Default.Create, onClick = {
                    closeDrawer()
                    onNavigateToRegister()
                })
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!isGuest) {
                DrawerItem(
                    label = "DESVANECER (LOGOUT)",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    color = PrimaryRed,
                    onClick = {
                        closeDrawer()
                        onLogout()
                    }
                )
            }
        }
    }
}

@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector? = null,
    color: Color = SoftGray,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label, color = color, fontWeight = FontWeight.Medium) },
        icon = icon?.let { { Icon(it, contentDescription = null, tint = color) } },
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
    userProfile: com.example.anibey_codex_tfg.domain.model.UserProfile?,
    onNavigateToProfile: () -> Unit
) {
    val scope = rememberCoroutineScope()
    // Aseguramos que cargue el avatar por defecto si no hay URL
    val imageModel = remember(userProfile?.photoUrl) {
        if (userProfile?.photoUrl.isNullOrEmpty()) R.drawable.default_avatar else userProfile.photoUrl
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Aura de fondo
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
                targetValue = if (isAnimating) 1.15f else 1f,
                animationSpec = tween(durationMillis = 300)
            )

            Image(
                painter = rememberAsyncImagePainter(
                    model = imageModel,
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
                            kotlinx.coroutines.delay(300)
                            onNavigateToProfile()
                            isAnimating = false
                        }
                    }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "BIENVENIDO, ${userProfile?.username?.uppercase() ?: "VIAJERO"}",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                shadow = Shadow(color = Color.Black, blurRadius = 8f)
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tu esencia fluye en el Códice",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
        )
    }
}