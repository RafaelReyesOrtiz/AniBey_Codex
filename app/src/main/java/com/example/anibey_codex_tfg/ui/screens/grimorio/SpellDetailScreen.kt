package com.example.anibey_codex_tfg.ui.screens.grimorio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.anibey_codex_tfg.domain.model.Hechizo
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed

// Identidad visual extendida por vía mágica
private fun getRamaColor(rama: String): Color {
    return when (rama.trim().uppercase()) {
        "FUEGO" -> Color(0xFFFF5722)
        "AGUA" -> Color(0xFF03A9F4)
        "AIRE" -> Color(0xFF00BCD4)
        "TIERRA" -> Color(0xFF795548)
        "LUZ" -> Color(0xFFFFD600)
        "OSCURIDAD" -> Color(0xFF673AB7)
        "CREACION" -> Color(0xFFE0E0E0)
        "DESTRUCCION" -> Color(0xFFB71C1C)
        "ESENCIA" -> Color(0xFFE91E63)
        "ILUSION" -> Color(0xFF009688)
        "NIGROMANCIA" -> Color(0xFF4CAF50)
        else -> Color(0xFF90A4AE)
    }
}

@Composable
fun SpellDetailScreen(
    spellId: String,
    onBackClick: () -> Unit,
    viewModel: SpellViewModel = hiltViewModel()
) {
    val hechizo by viewModel.selectedSpell.collectAsState()
    val grimorioIds by viewModel.grimorioIds.collectAsState()
    val isInGrimorio = grimorioIds.contains(spellId)

    LaunchedEffect(spellId) {
        viewModel.loadSpell(spellId)
    }

    SpellDetailContent(
        hechizo = hechizo,
        isInGrimorio = isInGrimorio,
        onBackClick = onBackClick,
        onToggleGrimorio = { viewModel.toggleGrimorio(spellId) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellDetailContent(
    hechizo: Hechizo?,
    isInGrimorio: Boolean,
    onBackClick: () -> Unit,
    onToggleGrimorio: () -> Unit
) {
    val ramaColor = hechizo?.ramaMagia?.let { getRamaColor(it) } ?: Color.Gray

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFF121212)
            ),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        hechizo?.nombre?.uppercase() ?: "CARGANDO...",
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleGrimorio) {
                        Icon(
                            imageVector = if (isInGrimorio) Icons.Filled.AutoStories else Icons.Outlined.AutoStories,
                            contentDescription = "Grimorio",
                            tint = if (isInGrimorio) ramaColor else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            if (hechizo == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryRed
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    // Cabecera con identidad de rama
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AbsoluteCutCornerShape(bottomRight = 40.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(1.dp, ramaColor)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Surface(
                                color = ramaColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = hechizo.ramaMagia.uppercase(),
                                    color = ramaColor,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "NIVEL DE VÍA: ${hechizo.nivel}",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Cuadrícula de Atributos adaptada
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AttributeBox(
                            "ZEON",
                            hechizo.zeonCost.toString(),
                            ramaColor,
                            Modifier.weight(1f)
                        )
                        AttributeBox("TIEMPO", hechizo.tiempoCasteo, ramaColor, Modifier.weight(1f))
                        AttributeBox("RANGO", hechizo.rango, ramaColor, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Descripción
                    SectionHeader("DESCRIPCIÓN", ramaColor)
                    Text(
                        text = hechizo.descripcion,
                        color = Color(0xFFE0E0E0),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 26.sp,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Efecto Místico con caja de énfasis
                    SectionHeader("EFECTO", ramaColor)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        color = ramaColor.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ramaColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = hechizo.efecto,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    if (isInGrimorio) {
                        Spacer(modifier = Modifier.height(48.dp))
                        Text(
                            text = "ESTE HECHIZO ESTÁ VINCULADO A TU ALMA",
                            color = ramaColor.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Column {
        Text(
            text = title,
            color = color,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            color = color.copy(alpha = 0.3f),
            thickness = 2.dp
        )
    }
}

@Composable
fun AttributeBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF1E1E1E))
            .border(1.dp, color.copy(alpha = 0.3f), AbsoluteCutCornerShape(4.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = color.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SpellDetailScreenPreview() {
    val dummyHechizo = Hechizo(
        id = "1",
        nombre = "Bola de Fuego",
        ramaMagia = "FUEGO",
        nivel = 10,
        tiempoCasteo = "1",
        zeonCost = 40,
        rango = "50m",
        descripcion = "Una esfera de fuego abrasador que estalla al contacto con el enemigo.",
        efecto = "Causa 40 puntos de daño de fuego en un área de 5 metros."
    )

    SpellDetailContent(
        hechizo = dummyHechizo,
        isInGrimorio = true,
        onBackClick = {},
        onToggleGrimorio = {}
    )
}
