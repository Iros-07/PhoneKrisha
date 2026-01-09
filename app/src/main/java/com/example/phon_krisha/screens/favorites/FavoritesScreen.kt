package com.example.phon_krisha.favorites

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.phon_krisha.home.AdCard
import com.example.phon_krisha.network.Ad
import com.example.phon_krisha.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FavoritesScreen(currentUserId: Int) {
    var favorites by remember { mutableStateOf<List<Ad>>(emptyList()) }

    LaunchedEffect(currentUserId) {
        withContext(Dispatchers.IO) {
            try {
                favorites = ApiClient.instance.getFavorites(currentUserId)
            } catch (e: Exception) {
                favorites = emptyList()
            }
        }
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        if (favorites.isEmpty()) {
            item { Text("Избранное пусто") }
        } else {
            items(favorites) { ad ->
                AdCard(ad = ad, currentUserId = currentUserId, onClick = { /* Открыть детали */ })
            }
        }
    }
}