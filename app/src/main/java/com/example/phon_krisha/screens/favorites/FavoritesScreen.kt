package com.example.phon_krisha.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.phon_krisha.network.ApiClient
import com.example.phon_krisha.network.Ad
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    userId: Int,
    onAdClick: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    var favorites by remember { mutableStateOf<List<Ad>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            favorites = ApiClient.api.getFavorites(userId) ?: emptyList()
        } catch (_: Exception) {
            favorites = emptyList()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (favorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Нет избранных объявлений")
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(favorites) { ad ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                onClick = { onAdClick(ad.id) }
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(ad.title.ifBlank { "Без названия" })
                    Text("Цена: ${ad.price ?: 0}")
                    Text("Город: ${ad.city.ifBlank { "Не указан" }}")
                }
            }
        }
    }
}
