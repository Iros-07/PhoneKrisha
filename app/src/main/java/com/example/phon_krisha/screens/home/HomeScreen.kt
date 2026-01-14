package com.example.phon_krisha.screens.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.phon_krisha.apistate.AuthState
import com.example.phon_krisha.network.Ad
import com.example.phon_krisha.network.AddFavoriteRequest
import com.example.phon_krisha.network.ApiClient
import com.example.phon_krisha.network.RemoveFavoriteRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    loggedInUserId: Int?,
    onAdClick: (Int) -> Unit,
    navController: NavController
) {
    val scope = rememberCoroutineScope()

    var ads by remember { mutableStateOf(listOf<Ad>()) }
    var favorites by remember { mutableStateOf(setOf<Int>()) }

    // Фильтры (оставляем как было)
    var titleFilter by remember { mutableStateOf("") }
    var cityFilter by remember { mutableStateOf("") }
    var roomsFilter by remember { mutableStateOf("") }
    var priceMinFilter by remember { mutableStateOf("") }
    var priceMaxFilter by remember { mutableStateOf("") }
    var adTypeFilter by remember { mutableStateOf("") }
    var houseTypeFilter by remember { mutableStateOf("") }
    var floorMinFilter by remember { mutableStateOf("") }
    var floorMaxFilter by remember { mutableStateOf("") }
    var yearBuiltMinFilter by remember { mutableStateOf("") }
    var yearBuiltMaxFilter by remember { mutableStateOf("") }
    var areaMinFilter by remember { mutableStateOf("") }
    var areaMaxFilter by remember { mutableStateOf("") }
    var complexFilter by remember { mutableStateOf("") }

    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(
        titleFilter, cityFilter, roomsFilter, priceMinFilter, priceMaxFilter,
        adTypeFilter, houseTypeFilter, floorMinFilter, floorMaxFilter,
        yearBuiltMinFilter, yearBuiltMaxFilter, areaMinFilter, areaMaxFilter, complexFilter
    ) {
        try {
            ads = ApiClient.api.getAds(
                title = if (titleFilter.isBlank()) null else titleFilter,
                city = if (cityFilter.isBlank()) null else cityFilter,
                rooms = roomsFilter.toIntOrNull(),
                price_min = priceMinFilter.toLongOrNull(),
                price_max = priceMaxFilter.toLongOrNull(),
                ad_type = if (adTypeFilter.isBlank()) null else adTypeFilter,
                house_type = if (houseTypeFilter.isBlank()) null else houseTypeFilter,
                floor_min = floorMinFilter.toIntOrNull(),
                floor_max = floorMaxFilter.toIntOrNull(),
                year_built_min = yearBuiltMinFilter.toIntOrNull(),
                year_built_max = yearBuiltMaxFilter.toIntOrNull(),
                area_min = areaMinFilter.toDoubleOrNull(),
                area_max = areaMaxFilter.toDoubleOrNull(),
                complex = if (complexFilter.isBlank()) null else complexFilter
            )

            if (loggedInUserId != null && !AuthState.isGuest) {
                val favList = ApiClient.api.getFavorites(loggedInUserId)
                favorites = favList.map { it.id }.toSet()
            }
        } catch (e: Exception) {
            Log.e("HomeScreen", "Ошибка загрузки объявлений", e)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Главная") },
                navigationIcon = {
                    if (AuthState.isGuest || loggedInUserId != null) {
                        IconButton(onClick = { navController.navigate("profile") }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
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
            // Поиск + фильтры
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = titleFilter,
                    onValueChange = { titleFilter = it },
                    label = { Text("Поиск по заголовку") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showFilters = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Фильтры")
                }
            }

            if (showFilters) {
                // Ваш существующий код диалога фильтров (оставляем без изменений)
                AlertDialog(
                    onDismissRequest = { showFilters = false },
                    title = { Text("Фильтры") },
                    text = {
                        // ... (ваш код фильтров)
                    },
                    confirmButton = {
                        TextButton(onClick = { showFilters = false }) { Text("Применить") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFilters = false }) { Text("Отмена") }
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
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
                                        ApiClient.api.removeFavorite(
                                            RemoveFavoriteRequest(loggedInUserId!!, ad.id)
                                        )
                                        favorites = favorites - ad.id
                                    } else {
                                        ApiClient.api.addFavorite(
                                            AddFavoriteRequest(loggedInUserId!!, ad.id)
                                        )
                                        favorites = favorites + ad.id
                                    }
                                } catch (e: Exception) {
                                    Log.e("Favorite", "Ошибка изменения избранного", e)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Фото + индикатор количества
            Box {
                if (!ad.photos.isNullOrEmpty()) {
                    AsyncImage(
                        model = ad.photos.first(),
                        contentDescription = "Фото объявления",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ImageNotSupported,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }

                // Количество фото (если больше 1)
                if ((ad.photos?.size ?: 0) > 1) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.68f)
                    ) {
                        Text(
                            "+${(ad.photos?.size ?: 0) - 1}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Информация
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = ad.title.ifBlank { "Без названия" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${ad.price} ₸",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "${ad.city.ifBlank { "Город не указан" }} • ${ad.rooms} к.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Площадь: ${ad.area} м²",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (showFavoriteButton) {
                        IconButton(onClick = onFavoriteClick) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "В избранное",
                                tint = if (isFavorite) Color.Red else LocalContentColor.current
                            )
                        }
                    }
                }
            }
        }
    }
}