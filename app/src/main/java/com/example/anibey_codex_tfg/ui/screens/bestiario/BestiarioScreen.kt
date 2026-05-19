package com.example.anibey_codex_tfg.ui.screens.bestiario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.anibey_codex_tfg.domain.model.Monstruo
import com.example.anibey_codex_tfg.domain.model.NivelPeligro
import com.example.anibey_codex_tfg.domain.model.nivelPeligro
import com.example.anibey_codex_tfg.ui.common.FileUtils
import com.example.anibey_codex_tfg.ui.common.component.CodexSearchBar
import com.example.anibey_codex_tfg.ui.common.component.EmptyScreen
import com.example.anibey_codex_tfg.ui.common.component.ErrorScreen
import com.example.anibey_codex_tfg.ui.common.component.LoadingScreen
import com.example.anibey_codex_tfg.ui.common.theme.GoldAccent
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed

@Composable
fun BestiarioScreen(
    viewModel: BestiarioViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onMonstruoClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState is BestiarioStates.Success) {
            val urls = (uiState as BestiarioStates.Success).monstruos.map { it.imagenURL }
            FileUtils.preloadImages(context, urls)
        }
    }

    BestiarioContent(
        uiState = uiState,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onRetry = viewModel::cargarBestiario,
        onBackClick = onBackClick,
        onMonstruoClick = onMonstruoClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestiarioContent(
    uiState: BestiarioStates,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onMonstruoClick: (String) -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val isSearching = searchQuery.isNotEmpty()

    LaunchedEffect(searchQuery) { selectedIndex = 0 }

    Scaffold(
        containerColor = Color(0xFF050505),
        topBar = {
            TopAppBar(
                title = { Text("CODEX BESTIARIO", color = Color.White, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CodexSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                hint = "Buscar en el bestiario..."
            )

            Box(modifier = Modifier.weight(1f)) {
                when (uiState) {
                    is BestiarioStates.Loading -> LoadingScreen()
                    is BestiarioStates.Error -> ErrorScreen(error = uiState.message, onRetry = onRetry)
                    is BestiarioStates.Empty -> EmptyScreen()
                    is BestiarioStates.Success -> {
                        if (isSearching) {
                            BestiarioSearchGrid(
                                monstruos = uiState.monstruos,
                                onMonstruoClick = onMonstruoClick
                            )
                        } else {
                            BestiarioBrowseMode(
                                monstruos = uiState.monstruos,
                                selectedIndex = selectedIndex,
                                onSelect = { selectedIndex = it },
                                onMonstruoClick = onMonstruoClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BestiarioBrowseMode(
    monstruos: List<Monstruo>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onMonstruoClick: (String) -> Unit
) {
    val safeIndex = if (monstruos.isEmpty()) 0 else selectedIndex.coerceIn(0, monstruos.size - 1)
    val selectedMonstruo = if (monstruos.isNotEmpty()) monstruos[safeIndex] else null

    Column(modifier = Modifier.fillMaxSize()) {
        MonstruoVisualizer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
                .padding(16.dp)
                .clickable { selectedMonstruo?.let { onMonstruoClick(it.id) } },
            monstruo = selectedMonstruo
        )

        MonstruoSelectorList(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f),
            monstruos = monstruos,
            selectedIndex = safeIndex,
            onSelect = onSelect
        )
    }
}

@Composable
fun MonstruoVisualizer(modifier: Modifier = Modifier, monstruo: Monstruo?) {
    Column(
        modifier = modifier
            .background(Color(0xFF101010), RoundedCornerShape(12.dp))
            .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .border(2.dp, PrimaryRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (monstruo != null && monstruo.imagenURL.isNotEmpty()) {
                AsyncImage(
                    model = FileUtils.formatDriveUrl(monstruo.imagenURL, sz = 800),
                    contentDescription = monstruo.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text("IDENTIFICANDO...", color = Color.DarkGray, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        monstruo?.let {
            val colorNivel = when(it.nivelPeligro) {
                NivelPeligro.BAJO -> Color(0xFF4FC3F7)
                NivelPeligro.MEDIO -> Color(0xFFFFD54F)
                NivelPeligro.ALTO -> Color(0xFFEF5350)
                NivelPeligro.LEGENDARIO -> Color(0xFFAB47BC)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = it.nombre.uppercase(),
                    color = PrimaryRed,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NIVEL ${it.nivel}", 
                    color = colorNivel, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun MonstruoSelectorList(modifier: Modifier = Modifier, monstruos: List<Monstruo>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex) { 
        if (monstruos.isNotEmpty()) listState.animateScrollToItem(selectedIndex) 
    }

    LazyColumn(
        state = listState,
        modifier = modifier.background(Color.Black),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        itemsIndexed(monstruos) { index, monstruo ->
            val isSelected = index == selectedIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(index) }
                    .background(if (isSelected) PrimaryRed.copy(alpha = 0.15f) else Color.Transparent)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(if (isSelected) 8.dp else 4.dp).background(if (isSelected) PrimaryRed else Color.DarkGray, CircleShape))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${(index + 1).toString().padStart(3, '0')} - ${monstruo.nombre}",
                    color = if (isSelected) Color.White else Color.Gray,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "LVL ${monstruo.nivel}", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.2f), thickness = 0.5.dp)
        }
    }
}

@Composable
fun BestiarioSearchGrid(monstruos: List<Monstruo>, onMonstruoClick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(monstruos) { monstruo ->
            SearchMonstruoCard(
                monstruo = monstruo,
                onClick = { onMonstruoClick(monstruo.id) }
            )
        }
    }
}

@Composable
fun SearchMonstruoCard(monstruo: Monstruo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.Black)) {
                AsyncImage(
                    model = FileUtils.formatDriveUrl(monstruo.imagenURL, sz = 400),
                    contentDescription = monstruo.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier.padding(8.dp).fillMaxWidth(), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = monstruo.nombre, 
                    color = Color.White, 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Bold, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Nivel ${monstruo.nivel}", 
                    color = GoldAccent, 
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BestiarioPreview() {
    BestiarioContent (
        uiState = BestiarioStates.Success(emptyList()),
        searchQuery = "",
        onSearchQueryChange = {},
        onRetry = {},
        onBackClick = {},
        onMonstruoClick = {}
    )
}
