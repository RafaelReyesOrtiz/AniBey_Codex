package com.example.anibey_codex_tfg.ui.screens.lugares

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.example.anibey_codex_tfg.R
import com.example.anibey_codex_tfg.domain.model.Lugar
import com.example.anibey_codex_tfg.ui.common.FileUtils
import com.example.anibey_codex_tfg.ui.common.component.CodexSearchBar
import com.example.anibey_codex_tfg.ui.common.component.EmptyScreen
import com.example.anibey_codex_tfg.ui.common.component.ErrorScreen
import com.example.anibey_codex_tfg.ui.common.component.LoadingScreen
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed

// Data class para agrupar las acciones (State Hoisting)
data class LugaresActions(
    val onBackClick: () -> Unit = {},
    val onLugarClick: (String) -> Unit = {},
    val onSearchQueryChange: (String) -> Unit = {},
    val onReload: () -> Unit = {},
    val getLugaresFiltrados: () -> List<Lugar> = { emptyList() }
)

@Composable
fun LugaresScreen(
    viewModel: LugaresViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onLugarClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Creamos la instancia de acciones
    val actions = LugaresActions(
        onBackClick = onBackClick,
        onLugarClick = onLugarClick,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onReload = viewModel::recargarLugares,
        getLugaresFiltrados = viewModel::getLugaresFiltrados
    )

    LugaresScreenContent(
        uiState = uiState,
        searchQuery = searchQuery,
        actions = actions
    )
}

@Composable
fun LugaresScreenContent(
    uiState: LugaresStates,
    searchQuery: String,
    actions: LugaresActions
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.fondo_login),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.5f), BlendMode.Darken)
            ),
        topBar = { LugaresTopBar(actions.onBackClick) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CodexSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = actions.onSearchQueryChange,
                hint = "Buscar lugares..."
            )

            when (uiState) {
                is LugaresStates.Loading -> LoadingScreen()
                is LugaresStates.Error -> ErrorScreen(
                    error = uiState.message,
                    onRetry = actions.onReload
                )
                is LugaresStates.Empty -> EmptyScreen()
                is LugaresStates.Success -> {
                    val filtrados = actions.getLugaresFiltrados()
                    if (filtrados.isEmpty()) {
                        EmptyScreen()
                    } else {
                        LugaresList(
                            lugares = filtrados,
                            onLugarClick = actions.onLugarClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LugaresTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                "LUGARES",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    color = Color.White
                )
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
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black.copy(alpha = 0.85f)
        )
    )
}

@Composable
fun LugaresList(
    lugares: List<Lugar>,
    onLugarClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(lugares) { lugar ->
            LugarCard(
                lugar = lugar,
                onClick = { onLugarClick(lugar.id) }
            )
        }
    }
}

@Composable
fun LugarCard(
    lugar: Lugar,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .border(1.dp, PrimaryRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(116.dp)
                    .width(100.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.DarkGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (lugar.imagenURL.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = FileUtils.formatDriveUrl(lugar.imagenURL),
                        contentDescription = lugar.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = PrimaryRed,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    )
                }
            }

            // Información
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = lugar.nombre.uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = PrimaryRed
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = lugar.descripcion,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.7f)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (lugar.region.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = lugar.region,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.6f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LugaresScreenPreview() {
    val mockLugares = listOf(
        Lugar(
            id = "1",
            nombre = "Puerto de Tol Rauko",
            region = "Tierras Sombrías",
            descripcion = "Un enclave místico donde las aguas del mar del olvido rompen contra los acantilados de la eternidad.",
            imagenURL = ""
        ),
        Lugar(
            id = "2",
            nombre = "Catedral de Gaibor",
            region = "Abel",
            descripcion = "La joya arquitectónica de la humanidad, centro espiritual del Sacro Santo Imperio.",
            imagenURL = ""
        )
    )

    LugaresScreenContent(
        uiState = LugaresStates.Success(mockLugares),
        searchQuery = "",
        actions = LugaresActions(
            getLugaresFiltrados = { mockLugares }
        )
    )
}
