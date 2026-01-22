//HomeScreen.kt
package com.example.phon_krisha.screens.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.phon_krisha.apistate.AuthState
import com.example.phon_krisha.network.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    loggedInUserId: Int?,
    onAdClick: (Int) -> Unit,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var ads by remember { mutableStateOf(listOf<Ad>()) }
    var favorites by remember { mutableStateOf(setOf<Int>()) }

    var titleFilter by remember { mutableStateOf("") }
    var cityFilter by remember { mutableStateOf("") }
    var roomsFilter by remember { mutableStateOf<Int?>(null) }
    var priceMinFilter by remember { mutableStateOf<Long?>(null) }
    var priceMaxFilter by remember { mutableStateOf<Long?>(null) }
    var adTypeFilter by remember { mutableStateOf("") }
    var houseTypeFilter by remember { mutableStateOf("") }
    var floorMinFilter by remember { mutableStateOf<Int?>(null) }
    var floorMaxFilter by remember { mutableStateOf<Int?>(null) }
    var yearBuiltMinFilter by remember { mutableStateOf<Int?>(null) }
    var yearBuiltMaxFilter by remember { mutableStateOf<Int?>(null) }
    var areaMinFilter by remember { mutableStateOf<Double?>(null) }
    var areaMaxFilter by remember { mutableStateOf<Double?>(null) }
    var complexFilter by remember { mutableStateOf("") }

    var showFilters by remember { mutableStateOf(false) }

    suspend fun loadAds() {
        try {
            ads = ApiClient.api.getAds(
                title = titleFilter.takeIf { it.isNotBlank() },
                city = cityFilter.takeIf { it.isNotBlank() },
                rooms = roomsFilter,
                price_min = priceMinFilter,
                price_max = priceMaxFilter,
                ad_type = adTypeFilter.takeIf { it.isNotBlank() },
                house_type = houseTypeFilter.takeIf { it.isNotBlank() },
                floor_min = floorMinFilter,
                floor_max = floorMaxFilter,
                year_built_min = yearBuiltMinFilter,
                year_built_max = yearBuiltMaxFilter,
                area_min = areaMinFilter,
                area_max = areaMaxFilter,
                complex = complexFilter.takeIf { it.isNotBlank() }
            )

            if (loggedInUserId != null && !AuthState.isGuest) {
                val favList = ApiClient.api.getFavorites(loggedInUserId)
                favorites = favList.map { it.id }.toSet()
            }
        } catch (e: Exception) {
            Log.e("HomeScreen", "Ошибка загрузки объявлений", e)
        }
    }

    LaunchedEffect(Unit) {
        loadAds()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Главная") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтры")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(ads) { ad ->
                    AdCard(
                        ad = ad,
                        isFavorite = favorites.contains(ad.id),
                        onClick = { onAdClick(ad.id) },
                        onFavoriteClick = {
                            scope.launch {
                                try {
                                    if (favorites.contains(ad.id)) {
                                        ApiClient.api.removeFavorite(RemoveFavoriteRequest(loggedInUserId!!, ad.id))
                                        favorites = favorites - ad.id
                                    } else {
                                        ApiClient.api.addFavorite(AddFavoriteRequest(loggedInUserId!!, ad.id))
                                        favorites = favorites + ad.id
                                    }
                                } catch (e: Exception) {
                                    Log.e("Favorite", "Ошибка избранного", e)
                                }
                            }
                        },
                        showFavoriteButton = loggedInUserId != null && !AuthState.isGuest
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // ===== ФИЛЬТРЫ =====
    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = sheetState
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                item { Text("Фильтры", style = MaterialTheme.typography.titleLarge) }

                item {
                    OutlinedTextField(value = cityFilter, onValueChange = { cityFilter = it }, label = { Text("Город") }, modifier = Modifier.fillMaxWidth())
                }

                item {
                    OutlinedTextField(value = roomsFilter?.toString() ?: "", onValueChange = { roomsFilter = it.toIntOrNull() }, label = { Text("Комнаты") }, modifier = Modifier.fillMaxWidth())
                }

                item {
                    Row {
                        OutlinedTextField(value = priceMinFilter?.toString() ?: "", onValueChange = { priceMinFilter = it.toLongOrNull() }, label = { Text("Цена от") }, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(value = priceMaxFilter?.toString() ?: "", onValueChange = { priceMaxFilter = it.toLongOrNull() }, label = { Text("Цена до") }, modifier = Modifier.weight(1f))
                    }
                }

                item {
                    OutlinedTextField(value = complexFilter, onValueChange = { complexFilter = it }, label = { Text("ЖК") }, modifier = Modifier.fillMaxWidth())
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Row {
                        OutlinedButton(
                            onClick = {
                                titleFilter = ""
                                cityFilter = ""
                                roomsFilter = null
                                priceMinFilter = null
                                priceMaxFilter = null
                                adTypeFilter = ""
                                houseTypeFilter = ""
                                floorMinFilter = null
                                floorMaxFilter = null
                                yearBuiltMinFilter = null
                                yearBuiltMaxFilter = null
                                areaMinFilter = null
                                areaMaxFilter = null
                                complexFilter = ""

                                scope.launch { loadAds() }
                                showFilters = false
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Сброс") }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    loadAds()
                                    showFilters = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Применить") }
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun AdCard(
    ad: Ad,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    showFavoriteButton: Boolean
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box {
                if (!ad.photos.isNullOrEmpty()) {
                    AsyncImage(
                        model = ad.photos!!.first(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ImageNotSupported, contentDescription = null, modifier = Modifier.size(64.dp))
                    }
                }
            }

            Column(Modifier.padding(16.dp)) {
                Text(ad.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${ad.price} ₸", style = MaterialTheme.typography.titleLarge)
                Text("${ad.city} • ${ad.rooms} к • ${ad.area} м²")

                if (showFavoriteButton) {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) Color.Red else LocalContentColor.current
                        )
                    }
                }
            }
        }
    }
}
