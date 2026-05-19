package com.example.anibey_codex_tfg.ui.screens.grimorio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anibey_codex_tfg.domain.model.Hechizo
import com.example.anibey_codex_tfg.ui.common.FileUtils.getRamaColor
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed

// Función helper para definir la identidad visual de cada rama


@Composable
fun SpellListScreen(
    onBackClick: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: SpellViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedRama by viewModel.selectedRama.collectAsState()
    val grimorioIds by viewModel.grimorioIds.collectAsState()
    val onlyGrimorio by viewModel.showOnlyGrimorio.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    SpellListContent(
        uiState = uiState,
        selectedRama = selectedRama,
        grimorioIds = grimorioIds,
        onlyGrimorio = onlyGrimorio,
        searchQuery = searchQuery,
        onBackClick = onBackClick,
        onNavigateToDetail = onNavigateToDetail,
        onRamaSelected = viewModel::selectRama,
        onToggleGrimorio = viewModel::toggleGrimorio,
        onToggleView = viewModel::setShowOnlyGrimorio,
        onSearchQueryChange = viewModel::setSearchQuery
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellListContent(
    uiState: SpellStates,
    selectedRama: String,
    grimorioIds: Set<String>,
    onlyGrimorio: Boolean,
    searchQuery: String,
    onBackClick: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onRamaSelected: (String) -> Unit,
    onToggleGrimorio: (String) -> Unit,
    onToggleView: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    // Fondo gris neutral profundo para reducir el contraste agresivo del negro puro
    val backgroundColor = Color(0xFF121212)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (onlyGrimorio) "MI GRIMORIO" else "HECHIZOS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // Selector de Sección (Biblioteca vs Mi Grimorio)
            TabRow(
                selectedTabIndex = if (onlyGrimorio) 1 else 0,
                containerColor = Color.Black,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (onlyGrimorio) 1 else 0]),
                        color = PrimaryRed
                    )
                }
            ) {
                Tab(
                    selected = !onlyGrimorio,
                    onClick = { onToggleView(false) },
                    text = { Text("BIBLIOTECA", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, null, modifier = Modifier.size(20.dp)) }
                )
                Tab(
                    selected = onlyGrimorio,
                    onClick = { onToggleView(true) },
                    text = { Text("MI GRIMORIO", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AutoStories, null, modifier = Modifier.size(20.dp)) }
                )
            }

            // Barra de Búsqueda con diseño más sutil
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar hechizo...", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedBorderColor = PrimaryRed.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.Transparent
                )
            )

            RunicFilterBar(selectedRama = selectedRama, onRamaSelected = onRamaSelected)

            Box(modifier = Modifier.weight(1f)) {
                when (uiState) {
                    is SpellStates.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryRed)
                        }
                    }
                    is SpellStates.Success -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.spells) { hechizo ->
                                SpellCard(
                                    hechizo = hechizo,
                                    isInGrimorio = grimorioIds.contains(hechizo.id),
                                    onClick = { onNavigateToDetail(hechizo.id) },
                                    onToggleGrimorio = { onToggleGrimorio(hechizo.id) }
                                )
                            }
                        }
                    }
                    is SpellStates.Empty -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (onlyGrimorio) "Tu grimorio está vacío.\nAñade hechizos desde la biblioteca." 
                                       else "No se encontraron hechizos arcana.",
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    }
                    is SpellStates.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error: ${uiState.message}", color = PrimaryRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RunicFilterBar(selectedRama: String, onRamaSelected: (String) -> Unit) {
    val ramas = listOf(
        "TODAS", "FUEGO", "AGUA", "AIRE", "TIERRA",
        "LUZ", "OSCURIDAD", "CREACION", "DESTRUCCION",
        "ESENCIA", "ILUSION", "NIGROMANCIA"
    )

    val selectedNormalized = selectedRama.uppercase()

    ScrollableTabRow(
        selectedTabIndex = ramas.indexOf(selectedNormalized).coerceAtLeast(0),
        containerColor = Color.Transparent,
        edgePadding = 16.dp,
        divider = {},
        indicator = {}
    ) {
        ramas.forEach { rama ->
            val ramaColor = getRamaColor(rama)
            FilterChip(
                selected = selectedNormalized == rama,
                onClick = { onRamaSelected(rama) },
                label = { Text(rama, fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ramaColor.copy(alpha = 0.2f),
                    labelColor = Color.Gray,
                    selectedLabelColor = ramaColor
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedNormalized == rama,
                    borderColor = Color.Transparent,
                    selectedBorderColor = ramaColor
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun SpellCard(
    hechizo: Hechizo,
    isInGrimorio: Boolean,
    onClick: () -> Unit,
    onToggleGrimorio: () -> Unit
) {
    val ramaColor = getRamaColor(hechizo.ramaMagia)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = AbsoluteCutCornerShape(topRight = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        border = BorderStroke(
            1.dp, 
            if (isInGrimorio) ramaColor else ramaColor.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleGrimorio) {
                Icon(
                    imageVector = if (isInGrimorio) Icons.Filled.AutoStories else Icons.Outlined.AutoStories,
                    contentDescription = null,
                    tint = if (isInGrimorio) ramaColor else Color.Gray
                )
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = hechizo.nombre.uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                
                Surface(
                    color = ramaColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = hechizo.ramaMagia.uppercase(),
                        color = ramaColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${hechizo.zeonCost}",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                Text(
                    text = "ZEON",
                    color = ramaColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "NIVEL ${hechizo.nivel}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SpellListScreenPreview() {
    SpellListContent (
        uiState = SpellStates.Success(emptyList()),
        selectedRama = "Todas",
        grimorioIds = emptySet(),
        onlyGrimorio = false,
        searchQuery = "",
        onBackClick = {},
        onNavigateToDetail = {},
        onRamaSelected = {},
        onToggleGrimorio = {},
        onToggleView = {},
        onSearchQueryChange = {}
    )
}
