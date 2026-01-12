package com.example.phon_krisha.screens.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.phon_krisha.network.Ad
import com.example.phon_krisha.network.AddFavoriteRequest
import com.example.phon_krisha.network.ApiClient
import com.example.phon_krisha.network.RemoveFavoriteRequest
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    loggedInUserId: Int?,  // ← параметр, а не by
    onAdClick: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()

    var ads by remember { mutableStateOf(listOf<Ad>()) }
    var favorites by remember { mutableStateOf(setOf<Int>()) }

    var cityFilter by remember { mutableStateOf("") }
    var roomsFilter by remember { mutableStateOf("") }

    LaunchedEffect(cityFilter, roomsFilter) {
        try {
            ads = ApiClient.api.getAds(
                city = if (cityFilter.isBlank()) null else cityFilter,
                rooms = roomsFilter.toIntOrNull()
            )

            if (loggedInUserId != null) {
                val favList = ApiClient.api.getFavorites(loggedInUserId)
                favorites = favList.map { it.id }.toSet()
            }
        } catch (e: Exception) {
            Log.e("HomeScreen", "Ошибка загрузки", e)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = cityFilter,
                onValueChange = { cityFilter = it },
                label = { Text("Город") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = roomsFilter,
                onValueChange = { roomsFilter = it },
                label = { Text("Комнат") },
                modifier = Modifier.width(100.dp)
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            items(ads) { ad ->
                Card(
                    onClick = { onAdClick(ad.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(ad.title, style = MaterialTheme.typography.titleMedium)
                        Text(ad.description.take(100) + "...", style = MaterialTheme.typography.bodyMedium)
                        Text("Цена: ${ad.price} ₸", style = MaterialTheme.typography.bodyLarge)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Город: ${ad.city} • ${ad.rooms} комнат", style = MaterialTheme.typography.bodySmall)

                            IconButton(onClick = {
                                if (loggedInUserId == null) return@IconButton

                                scope.launch {
                                    val isFav = favorites.contains(ad.id)
                                    try {
                                        if (isFav) {
                                            ApiClient.api.removeFavorite(RemoveFavoriteRequest(loggedInUserId, ad.id))
                                            favorites = favorites - ad.id
                                        } else {
                                            ApiClient.api.addFavorite(AddFavoriteRequest(loggedInUserId, ad.id))
                                            favorites = favorites + ad.id
                                        }
                                    } catch (e: Exception) {
                                        Log.e("Favorites", "Ошибка лайка", e)
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = if (favorites.contains(ad.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Избранное",
                                    tint = if (favorites.contains(ad.id)) Color.Red else LocalContentColor.current
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}