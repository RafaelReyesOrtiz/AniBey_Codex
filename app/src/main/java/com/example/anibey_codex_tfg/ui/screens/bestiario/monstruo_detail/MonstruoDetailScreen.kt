package com.example.anibey_codex_tfg.ui.screens.bestiario.monstruo_detail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.anibey_codex_tfg.domain.model.Monstruo
import com.example.anibey_codex_tfg.domain.model.NivelPeligro
import com.example.anibey_codex_tfg.domain.model.nivelPeligro
import com.example.anibey_codex_tfg.ui.common.FileUtils
import com.example.anibey_codex_tfg.ui.common.component.ErrorScreen
import com.example.anibey_codex_tfg.ui.common.component.LoadingScreen
import com.example.anibey_codex_tfg.ui.common.theme.GoldAccent
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed

@Composable
fun MonstruoDetailScreen(
    monstruoId: String,
    viewModel: MonstruoDetailViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(monstruoId) {
        viewModel.getMonstruo(monstruoId)
    }

    when (val state = uiState) {
        is MonstruoDetailStates.Loading -> LoadingScreen()
        is MonstruoDetailStates.Error -> ErrorScreen(error = state.message, onRetry = { viewModel.getMonstruo(monstruoId) })
        is MonstruoDetailStates.Success -> {
            MonstruoDetailContent(
                monstruo = state.monstruo,
                onBackClick = onBackClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonstruoDetailContent(
    monstruo: Monstruo,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "BESTIA",
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFF080808) // Casi negro
    ) { padding ->
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Cabecera con Imagen Impactante
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                AsyncImage(
                    model = FileUtils.formatDriveUrl(monstruo.imagenURL, sz = 1200),
                    contentDescription = monstruo.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF080808)),
                                startY = 300f
                            )
                        )
                )

                DangerBadge(
                    monstruo = monstruo,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                )
            }

            // Título y Categoría
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = monstruo.nombre.uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        color = PrimaryRed
                    ),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "— ${monstruo.categoria} —",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontStyle = FontStyle.Italic,
                        color = GoldAccent
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Cuerpo del Compendio
            InfoSection(title = "RELATO ANCESTRAL", icon = Icons.AutoMirrored.Filled.MenuBook) {
                Text(
                    text = monstruo.descripcion,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp,
                        color = Color.LightGray
                    ),
                    textAlign = TextAlign.Justify
                )
            }

            InfoSection(title = "HABITAT CONOCIDO", icon = Icons.Default.LocationOn) {
                Text(
                    text = monstruo.habitat,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    fontWeight = FontWeight.Bold
                )
            }

            // Habilidades y Debilidades
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ListCard(
                    title = "HABILIDADES",
                    items = monstruo.habilidades,
                    color = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
                ListCard(
                    title = "DEBILIDADES",
                    items = monstruo.debilidades,
                    color = PrimaryRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = GoldAccent
                )
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 0.5.dp,
            color = GoldAccent.copy(alpha = 0.3f)
        )
        content()
    }
}

@Composable
fun ListCard(
    title: String,
    items: List<String>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF121212), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
            color = color
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (items.isEmpty()) {
            Text("Desconocido", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        } else {
            items.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("•", color = color, modifier = Modifier.padding(end = 4.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun DangerBadge(monstruo: Monstruo, modifier: Modifier = Modifier) {
    val dangerColor = when (monstruo.nivelPeligro) {
        NivelPeligro.BAJO -> Color(0xFF4FC3F7) // Azul claro / Cian
        NivelPeligro.MEDIO -> Color(0xFFFFD54F) // Dorado / Ámbar
        NivelPeligro.ALTO -> Color(0xFFEF5350) // Rojo suave
        NivelPeligro.LEGENDARIO -> Color(0xFFAB47BC) // Púrpura / Legendario
    }

    Box(
        modifier = modifier
            .drawBehind {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawRect(
                    color = dangerColor,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = pathEffect)
                )
            }
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = monstruo.nivelPeligro.name,
                style = MaterialTheme.typography.labelSmall,
                color = dangerColor.copy(alpha = 0.8f)
            )
            Text(
                text = "NIVEL ${monstruo.nivel}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            )
        }
    }
}

@Preview
@Composable
fun MonstruoDetailPreview() {
    val mock = Monstruo(
        id = "M-999",
        nombre = "Sierpe Mayor",
        descripcion = "Una colosal criatura que habita en las profundidades de las montañas. Su mera presencia altera el clima de la región.",
        categoria = "Dragón Ancestral",
        nivel = 8,
        habitat = "Cumbres de Tiembla",
        habilidades = listOf("Aliento Ígneo", "Piel de Diamante"),
        debilidades = listOf("Frío Absoluto")
    )
    MonstruoDetailContent(monstruo = mock, onBackClick = {})
}
